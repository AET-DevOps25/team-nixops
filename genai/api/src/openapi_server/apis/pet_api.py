# coding: utf-8

from typing import Dict, List  # noqa: F401
import importlib
import pkgutil

from openapi_server.apis.pet_api_base import BasePetApi
import openapi_server.impl

from fastapi import (  # noqa: F401
    APIRouter,
    Body,
    Cookie,
    Depends,
    Form,
    Header,
    HTTPException,
    Path,
    Query,
    Response,
    Security,
    status,
)

from openapi_server.models.extra_models import TokenModel  # noqa: F401
from pydantic import Field, StrictStr, field_validator
from typing import Any, List, Optional
from typing_extensions import Annotated
from openapi_server.models.error import Error
from openapi_server.models.pet import Pet


router = APIRouter()

ns_pkg = openapi_server.impl
for _, name, _ in pkgutil.iter_modules(ns_pkg.__path__, ns_pkg.__name__ + "."):
    importlib.import_module(name)


@router.get(
    "/pet/findByStatus",
    responses={
        200: {"model": List[Pet], "description": "successful operation"},
        400: {"description": "Invalid status value"},
        200: {"model": Error, "description": "Unexpected error"},
    },
    tags=["pet"],
    summary="Finds Pets by status.",
    response_model_by_alias=True,
)
async def find_pets_by_status(
    status: Annotated[Optional[StrictStr], Field(description="Status values that need to be considered for filter")] = Query(available, description="Status values that need to be considered for filter", alias="status"),
) -> List[Pet]:
    """Multiple status values can be provided with comma separated strings."""
    if not BasePetApi.subclasses:
        raise HTTPException(status_code=500, detail="Not implemented")
    return await BasePetApi.subclasses[0]().find_pets_by_status(status)
