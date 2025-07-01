from openapi_server.apis.embedding_api_base import BaseEmbeddingApi
from openapi_server.apis.embedding_api import router as embedding_api_router
from openapi_server.models.study_program import StudyProgram
# from openapi_server.models.pet import Pet
from typing import ClassVar, Dict, List, Tuple  # noqa: F401
from pydantic import Field, StrictInt, StrictStr, field_validator
from typing import Any, List, Optional
from typing_extensions import Annotated

class CustomEmbeddingApi(BaseEmbeddingApi):
    subclasses: ClassVar[Tuple] = ()

    def __init_subclass__(cls, **kwargs):
        super().__init_subclass__(**kwargs)
        BaseEmbeddingApi.subclasses = BaseEmbeddingApi.subclasses + (cls,)
    async def create_study_program(
        self,
        study_program: StudyProgram,
    ) -> None:
        """Create a new study program and embed the modules."""
        ...


    async def delete_study_program(
        self,
        id: Annotated[StrictInt, Field(description="ID of the study program that should be deleted")],
    ) -> None:
        """Delete a study program"""
        ...


    async def update_study_program(
        self,
        id: Annotated[StrictInt, Field(description="ID of the study program")],
        request_body: List[StrictInt],
    ) -> None:
        """Add new modules to a study program."""
        ...

router = embedding_api_router
