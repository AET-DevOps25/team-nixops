from openapi_server.apis.embedding_api_base import BaseEmbeddingApi
from openapi_server.apis.embedding_api import router as embedding_api_router
from openapi_server.models.study_program import StudyProgram

from pydantic import Field, StrictInt
from typing import List
from typing_extensions import Annotated
from sqlalchemy.orm import Session
from sqlalchemy import select
from ..db.relational_db import (
    engine,
    StudyProgram as SqlStudyProgram,
    Semester as SqlSemester,
)
from ..db.vector_db import create_collection, embed_text, milvus_client
from datetime import datetime, date
from openapi_server.models.study_program_selector_item import StudyProgramSelectorItem
import json

# NOTE: https://milvus.io/docs/use-async-milvus-client-with-asyncio.md#Create-index


# fixes "datetime.datetime not JSON serializable"
class datetime_encoder(json.JSONEncoder):
    def default(self, obj):
        if isinstance(obj, (datetime, date)):
            return str(obj)
        return json.JSONEncoder.default(self, obj)


class CustomEmbeddingApi(BaseEmbeddingApi):
    def __init_subclass__(cls, **kwargs):
        super().__init_subclass__(**kwargs)
        BaseEmbeddingApi.subclasses = BaseEmbeddingApi.subclasses + (cls,)

    async def create_study_program(
        self,
        study_program: StudyProgram,
    ) -> None:
        with Session(engine) as session:
            sem = list(
                map(lambda x: SqlSemester(name=x), study_program.semesters.keys())
            )

            sp = session.query(SqlStudyProgram).get(study_program.study_id)

            if sp:
                # Update existing fields
                sp.name = study_program.program_name
                sp.degree_program_name = study_program.degree_program_name
                sp.degree_type_name = study_program.degree_type_name
                sp.semesters = sem
            else:
                # Create new object if not exists
                sp = SqlStudyProgram(
                    id=study_program.study_id,
                    name=study_program.program_name,
                    degree_program_name=study_program.degree_program_name,
                    degree_type_name=study_program.degree_type_name,
                    semesters=sem,
                )
                session.add(sp)

            session.commit()

            for s in sem:
                collection_name = f"_{study_program.study_id}_{s.name}"
                create_collection(collection_name)
                milvus_client.load_collection(collection_name)
                modules = study_program.semesters[s.name]
                for mod in modules:
                    desc = (
                        str(mod.id)
                        + "\n\n"
                        + (mod.content or "")
                        + "\n\n"
                        + (mod.outcome or "")
                        + "\n\n"
                        + (mod.methods or "")
                        + "\n\n"
                        + (mod.exam or "")
                        + "\n\nCredits: "
                        + str(mod.credits)
                    )[:16192]

                    courses = json.dumps(mod.courses.to_dict(), cls=datetime_encoder)
                    data = {
                        "id": mod.id,
                        "name": mod.title,
                        "description": desc,
                        "description_vec": embed_text(desc),
                        "courses": courses,
                    }

                    res = milvus_client.insert(
                        collection_name=collection_name, data=[data]
                    )
                    milvus_client.flush(collection_name=collection_name)
                    print(res)
        return None

    async def fetch_study_programs(
        self,
    ) -> List[StudyProgramSelectorItem]:
        """Get all scraped study programs and matching semesters"""
        results: List[StudyProgramSelectorItem] = []
        with Session(engine) as session:
            stmt = select(SqlStudyProgram)
            for s in session.scalars(stmt):
                semesters = list(map(lambda sem: sem.name, s.semesters))
                res = StudyProgramSelectorItem(
                    title=s.degree_program_name, id=s.id, semesters=semesters
                )
                results.append(res)
        return results


router = embedding_api_router
