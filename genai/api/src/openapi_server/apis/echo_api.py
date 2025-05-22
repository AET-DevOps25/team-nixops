# coding: utf-8

from typing import Dict, List  # noqa: F401
import importlib
import pkgutil

from openapi_server.apis.echo_api_base import BaseEchoApi
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
from openapi_server.security_api import get_token_basic_auth, get_token_api_key

router = APIRouter()

ns_pkg = openapi_server.impl
for _, name, _ in pkgutil.iter_modules(ns_pkg.__path__, ns_pkg.__name__ + "."):
    importlib.import_module(name)


@router.post(
    "/echo",
    responses={
        200: {"model": str, "description": "OK"},
    },
    tags=["Echo"],
    summary="Echo test",
    response_model_by_alias=True,
)
async def echo(
    body: str = Body(None, description="Echo payload"),
    token_basic_auth: TokenModel = Security(
        get_token_basic_auth
    ),
    token_api_key: TokenModel = Security(
        get_token_api_key
    ),
) -> str:
    """Receive the exact message you&#39;ve sent"""
    if not BaseEchoApi.subclasses:
        raise HTTPException(status_code=500, detail="Not implemented")
    return await BaseEchoApi.subclasses[0]().echo(body)
