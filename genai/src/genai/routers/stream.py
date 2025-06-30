from asyncio import sleep
from typing import Annotated
from typing_extensions import TypedDict

from fastapi.responses import StreamingResponse
from fastapi import APIRouter

from langgraph.graph import StateGraph, START
from langgraph.graph.message import add_messages
from langchain_ollama.chat_models import ChatOllama
from langchain_ollama import OllamaEmbeddings
from langgraph.checkpoint.memory import InMemorySaver

from ..config import llm_api_url, llm_api_key, llm_chat_temp, llm_chat_model, llm_embedding_model

router = APIRouter()

class State(TypedDict):
    messages: Annotated[list, add_messages]


graph_builder = StateGraph(State)

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


@router.get("/stream")
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
