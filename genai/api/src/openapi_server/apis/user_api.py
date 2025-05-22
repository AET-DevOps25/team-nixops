# coding: utf-8

from typing import Dict, List  # noqa: F401
import importlib
import pkgutil

from openapi_server.apis.user_api_base import BaseUserApi
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
from openapi_server.models.user import User
from openapi_server.security_api import get_token_api_key, get_token_main_auth

router = APIRouter()

ns_pkg = openapi_server.impl
for _, name, _ in pkgutil.iter_modules(ns_pkg.__path__, ns_pkg.__name__ + "."):
    importlib.import_module(name)


@router.get(
    "/users/{username}",
    responses={
        200: {"model": User, "description": "Success"},
        403: {"description": "Forbidden"},
        404: {"description": "User not found"},
    },
    tags=["User"],
    summary="Get user by user name",
    response_model_by_alias=True,
)
async def get_user_by_name(
    username: str = Path(..., description="The name that needs to be fetched"),
    pretty_print: bool = Query(None, description="Pretty print response", alias="pretty_print"),
    with_email: bool = Query(None, description="Filter users without email", alias="with_email"),
    token_api_key: TokenModel = Security(
        get_token_api_key
    ),
    token_main_auth: TokenModel = Security(
        get_token_main_auth, scopes=["read:users"]
    ),
) -> User:
    """Some description of the operation.  You can use &#x60;markdown&#x60; here. """
    if not BaseUserApi.subclasses:
        raise HTTPException(status_code=500, detail="Not implemented")
    return await BaseUserApi.subclasses[0]().get_user_by_name(username, pretty_print, with_email)


@router.put(
    "/users/{username}",
    responses={
        200: {"description": "OK"},
        400: {"description": "Invalid user supplied"},
        404: {"description": "User not found"},
    },
    tags=["User"],
    summary="Updated user",
    response_model_by_alias=True,
)
async def update_user(
    username: str = Path(..., description="The name that needs to be updated"),
    user: User = Body(None, description="Updated user object"),
    pretty_print: bool = Query(None, description="Pretty print response", alias="pretty_print"),
    token_main_auth: TokenModel = Security(
        get_token_main_auth, scopes=["write:users"]
    ),
) -> None:
    """This can only be done by the logged in user."""
    if not BaseUserApi.subclasses:
        raise HTTPException(status_code=500, detail="Not implemented")
    return await BaseUserApi.subclasses[0]().update_user(username, user, pretty_print)
