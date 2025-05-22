# coding: utf-8

from typing import ClassVar, Dict, List, Tuple  # noqa: F401

from openapi_server.models.user import User
from openapi_server.security_api import get_token_api_key, get_token_main_auth

class BaseUserApi:
    subclasses: ClassVar[Tuple] = ()

    def __init_subclass__(cls, **kwargs):
        super().__init_subclass__(**kwargs)
        BaseUserApi.subclasses = BaseUserApi.subclasses + (cls,)
    async def get_user_by_name(
        self,
        username: str,
        pretty_print: bool,
        with_email: bool,
    ) -> User:
        """Some description of the operation.  You can use &#x60;markdown&#x60; here. """
        ...


    async def update_user(
        self,
        username: str,
        user: User,
        pretty_print: bool,
    ) -> None:
        """This can only be done by the logged in user."""
        ...
