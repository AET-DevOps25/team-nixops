import asyncio
from typing import List

from openapi_server.apis.embedding_api_base import BaseEmbeddingApi
from openapi_server.apis.embedding_api import router as embedding_api_router
from openapi_server.models.study_program import StudyProgram
from openapi_server.models.study_program_selector_item import StudyProgramSelectorItem

from sqlalchemy.orm import Session
from sqlalchemy import select

from genai.db.relational_db import (
    engine,
    StudyProgram as SqlStudyProgram,
)
from genai.db.vector_db import milvus_client, embed_text, create_collection
from genai.config import telemetry
from genai.embedding import embed_study_program

# NOTE: https://milvus.io/docs/use-async-milvus-client-with-asyncio.md#Create-index


class CustomEmbeddingApi(BaseEmbeddingApi):
    def __init_subclass__(cls, **kwargs):
        super().__init_subclass__(**kwargs)
        BaseEmbeddingApi.subclasses = BaseEmbeddingApi.subclasses + (cls,)

    async def create_study_program(
        self,
        study_program: StudyProgram,
    ) -> None:
        loop = asyncio.get_running_loop()
        loop.run_in_executor(None, self.embed_study_program, study_program)

        return {"detail": "Embedding started in background"}

    def embed_study_program(
        self,
        study_program: StudyProgram,
    ) -> None:
        with Session(engine) as sql_session:
            embed_study_program(
                milvus_client, embed_text, create_collection, sql_session, study_program
            )

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
