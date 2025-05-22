# coding: utf-8

from typing import ClassVar, Dict, List, Tuple  # noqa: F401

from pydantic import Field, StrictStr, field_validator
from typing import Any, List, Optional
from typing_extensions import Annotated
from openapi_server.models.error import Error
from openapi_server.models.pet import Pet


class BasePetApi:
    subclasses: ClassVar[Tuple] = ()

    def __init_subclass__(cls, **kwargs):
        super().__init_subclass__(**kwargs)
        BasePetApi.subclasses = BasePetApi.subclasses + (cls,)
    async def find_pets_by_status(
        self,
        status: Annotated[Optional[StrictStr], Field(description="Status values that need to be considered for filter")],
    ) -> List[Pet]:
        """Multiple status values can be provided with comma separated strings."""
        ...
