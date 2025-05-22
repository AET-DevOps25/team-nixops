# coding: utf-8

from typing import ClassVar, Dict, List, Tuple  # noqa: F401

from openapi_server.security_api import get_token_basic_auth, get_token_api_key

class BaseEchoApi:
    subclasses: ClassVar[Tuple] = ()

    def __init_subclass__(cls, **kwargs):
        super().__init_subclass__(**kwargs)
        BaseEchoApi.subclasses = BaseEchoApi.subclasses + (cls,)
    async def echo(
        self,
        body: str,
    ) -> str:
        """Receive the exact message you&#39;ve sent"""
        ...
