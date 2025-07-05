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
from langgraph.graph import END, START, StateGraph
from langgraph.prebuilt import InjectedState, ToolNode
import os
from ..data.vector_db import create_collection, embed_text, milvus_client

from langchain.tools.retriever import create_retriever_tool
from langchain_core.callbacks import CallbackManagerForRetrieverRun
from langchain_core.documents import Document
from langchain_core.retrievers import BaseRetriever
from typing import List
from pydantic import BaseModel, Field
from langgraph.graph import StateGraph, START, END
from langgraph.prebuilt import tools_condition


class State(TypedDict):
    messages: Annotated[list, add_messages]
    study_program_id: int
    semester: str


@tool(parse_docstring=True)
def retrieve_modules(
    query: str, state: Annotated[State, InjectedState]
) -> List[Document]:
    """Search and return information about courses and modules available at Munich's technical university TUM.

    Args:
        query: Search query for the lookup
    """
    results = milvus_client.search(
        collection_name=f"_{str(state['study_program_id'])}_{state['semester']}",
        data=[embed_text(query)],
        anns_field="description_vec",  # only one anns field can exist
        limit=3,
        output_fields=["name", "description", "courses"],
    )
    docs = [
        Document(
            "Name:"
            + doc["entity"]["name"]
            + "\n\n"
            + "Description: "
            + doc["entity"]["description"]
        )
        for doc in results[0]
    ]
    return docs


router = APIRouter()

llm_api_url = config("LLM_API_URL", default="https://gpu.aet.cit.tum.de/ollama")
llm_api_key = config("LLM_API_KEY")
llm_chat_model = config("LLM_CHAT_MODEL", default="llama3.3:latest")
llm_chat_temp = config("LLM_CHAT_TEMP", default=0.5, cast=float)

chat_llm = ChatOllama(
    model=llm_chat_model,
    temperature=llm_chat_temp,
    base_url=llm_api_url,
    tags=["chatting", "chat"],
    client_kwargs={"headers": {"Authorization": f"Bearer {llm_api_key}"}},
)

reasoning_llm = ChatOllama(
    model=llm_chat_model,
    temperature=0,
    base_url=llm_api_url,
    tags=["reasoning", "system"],
    client_kwargs={"headers": {"Authorization": f"Bearer {llm_api_key}"}},
)

TOOL_SELECTION_PROMPT = (
    "You are a highly intelligent assistant. Before you decide to execute any tools, "
    "analyze the user's request: '{}'"
    "If you do not believe executing a tool is necessary, end your statement without further action."
)


def generate_query_or_respond(state: State):
    """Call the model to generate a response based on the current state. Given
    the question, it will decide to retrieve using the retriever tool, or simply respond to the user.
    """
    response = reasoning_llm.bind_tools([retrieve_modules]).invoke(
        TOOL_SELECTION_PROMPT.format(state["messages"])
    )
    return {"messages": [response]}


GRADE_PROMPT = (
    "You are a grader assessing relevance of a retrieved document to a user question. \n "
    "Here is the retrieved document: \n\n {context} \n\n"
    "Here is the user question: {question} \n"
    "If the document contains keyword(s) or semantic meaning related to the user question, grade it as relevant. \n"
    "Give a binary score 'yes' or 'no' score to indicate whether the document is relevant to the question."
)


class GradeDocuments(BaseModel):
    """Grade documents using a binary score for relevance check."""

    binary_score: str = Field(
        description="Relevance score: 'yes' if relevant, or 'no' if not relevant"
    )


def grade_documents(
    state: State,
) -> Literal["generate_answer", "rewrite_question"]:
    """Determine whether the retrieved documents are relevant to the question."""
    messages = state["messages"]
    context = messages[-1].content
    question = messages[-2].content

    for i in range(len(messages) - 2, -1, -1):
        is_human_msg = not hasattr(messages[i], "tool_calls")
        if is_human_msg:
            question = messages[i].content
            # print("------------------------")
            # print(question)
            # print("------------------------")
            break

    prompt = GRADE_PROMPT.format(question=question, context=context)
    print(prompt)
    response = (
        reasoning_llm.with_structured_output(  # TODO: use separate model with temp zero
            GradeDocuments
        ).invoke([{"role": "user", "content": prompt}])
    )
    score = response.binary_score

    if score == "yes":
        return "generate_answer"
    else:
        return "rewrite_question"


