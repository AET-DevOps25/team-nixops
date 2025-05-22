from openapi_server.apis.echo_api_base import BaseEchoApi
import openapi_server.impl
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
from openapi_server.security_api import get_token_basic_auth, get_token_api_key
from fastapi import FastAPI
from openapi_server.models.extra_models import TokenModel  # noqa: F401
from openapi_server.models.user import User
from openapi_server.security_api import get_token_api_key, get_token_main_auth

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
from fastapi import FastAPI

from openapi_server.apis.echo_api import router as EchoApiRouter
from openapi_server.apis.user_api import router as UserApiRouter

router = APIRouter()
app = FastAPI()
app.include_router(router)

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
