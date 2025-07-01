# coding: utf-8

from typing import ClassVar, Dict, List, Tuple  # noqa: F401

from pydantic import Field, StrictInt, StrictStr
from typing_extensions import Annotated


class BaseGenerationApi:
    subclasses: ClassVar[Tuple] = ()

    def __init_subclass__(cls, **kwargs):
        super().__init_subclass__(**kwargs)
        BaseGenerationApi.subclasses = BaseGenerationApi.subclasses + (cls,)
    async def stream_chat(
        self,
        prompt: Annotated[StrictStr, Field(description="Chat Prompt")],
        id: Annotated[StrictInt, Field(description="Conversation ID")],
    ) -> str:
        """Communicate with the ChatBot making use of streamed responses"""
        ...
