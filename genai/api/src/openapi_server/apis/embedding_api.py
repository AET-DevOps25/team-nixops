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
    BackgroundTasks,
)

from openapi_server.models.extra_models import TokenModel  # noqa: F401
from typing import Any, List
from openapi_server.models.error import Error
from openapi_server.models.study_program import StudyProgram
from openapi_server.models.study_program_selector_item import StudyProgramSelectorItem


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
    background_tasks: BackgroundTasks,
    study_program: StudyProgram = Body(None, description=""),
) -> None:
    """Create a new study program and embed the modules."""
    if not BaseEmbeddingApi.subclasses:
        raise HTTPException(status_code=500, detail="Not implemented")

    background_tasks.add_task(
        BaseEmbeddingApi.subclasses[0]().create_study_program,
        study_program,
    )

    return {"detail": "Embedding started in background"}


@router.get(
    "/embed/studyPrograms",
    responses={
        200: {
            "model": List[StudyProgramSelectorItem],
            "description": "A stream of message tokens",
        },
    },
    tags=["embedding"],
    summary="Get all scraped study programs",
    response_model_by_alias=True,
)
async def fetch_study_programs() -> List[StudyProgramSelectorItem]:
    """Get all scraped study programs and matching semesters"""
    if not BaseEmbeddingApi.subclasses:
        raise HTTPException(status_code=500, detail="Not implemented")
    return await BaseEmbeddingApi.subclasses[0]().fetch_study_programs()