REWRITE_PROMPT = (
    "Look at the input and try to reason about the underlying semantic intent / meaning.\n"
    "Here is the initial question:"
    "\n ------- \n"
    "{question}"
    "\n ------- \n"
    "Formulate an improved question:"
)


def rewrite_question(state: State):
    """Rewrite the original user question."""
    messages = state["messages"]
    question = messages[-2].content
    for i in range(len(messages) - 2, -1, -1):
        is_human_msg = not hasattr(messages[i], "tool_calls")
        if is_human_msg:
            question = messages[i].content
            # print("------------------------")
            # print(question)
            # print("------------------------")
            break

    prompt = REWRITE_PROMPT.format(question=question)
    response = reasoning_llm.invoke([{"role": "user", "content": prompt}])
    return {"messages": [{"role": "user", "content": response.content}]}


GENERATE_PROMPT = (
    "You are an assistant for question-answering tasks. "
    "Use the following pieces of retrieved context to answer the question. "
    "If you don't know the answer, just say that you don't know. "
    "Use three sentences maximum and keep the answer concise.\n"
    "Question: {question} \n"
    "Context: {context} \n"
    "Previous messages: {messages}"
)


def generate_answer(state: State):
    """Generate an answer."""
    messages = state["messages"]
    context = messages[-1].content
    question = messages[-2].content

    for i in range(len(messages) - 2, -1, -1):
        is_human_msg = not hasattr(messages[i], "tool_calls")
        if is_human_msg:
            question = messages[i].content
            # print("-----------generate_answer-------------")
            # print(messages)
            # print("QUESTION")
            # print(question)
            # print("-----------generate_answer-------------")
            break
    prompt = GENERATE_PROMPT.format(
        question=question, context=context, messages=messages
    )
    response = chat_llm.invoke([{"role": "user", "content": prompt}])
    return {"messages": [response]}


checkpointer = InMemorySaver()
workflow = StateGraph(State)
workflow.add_node(generate_query_or_respond)
workflow.add_node("retrieve", ToolNode([retrieve_modules]))
workflow.add_node(rewrite_question)
workflow.add_node(generate_answer)

workflow.add_edge(START, "generate_query_or_respond")

# Decide whether to retrieve
workflow.add_conditional_edges(
    "generate_query_or_respond",
    # Assess LLM decision (call `retriever_tool` tool or respond to the user)
    tools_condition,
    {
        # Translate the condition outputs to nodes in our graph
        "tools": "retrieve",
        END: "generate_answer",
    },
)

# Edges taken after the `action` node is called.
workflow.add_conditional_edges(
    "retrieve",
    # Assess agent decision
    grade_documents,
)
workflow.add_edge("generate_answer", END)
workflow.add_edge("rewrite_question", "generate_query_or_respond")
graph = workflow.compile(checkpointer=checkpointer)


@router.get("/chat")
async def stream_response(prompt: str, convId: str, studyProgramId: int, semester: str):
    async def generate(
        user_input: str, user_id: str, study_program: int, semester: str
    ):
        config = {"configurable": {"thread_id": user_id}}
        async for msg, metadata in graph.astream(
            {
                "messages": [("user", user_input)],
                "study_program_id": study_program,
                "semester": semester,
            },
            config=config,
            stream_mode="messages",
        ):
            if msg.content and metadata["langgraph_node"] == "generate_answer":
                yield f"data: {msg.content}\n\n"
                await sleep(0.04)  # simply for chat output smoothing

    res = StreamingResponse(
        generate(prompt, convId, studyProgramId, semester),
        media_type="text/event-stream",
    )
    return res
