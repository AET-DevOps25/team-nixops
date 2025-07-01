from openapi_server.apis.embedding_api_base import BaseEmbeddingApi
from openapi_server.apis.embedding_api import router as embedding_api_router
from openapi_server.models.study_program import StudyProgram

# from openapi_server.models.pet import Pet
from typing import ClassVar, Dict, List, Tuple  # noqa: F401
from pydantic import Field, StrictInt, StrictStr, field_validator
from typing import Any, List, Optional
from typing_extensions import Annotated
from sqlalchemy.orm import Session
from ..data.db import engine, StudyProgram as SqlStudyProgram, Semester as SqlSemester
from ..data.vector_db import create_collection, embed_text, milvus_client
import json

from datetime import datetime, date
import json


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
            sp = SqlStudyProgram(
                id=study_program.study_id,
                name=study_program.program_name,
                degree_program_name=study_program.degree_program_name,
                degree_type_name=study_program.degree_type_name,
                semesters=sem,
            )
            session.add_all([sp])
            session.commit()

            for s in sem:
                collection_name = f"_{study_program.study_id}_{s.name}"
                create_collection(collection_name)
                milvus_client.load_collection(collection_name)
                modules = study_program.semesters[s.name]
                for mod in modules:
                    desc = (
                        mod.id
                        + "\n\n"
                        + mod.content
                        + "\n\n"
                        + mod.outcome
                        + "\n\n"
                        + mod.methods
                        + "\n\n"
                        + mod.exam
                        + "\n\nCredits: "
                        + str(mod.credits)
                    )
                    courses = json.dumps(mod.courses.to_dict(), cls=datetime_encoder)
                    data = [
                        {
                            "id": mod.id,
                            "name": mod.title,
                            "description": desc,
                            "description_vec": embed_text(desc),
                            "timeslots": courses,
                        }
                    ]
                    res = milvus_client.insert(
                        collection_name=collection_name, data=data
                    )
                    milvus_client.flush(collection_name=collection_name)
                    print(res)
        return None

    async def delete_study_program(
        self,
        id: Annotated[
            StrictInt,
            Field(description="ID of the study program that should be deleted"),
        ],
    ) -> None:
        search_res = milvus_client.search(
            collection_name="_171017263_additionalProp1",
            data=[embed_text("i like programming")],
            anns_field="description_vec",  # only one anns field can exist
            limit=3,
            output_fields=["name", "timeslots"],
        )
        print(search_res)


router = embedding_api_router
