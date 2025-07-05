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
        conv_id: Annotated[StrictInt, Field(description="Conversation ID")],
        study_program_id: Annotated[
            StrictInt,
            Field(description="ID of the study program that is being discussed"),
        ],
        semester: Annotated[
            StrictStr, Field(description="semester that is being discussed")
        ],
    ) -> str:
        """Communicate with the ChatBot making use of streamed responses"""
        ...
