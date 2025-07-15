from asyncio import sleep
import asyncio
from typing import Annotated
from typing_extensions import TypedDict
from typing import List
from typing import Literal

from fastapi.responses import StreamingResponse
from fastapi import APIRouter

from langgraph.graph.message import add_messages
from langgraph.checkpoint.memory import InMemorySaver
from langchain_core.tools import tool
from langgraph.graph import END, START, StateGraph
from langgraph.prebuilt import InjectedState, ToolNode
from langchain_core.documents import Document
from langchain_core.runnables.config import RunnableConfig
from pydantic import BaseModel, Field
from langgraph.prebuilt import tools_condition

from langgraph.checkpoint.redis import AsyncRedisSaver
from redis.asyncio import Redis as AsyncRedis

from ..db.vector_db import embed_text, milvus_client
from ..db.relational_db import get_study_program_name_by_id
from ..config import env
from ..clients import chat_client, reasoning_client


class TTLRedisSaver(AsyncRedisSaver):
    def __init__(self, *args, ttl_seconds=600, **kwargs):
        super().__init__(*args, **kwargs)
        self.ttl_seconds = ttl_seconds

    async def awrite(self, config, state):
        result = await super().awrite(config, state)
        key = self._get_key(config)
        await self.client.expire(key, self.ttl_seconds)
        return result


class State(TypedDict):
    messages: Annotated[list, add_messages]
    study_program_id: int
    study_program_name: str
    semester: str
    user_id: str


@tool(parse_docstring=True)
def retrieve_modules(
    keywords: List[str], state: Annotated[State, InjectedState]
) -> List[Document]:
    """Search and return information about courses and modules available at Munich's technical university TUM.

    Args:
        keywords: A list of keywords to search for in the course and module descriptions (e.g., "["programming","not languages", "IN0001", "CIT000323"]")
    """

    print("retrieve modules:", keywords)

    # Combine keywords into a single search string
    combined_query = " ".join(keywords)

    collection_name = f"_{str(state['study_program_id'])}_{state['semester']}"

    print("collection_name:", collection_name)

    results = milvus_client.search(
        collection_name=collection_name,
        data=[embed_text(combined_query)],
        anns_field="description_vec",  # only one anns field can exist
        limit=3,
        output_fields=["name", "id", "code", "description", "courses"],
    )

    for doc in results[0]:
        print("found:", doc["entity"]["code"], doc["entity"]["name"])

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

TOOL_SELECTION_PROMPT = (
    "You are a university assistant that helps students helping students in the study program '{study_program_name}' manage their semester schedules, choose suitable modules, "
    "and understand module details. Carefully analyze the user's request: '{request}'. "
    "Use tools whenever they help you retrieve information, manage the schedule, or improve your response. "
    "If a tool is not needed to fulfill the request effectively, respond directly instead."
)


