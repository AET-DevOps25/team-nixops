from asyncio import sleep
from typing import Annotated
from decouple import config
from typing_extensions import TypedDict

from fastapi.responses import StreamingResponse
from fastapi import APIRouter

from langgraph.graph import StateGraph, START
from langgraph.graph.message import add_messages
from langchain_ollama.chat_models import ChatOllama
from langgraph.checkpoint.memory import InMemorySaver
from typing import Literal
from langchain_core.messages import HumanMessage
from langchain_openai.chat_models import ChatOpenAI
from langchain.text_splitter import RecursiveCharacterTextSplitter
from langchain_openai.embeddings import OpenAIEmbeddings
from langchain_core.tools import tool
from langgraph.checkpoint.memory import MemorySaver
from langgraph.graph import END, START, StateGraph, MessagesState
from langgraph.prebuilt import ToolNode
import os
from ..data.vector_db import create_collection, embed_text, milvus_client

router = APIRouter()


class State(TypedDict):
    messages: Annotated[list, add_messages]


graph_builder = StateGraph(State)

llm_api_url = config("LLM_API_URL", default="https://gpu.aet.cit.tum.de/ollama")
llm_api_key = config("LLM_API_KEY")
llm_chat_model = config("LLM_CHAT_MODEL", default="llama3.3:latest")
llm_chat_temp = config("LLM_CHAT_TEMP", default=0.5, cast=float)


@tool
def retrieve_context(query: str):
    """Search for relevant lectures."""
    print(query)
    results = milvus_client.search(
        collection_name="_171017263_additionalProp1",
        data=[embed_text(query)],
        anns_field="description_vec",  # only one anns field can exist
        limit=3,
        output_fields=["name", "courses"],
    )
    docs = [doc["entity"]["name"] for doc in results[0]]
    # print(docs)
    return "\n".join(docs)


tools = [retrieve_context]
tool_node = ToolNode(tools)

llm = ChatOllama(
    model=llm_chat_model,
    temperature=llm_chat_temp,
    base_url=llm_api_url,
    client_kwargs={"headers": {"Authorization": f"Bearer {llm_api_key}"}},
).bind_tools(tools)


def chatbot(state: State):
    return {"messages": [llm.invoke(state["messages"])]}


from langgraph.graph import END, START, StateGraph, MessagesState


def should_continue(state: MessagesState) -> Literal["tools", END]:
    messages = state["messages"]
    last_message = messages[-1]
    print(last_message)
    # If the LLM makes a tool call, go to the "tools" node
    if last_message.tool_calls:
        return "tools"
    # Otherwise, finish the graph_builder
    return END


checkpointer = InMemorySaver()
graph_builder.add_node("agent", chatbot)
graph_builder.add_node("tools", tool_node)
graph_builder.add_edge(START, "agent")  # Initial entry
graph_builder.add_conditional_edges(
    "agent", should_continue
)  # Decision after the "agent" node
graph_builder.add_edge("tools", "agent")  # Cycle between tools and agent
graph = graph_builder.compile(checkpointer=checkpointer)


@router.get("/chat")
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
