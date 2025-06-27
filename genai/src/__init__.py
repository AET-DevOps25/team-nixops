"""
Module Name: GenAI server
Description:
    A basic FastAPI API that returns a "Hello, World!" message for the root endpoint.
    This module demonstrates a minimal FastAPI setup and endpoint implementation.
"""

import uuid
from fastapi import FastAPI
from fastapi.params import Cookie
from fastapi.responses import StreamingResponse
import yaml
from decouple import config

import uvicorn
import logging
from typing import Annotated
from asyncio import sleep

from typing_extensions import TypedDict

from langgraph.graph import StateGraph, START
from langgraph.graph.message import add_messages
from langchain_ollama.chat_models import ChatOllama
from langchain_ollama import OllamaEmbeddings
from langgraph.checkpoint.memory import InMemorySaver
from langchain_core.documents import Document
from fastapi.middleware.cors import CORSMiddleware

logger = logging.getLogger("uvicorn.error")

app = FastAPI()

origins = [
    "http://localhost",
    "http://localhost:8000",
    "http://localhost:3000",
]

app.add_middleware(
    CORSMiddleware,
    allow_origins=origins,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


class State(TypedDict):
    messages: Annotated[list, add_messages]


graph_builder = StateGraph(State)

llm_api_url = config("LLM_API_URL", default="https://gpu.aet.cit.tum.de/ollama")
llm_api_key = config("LLM_API_KEY")
llm_chat_model = config("LLM_CHAT_MODEL", default="llama3.3:latest")
llm_embedding_model = config("LLM_EMBEDDING_MODEL", default="llama3.3:latest")
llm_chat_temp = config("LLM_CHAT_TEMP", default=0.5, cast=float)

llm = ChatOllama(
    model=llm_chat_model,
    temperature=llm_chat_temp,
    base_url=llm_api_url,
    client_kwargs={"headers": {"Authorization": f"Bearer {llm_api_key}"}},
)
embeddings = OllamaEmbeddings(
    model=llm_embedding_model,
    base_url=llm_api_url,
    client_kwargs={"headers": {"Authorization": f"Bearer {llm_api_key}"}},
)


def chatbot(state: State):
    return {"messages": [llm.invoke(state["messages"])]}


checkpointer = InMemorySaver()
graph_builder.add_node("chatbot", chatbot)
graph_builder.add_edge(START, "chatbot")
graph = graph_builder.compile(checkpointer=checkpointer)


@app.get("/stream")
async def stream_response(prompt: str, id: str):
    async def generate(user_input: str, user_id: str):
        config = {"configurable": {"thread_id": user_id}}
        async for message_chunk, _ in graph.astream(
            {"messages": [("user", user_input)]},
            config=config,
            stream_mode="messages",
        ):
            yield f"data: {message_chunk.content}\n\n"
            await sleep(0.04)  # simply for chat output smoothing

    res = StreamingResponse(generate(prompt, id), media_type="text/event-stream")
    return res


def custom_openapi():
    with open("openapi.yml", "r") as openapi:
        return yaml.safe_load(openapi)


app.openapi = custom_openapi

if __name__ == "__main__":
    uvicorn.run(app, log_level="trace")
