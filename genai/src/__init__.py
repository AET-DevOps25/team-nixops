"""
Module Name: GenAI server
Description:
    A basic FastAPI API that returns a "Hello, World!" message for the root endpoint.
    This module demonstrates a minimal FastAPI setup and endpoint implementation.
"""

from fastapi import FastAPI, Request
from fastapi.responses import StreamingResponse
import yaml

from openapi_server.models.pet import Pet
from typing import List
import uvicorn
import logging
from typing import Annotated
from dotenv import load_dotenv
from asyncio import sleep
import os

from typing_extensions import TypedDict

from langgraph.graph import StateGraph, START
from langgraph.graph.message import add_messages
from langchain_openai import ChatOpenAI
from langchain_ollama.chat_models import ChatOllama
from langgraph.checkpoint.memory import InMemorySaver

logger = logging.getLogger("uvicorn.error")

app = FastAPI()

load_dotenv()


class State(TypedDict):
    messages: Annotated[list, add_messages]


graph_builder = StateGraph(State)

llm_api_key = os.getenv("LLM_API_KEY")

llm = ChatOpenAI(
    model_name="llama3.3:latest",  # Or any other model available on this Open WebUI instance.
    temperature=0.5,
    openai_api_key=llm_api_key,  # Replace with your Open WebUI API key.
    openai_api_base="https://gpu.aet.cit.tum.de/api",  # The base URL of your Open WebUI instance.
)


def chatbot(state: State):
    return {"messages": [llm.invoke(state["messages"])]}


checkpointer = InMemorySaver()
graph_builder.add_node("chatbot", chatbot)
graph_builder.add_edge(START, "chatbot")
graph = graph_builder.compile(checkpointer=checkpointer)


@app.get("/stream")
async def stream_response(prompt: str, user_id: str):
    async def generate(user_input: str, user_id: str):
        config = {"configurable": {"thread_id": user_id}}
        async for message_chunk, _ in graph.astream(
            {"messages": [("user", user_input)]},
            config=config,
            stream_mode="messages",
        ):
            yield message_chunk.content or ""
            await sleep(0.04)  # simply for chat output smoothing

    return StreamingResponse(generate(prompt, user_id), media_type="text/event-stream")


@app.get(
    "/pet/findByStatus",
    responses={
        200: {"model": List[Pet], "description": "successful operation"},
        400: {"description": "Invalid status value"},
    },
    tags=["pet"],
    summary="Finds Pets by status.",
    response_model_by_alias=True,
)
async def find_pets_by_status() -> List[Pet]:
    """Multiple status values can be provided with comma separated strings."""
    pet_dict = {
        "id": 1,
        "name": "Frany",
        "category": {"id": 10, "name": "Cats"},
        "photoUrls": ["http://example.com/photo1.jpg", "http://example.com/photo2.jpg"],
        "tags": [{"id": 101, "name": "cute"}, {"id": 102, "name": "small"}],
        "status": "available",
    }
    return [Pet.from_dict(pet_dict)]


def custom_openapi():
    with open("openapi.yml", "r") as openapi:
        return yaml.safe_load(openapi)


app.openapi = custom_openapi

if __name__ == "__main__":
    uvicorn.run(app, log_level="trace")