def generate_query_or_respond(state: State):
    """Call the model to generate a response based on the current state. Given
    the question, it will decide to retrieve using the retriever tool, or simply respond to the user.
    """

    schedule_id = state["user_id"]
    semester = state["semester"]

    print("call model:", schedule_id, semester)

    response = reasoning_client.bind_tools(
        [
            retrieve_modules,
            generate_schedule,
            add_module_to_schedule,
            remove_module_from_schedule,
            get_schedule,
        ]
    ).invoke(
        TOOL_SELECTION_PROMPT.format(
            request=state["messages"], study_program_name=state["study_program_name"]
        )
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
    response = reasoning_client.with_structured_output(  # TODO: use separate model with temp zero
        GradeDocuments
    ).invoke(
        [{"role": "user", "content": prompt}]
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
    response = reasoning_client.invoke([{"role": "user", "content": prompt}])
    return {"messages": [{"role": "user", "content": response.content}]}


GENERATE_PROMPT = (
    "You are an assistant for question-answering tasks. "
    "Use the following pieces of retrieved context to answer the question. "
    "If you don't know the answer, just say that you don't know. "
    "Use three sentences maximum and keep the answer concise."
    "If you just edited the schedule, do not recap module contents unless asked to do so\n"
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
    response = chat_client.invoke([{"role": "user", "content": prompt}])
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


@tool(parse_docstring=True)
def add_module_to_schedule(
    module_code: str,
    state: Annotated[State, InjectedState],
    config: RunnableConfig = None,
) -> List[Document]:
    """
    Add a module to the schedule

    Args:
        module_code: The code of  amodule (e.g., "IN0001")
    """

    print("add module to schedule:", module_code)

    import requests

    schedule_id = state["user_id"]
    semester = state["semester"]

    response = requests.post(
        f"{env.schedule_manager_base_url}/schedule/{schedule_id}/modules?semester={semester}",
        json=module_code,
        headers={"Content-Type": "application/json"},
    )

    if response.status_code == 200:
        return Document(f"Module {module_code} added")
    elif response.status_code == 400:
        return Document("Invalid module code.")
    else:
        return Document(
            f"Unexpected response: {response.status_code} - {response.text}"
        )


@tool(parse_docstring=True)
def remove_module_from_schedule(
    module_code: str,
    state: Annotated[State, InjectedState],
    config: RunnableConfig = None,
) -> List[Document]:
    """
    Remove a module from the schedule

    Args:
        module_code: The code of  amodule (e.g., "IN0001")
    """

    print("remove module from schedule:", module_code)

    import requests

    schedule_id = state["user_id"]
    semester = state["semester"]

    response = requests.delete(
        f"{env.schedule_manager_base_url}/schedule/{schedule_id}/modules?semester={semester}",
        json=module_code,
        headers={"Content-Type": "application/json"},
    )

    if response.status_code == 204:
        return Document(f"Module {module_code} removed")
    elif response.status_code == 404:
        return Document("Module or schedule not found.")
    else:
        return Document(
            f"Unexpected response: {response.status_code} - {response.text}"
        )


@tool(parse_docstring=True)
def get_schedule(state: Annotated[State, InjectedState]) -> List[Document]:
    """
    Get the schedule

    Args:
    """

    print("get schedule")

    import requests

    schedule_id = state["user_id"]
    semester = state["semester"]

    response = requests.get(
        f"{env.schedule_manager_base_url}/schedule/{schedule_id}/modules?semester={semester}"
    )

    if response.status_code == 200:
        module_list = response.json()
        if module_list:
            return Document("Modules in schedule:\n" + "\n".join(module_list))
        else:
            return Document("No modules in schedule.")
    elif response.status_code == 404:
        return Document("Schedule not found.")
    else:
        return Document(
            f"Unexpected response: {response.status_code} - {response.text}"
        )


def route_tool_output(state: State) -> Literal["post_retrieval", "generate_answer"]:
    if state["messages"][-1].name == "retrieve_modules":
        return "post_retrieval"
    else:
        return "generate_answer"


def post_retrieval(state: State) -> dict:
    print("Running grade_documents node...")
    return state


async def build_graph():
    valkey_client = AsyncRedis.from_url(env.redis_uri, decode_responses=True)
    checkpointer = TTLRedisSaver(
        ttl_seconds=2 * 60 * 60,
        redis_client=valkey_client,
    )
    await checkpointer.asetup()

    workflow = StateGraph(State)
    workflow.add_node(generate_query_or_respond)

    tool_node = ToolNode(
        [
            retrieve_modules,
            generate_schedule,
            add_module_to_schedule,
            remove_module_from_schedule,
            get_schedule,
        ]
    )
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

    workflow.add_node("post_retrieval", post_retrieval)

    workflow.add_conditional_edges(
        "tools",
        route_tool_output,
        {
            "post_retrieval": "post_retrieval",
            "generate_answer": "generate_answer",
        },
    )

    workflow.add_node("grade_documents", grade_documents)

    workflow.add_conditional_edges(
        "post_retrieval",
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
    return workflow.compile(checkpointer=checkpointer)


@router.get("/chat")
async def stream_response(prompt: str, convId: str, studyProgramId: int, semester: str):
    study_program_name = get_study_program_name_by_id(studyProgramId)

    if study_program_name is None:
        return {"error": f"No study program found with ID '{studyProgramId}'"}

    graph = await build_graph()

    async def generate(
        user_input: str, user_id: str, study_program: int, semester: str
    ):
        config = {"configurable": {"thread_id": user_id}}
        async for msg, metadata in graph.astream(
            {
                "messages": [("user", user_input)],
                "study_program_id": study_program,
                "study_program_name": study_program_name,
                "semester": semester,
                "user_id": user_id,
            },
            config=config,
            stream_mode="messages",
        ):
            if msg.content and metadata["langgraph_node"] == "generate_answer":
                yield f"data: {msg.content}\n\n"
                await sleep(0.04)  # simply for chat output smoothing

    try:
        res = StreamingResponse(
            generate(prompt, convId, studyProgramId, semester),
            media_type="text/event-stream",
        )
        return res
    except Exception as e:
        print("Error in route handler:", e)
        return StreamingResponse(
            iter([f"data: Fatal error occurred: {str(e)}\n\n"]),
            media_type="text/event-stream",
        )
