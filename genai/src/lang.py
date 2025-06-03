from typing import Annotated
from dotenv import load_dotenv
import os

from typing_extensions import TypedDict

from langgraph.graph import StateGraph, START
from langgraph.graph.message import add_messages
from langchain_openai import ChatOpenAI
from langchain_ollama.chat_models import ChatOllama

load_dotenv()


class State(TypedDict):
    messages: Annotated[list, add_messages]


graph_builder = StateGraph(State)

llm_api_key = os.getenv("LLM_API_KEY")

# Ollama proxy is unauthorized
# llm = ChatOllama(
#     model="llama3.3:latest",
#     temperature=0.5,
#     base_url="https://gpu.aet.cit.tum.de/ollama",
#     client_kwargs={
#         "headers": {
#             "Authorization": f"Bearer {llm_api_key}"
#         }
#     }
# )

# llm = ChatOllama(
#     model="qwen2.5:0.5b",
#     temperature=0.5,
# )

llm = ChatOpenAI(
    model_name="llama3.3:latest",  # Or any other model available on this Open WebUI instance.
    temperature=0.5,
    openai_api_key=llm_api_key,  # Replace with your Open WebUI API key.
    openai_api_base="https://gpu.aet.cit.tum.de/api",  # The base URL of your Open WebUI instance.
)


def chatbot(state: State):
    return {"messages": [llm.invoke(state["messages"])]}


# The first argument is the unique node name
# The second argument is the function or object that will be called whenever
# the node is used.
graph_builder.add_node("chatbot", chatbot)
graph_builder.add_edge(START, "chatbot")
graph = graph_builder.compile()


def stream_graph_updates(user_input: str):
    for message_chunk, metadata in graph.stream(
        {"messages": [("user", user_input)]},
        stream_mode="messages",
    ):
        if message_chunk.content:
            print(message_chunk.content, end="|", flush=True)


while True:
    try:
        user_input = input("User: ")
        if user_input.lower() in ["quit", "exit", "q"]:
            print("Goodbye!")
            break
        stream_graph_updates(user_input)
    except:
        # fallback if input() is not available
        user_input = "What do you know about LangGraph?"
        print("User: " + user_input)
        stream_graph_updates(user_input)
        break
