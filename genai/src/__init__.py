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
from langchain_core.documents import Document


from langchain_openai import OpenAIEmbeddings
from pymilvus import Collection, MilvusException, connections, db, utility
from langchain_milvus import BM25BuiltInFunction, Milvus

logger = logging.getLogger("uvicorn.error")

app = FastAPI()

load_dotenv()

def create_milvus_db():
    # Check if the database exists
    db_name = "milvus_demo"
    try:
        existing_databases = db.list_database()
        if db_name not in existing_databases:
            print(f"Database '{db_name}' does not exist.")
            database = db.create_database(db_name)
            print(f"Database '{db_name}' created successfully.")
        else:
            print(f"Database '{db_name}' already exists.")
    except MilvusException as e:
        print(f"An error occurred: {e}")


class State(TypedDict):
    messages: Annotated[list, add_messages]


graph_builder = StateGraph(State)

llm_api_key = os.getenv("LLM_API_KEY")

llm = ChatOpenAI(
    model_name="llama3.3:latest",
    temperature=0.5,
    openai_api_key=llm_api_key,
    openai_api_base="https://gpu.aet.cit.tum.de/api",
)
embeddings = OpenAIEmbeddings(
    model="llama3.3:latest",
    openai_api_key=llm_api_key,
    openai_api_base="https://gpu.aet.cit.tum.de/api",
)

conn = connections.connect(host="127.0.0.1", port=19530)
URI = "http://localhost:19530"
create_milvus_db()

vector_store = Milvus(
    embedding_function=embeddings,
    connection_args={"uri": URI, "token": "root:Milvus", "db_name": "milvus_demo"},
    index_params={"index_type": "FLAT", "metric_type": "L2"},
    consistency_level="Strong",
    drop_old=False,  # set to True if seeking to drop the collection with that name if it exists
)

document_1 = Document(
    page_content="I had chocolate chip pancakes and scrambled eggs for breakfast this morning.",
    metadata={"source": "tweet"},
)

document_2 = Document(
    page_content="The weather forecast for tomorrow is cloudy and overcast, with a high of 62 degrees.",
    metadata={"source": "news"},
)

document_3 = Document(
    page_content="Building an exciting new project with LangChain - come check it out!",
    metadata={"source": "tweet"},
)

documents = [
    document_1,
    document_2,
    document_3,
]
uuids = [str(uuid.uuid4()) for _ in range(len(documents))]

# fails due to embedding endpoint unauthorized
# vector_store.add_documents(documents=documents, ids=uuids)


def chatbot(state: State):
    return {"messages": [llm.invoke(state["messages"])]}


checkpointer = InMemorySaver()
graph_builder.add_node("chatbot", chatbot)
graph_builder.add_edge(START, "chatbot")
graph = graph_builder.compile(checkpointer=checkpointer)


@app.get("/stream")
async def stream_response(prompt: str, uid: Annotated[str | None, Cookie()] = None):
    async def generate(user_input: str, user_id: str):
        config = {"configurable": {"thread_id": user_id}}
        async for message_chunk, _ in graph.astream(
            {"messages": [("user", user_input)]},
            config=config,
            stream_mode="messages",
        ):
            yield message_chunk.content or ""
            await sleep(0.04)  # simply for chat output smoothing

    if uid is None:
        uid = str(uuid.uuid4())
    res = StreamingResponse(generate(prompt, uid), media_type="text/event-stream")
    res.set_cookie(key="uid", value=uid)
    return res


def custom_openapi():
    with open("openapi.yml", "r") as openapi:
        return yaml.safe_load(openapi)


# app.openapi = custom_openapi

if __name__ == "__main__":
    uvicorn.run(app, log_level="trace")
