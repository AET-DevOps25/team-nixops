# coding: utf-8

from typing import Dict, List  # noqa: F401
import importlib
import pkgutil

from openapi_server.apis.embedding_api_base import BaseEmbeddingApi
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
from pydantic import Field, StrictInt
from typing import Any
from typing_extensions import Annotated
from openapi_server.models.error import Error
from openapi_server.models.study_program import StudyProgram


router = APIRouter()

ns_pkg = openapi_server.impl
for _, name, _ in pkgutil.iter_modules(ns_pkg.__path__, ns_pkg.__name__ + "."):
    importlib.import_module(name)


@router.post(
    "/embed",
    responses={
        200: {"description": "successful operation"},
        400: {"description": "Invalid status value"},
        200: {"model": Error, "description": "Unexpected error"},
    },
    tags=["embedding"],
    summary="Create a new study program and embed the modules.",
    response_model_by_alias=True,
)
async def create_study_program(
    study_program: StudyProgram = Body(None, description=""),
) -> None:
    """Create a new study program and embed the modules."""
    if not BaseEmbeddingApi.subclasses:
        raise HTTPException(status_code=500, detail="Not implemented")
    return await BaseEmbeddingApi.subclasses[0]().create_study_program(study_program)


@router.delete(
    "/embed/{id}",
    responses={
        200: {"description": "Study program deleted"},
        400: {"description": "Invalid study program id value"},
        200: {"model": Error, "description": "Unexpected error"},
    },
    tags=["embedding"],
    summary="Delete a study program",
    response_model_by_alias=True,
)
async def delete_study_program(
    id: Annotated[
        StrictInt, Field(description="ID of the study program that should be deleted")
    ] = Path(..., description="ID of the study program that should be deleted"),
) -> None:
    """Delete a study program"""
    if not BaseEmbeddingApi.subclasses:
        raise HTTPException(status_code=500, detail="Not implemented")
    return await BaseEmbeddingApi.subclasses[0]().delete_study_program(id)
