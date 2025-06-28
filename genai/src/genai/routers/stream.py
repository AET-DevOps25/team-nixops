from typing import Annotated
from decouple import config
from typing_extensions import TypedDict

from fastapi.responses import StreamingResponse
from fastapi import APIRouter

from langgraph.graph import StateGraph, START
from langgraph.graph.message import add_messages
from langchain_ollama.chat_models import ChatOllama
from langgraph.checkpoint.memory import InMemorySaver

router = APIRouter()

class State(TypedDict):
    messages: Annotated[list, add_messages]


graph_builder = StateGraph(State)

llm_api_url = config("LLM_API_URL", default="https://gpu.aet.cit.tum.de/ollama")
llm_api_key = config("LLM_API_KEY")
llm_chat_model = config("LLM_CHAT_MODEL", default="llama3.3:latest")
llm_chat_temp = config("LLM_CHAT_TEMP", default=0.5, cast=float)

llm = ChatOllama(
    model=llm_chat_model,
    temperature=llm_chat_temp,
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

    res = StreamingResponse(generate(prompt, id), media_type="text/event-stream")
    return res
