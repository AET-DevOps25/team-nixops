# coding: utf-8

from typing import ClassVar, Dict, List, Tuple  # noqa: F401

from typing import Any, List
from openapi_server.models.error import Error
from openapi_server.models.study_program import StudyProgram
from openapi_server.models.study_program_selector_item import StudyProgramSelectorItem


class BaseEmbeddingApi:
    subclasses: ClassVar[Tuple] = ()

    def __init_subclass__(cls, **kwargs):
        super().__init_subclass__(**kwargs)
        BaseEmbeddingApi.subclasses = BaseEmbeddingApi.subclasses + (cls,)

    def create_study_program(
        self,
        study_program: StudyProgram,
    ) -> None:
        """Create a new study program and embed the modules."""
        ...

    async def fetch_study_programs(
        self,
    ) -> List[StudyProgramSelectorItem]:
        """Get all scraped study programs and matching semesters"""
        ...
