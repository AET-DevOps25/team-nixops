from asyncio import sleep
from typing import Annotated
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
from ..db.vector_db import create_collection, embed_text, milvus_client

from langchain.tools.retriever import create_retriever_tool
from langchain_core.callbacks import CallbackManagerForRetrieverRun
from langchain_core.documents import Document
from langchain_core.retrievers import BaseRetriever
from typing import List
from pydantic import BaseModel, Field
from langgraph.graph import StateGraph, START, END
from langgraph.prebuilt import tools_condition

from ..config import env


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
    print("retrieve modules:", query)

    results = milvus_client.search(
        collection_name=f"_{str(state['study_program_id'])}_{state['semester']}",
        data=[embed_text(query)],
        anns_field="description_vec",  # only one anns field can exist
        limit=3,
        output_fields=["name", "id", "code", "description", "courses"],
    )
    docs = [
        Document(
            "Name:"
            + doc["entity"]["name"]
            + "\n\n"
            + "Id:"
            + str(doc["entity"]["id"])
            + "\n\n"
            + "Code:"
            + doc["entity"]["code"]
            + "\n\n"
            + "Description: "
            + doc["entity"]["description"]
        )
        for doc in results[0]
    ]
    return docs


router = APIRouter()

chat_llm = ChatOllama(
    model=env.llm_chat_model,
    temperature=env.llm_chat_temp,
    base_url=env.llm_api_url,
    tags=["chatting", "chat"],
    client_kwargs={"headers": {"Authorization": f"Bearer {env.llm_api_key}"}},
)

reasoning_llm = ChatOllama(
    model=env.llm_chat_model,
    temperature=0,
    base_url=env.llm_api_url,
    tags=["reasoning", "system"],
    client_kwargs={"headers": {"Authorization": f"Bearer {env.llm_api_key}"}},
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

    print("call model")

    response = reasoning_llm.bind_tools([retrieve_modules, generate_schedule]).invoke(
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

    print("grade documents")

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

    print("rewrite question")

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

    print("generate answer")

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


@tool(parse_docstring=True)
def generate_schedule(
    module_codes: List[str], state: Annotated[State, InjectedState]
) -> List[Document]:
    """
    Generate a weekly schedule from a list of module codes

    Args:
        module_codes: A list of module codes (e.g., "["IN0001", "MA15", "CIT000323"]")
    """

    print("generate_schedule:", module_codes)

    mock_schedule = [
        Document("Monday 9AM-11AM: Machine Learning\nRoom: 101\nLecturer: Prof. X"),
        Document("Wednesday 2PM-4PM: Data Mining\nRoom: 203\nLecturer: Dr. Y"),
        Document("Friday 10AM-12PM: AI Ethics\nRoom: 305\nLecturer: Dr. Z"),
    ]
    return mock_schedule


checkpointer = InMemorySaver()
workflow = StateGraph(State)
workflow.add_node(generate_query_or_respond)

tool_node = ToolNode([retrieve_modules, generate_schedule])
workflow.add_node("tools", tool_node)

workflow.add_edge(START, "generate_query_or_respond")

# Decide whether to retrieve
workflow.add_conditional_edges(
    "generate_query_or_respond",
    tools_condition,
    {
        "tools": "tools",
        END: "generate_answer",
    },
)


def route_tool_output(state: State) -> Literal["grade_documents", "generate_answer"]:
    tool_calls = getattr(state["messages"][-1], "tool_calls", None)
    if not tool_calls:
        return "generate_answer"

    # Tool name called by the LLM
    tool_name = tool_calls[0]["name"]
    if tool_name == "retrieve_modules":
        return "grade_documents"
    else:
        return "generate_answer"


workflow.add_conditional_edges(
    "tools",
    route_tool_output,
    {
        "grade_documents": "grade_documents",
        "generate_answer": "generate_answer",
    },
)

workflow.add_node("grade_documents", grade_documents)

workflow.add_conditional_edges(
    "grade_documents",
    grade_documents,
    {
        "generate_answer": "generate_answer",
        "rewrite_question": "rewrite_question",
    },
)

workflow.add_node(rewrite_question)
workflow.add_node(generate_answer)
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
