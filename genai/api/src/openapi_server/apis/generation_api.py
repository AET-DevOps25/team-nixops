# coding: utf-8

from typing import Dict, List  # noqa: F401
import importlib
import pkgutil

from openapi_server.apis.generation_api_base import BaseGenerationApi
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
from pydantic import Field, StrictInt, StrictStr
from typing_extensions import Annotated


router = APIRouter()

ns_pkg = openapi_server.impl
for _, name, _ in pkgutil.iter_modules(ns_pkg.__path__, ns_pkg.__name__ + "."):
    importlib.import_module(name)


@router.get(
    "/chat",
    responses={
        200: {"model": str, "description": "A stream of message tokens"},
    },
    tags=["generation"],
    summary="Communicate with the ChatBot",
    response_model_by_alias=True,
)
async def stream_chat(
    prompt: Annotated[StrictStr, Field(description="Chat Prompt")] = Query(
        "Write a poem", description="Chat Prompt", alias="prompt"
    ),
    conv_id: Annotated[StrictInt, Field(description="Conversation ID")] = Query(
        420, description="Conversation ID", alias="convId"
    ),
    study_program_id: Annotated[
        StrictInt, Field(description="ID of the study program that is being discussed")
    ] = Query(
        None,
        description="ID of the study program that is being discussed",
        alias="studyProgramId",
    ),
    semester: Annotated[
        StrictStr, Field(description="semester that is being discussed")
    ] = Query(None, description="semester that is being discussed", alias="semester"),
) -> str:
    """Communicate with the ChatBot making use of streamed responses"""
    if not BaseGenerationApi.subclasses:
        raise HTTPException(status_code=500, detail="Not implemented")
    return await BaseGenerationApi.subclasses[0]().stream_chat(
        prompt, conv_id, study_program_id, semester
    )
