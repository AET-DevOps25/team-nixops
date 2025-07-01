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

from langchain.tools.retriever import create_retriever_tool
from langchain_core.callbacks import CallbackManagerForRetrieverRun
from langchain_core.documents import Document
from langchain_core.retrievers import BaseRetriever
from typing import List


class ToyRetriever(BaseRetriever):
    """A toy retriever that contains the top k documents that contain the user query."""

    def _get_relevant_documents(
        self, query: str, *, run_manager: CallbackManagerForRetrieverRun
    ) -> List[Document]:
        """Sync implementations for retriever."""
        results = milvus_client.search(
            collection_name="_171017263_additionalProp1",
            data=[embed_text(query)],
            anns_field="description_vec",  # only one anns field can exist
            limit=3,
            output_fields=["name", "courses"],
        )
        docs = [Document(doc["entity"]["name"]) for doc in results[0]]
        return docs


retriever_tool = create_retriever_tool(
    ToyRetriever(),
    "retrieve_modules",
    "only ever call this when the user is asking about university courses and modules! in this case this tool is used to Search and return information about univeristy modules. do not call this under any other circumstances otherwise you will be killed and deleted. this is of extremely high priority",
)
print("-----------------")
print(retriever_tool.name)
print(retriever_tool.description)
print(retriever_tool.args)

router = APIRouter()


class State(TypedDict):
    messages: Annotated[list, add_messages]


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
)


def generate_query_or_respond(state: MessagesState):
    """Call the model to generate a response based on the current state. Given
    the question, it will decide to retrieve using the retriever tool, or simply respond to the user.
    """
    response = llm.bind_tools([retriever_tool]).invoke(state["messages"])
    return {"messages": [response]}


# input = {"messages": [{"role": "user", "content": "i dont know which university courses i should take. i like programming"}]}
# print(generate_query_or_respond(input)["messages"][-1].pretty_print())

GRADE_PROMPT = (
    "You are a grader assessing relevance of a retrieved document to a user question. \n "
    "Here is the retrieved document: \n\n {context} \n\n"
    "Here is the user question: {question} \n"
    "If the document contains keyword(s) or semantic meaning related to the user question, grade it as relevant. \n"
    "Give a binary score 'yes' or 'no' score to indicate whether the document is relevant to the question."
)

from pydantic import BaseModel, Field


class GradeDocuments(BaseModel):
    """Grade documents using a binary score for relevance check."""

    binary_score: str = Field(
        description="Relevance score: 'yes' if relevant, or 'no' if not relevant"
    )


def grade_documents(
    state: MessagesState,
) -> Literal["generate_answer", "rewrite_question"]:
    """Determine whether the retrieved documents are relevant to the question."""
    messages = state["messages"]
    context = messages[-1].content
    question = messages[0].content

    for i in range(len(messages) - 2, -1, -1):
        is_human_msg = not hasattr(messages[i], "tool_calls")
        if is_human_msg:
            question = messages[i].content
            print("------------------------")
            print(question)
            print("------------------------")
            break

    prompt = GRADE_PROMPT.format(question=question, context=context)
    response = llm.with_structured_output(  # TODO: use separate model with temp zero
        GradeDocuments
    ).invoke([{"role": "user", "content": prompt}])
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


def rewrite_question(state: MessagesState):
    """Rewrite the original user question."""
    messages = state["messages"]
    question = messages[0].content
    prompt = REWRITE_PROMPT.format(question=question)
    response = llm.invoke([{"role": "user", "content": prompt}])
    return {"messages": [{"role": "user", "content": response.content}]}


GENERATE_PROMPT = (
    "You are an assistant for question-answering tasks. "
    "Use the following pieces of retrieved context to answer the question. "
    "If you don't know the answer, just say that you don't know. "
    "Use three sentences maximum and keep the answer concise.\n"
    "Question: {question} \n"
    "Context: {context}"
)


def generate_answer(state: MessagesState):
    """Generate an answer."""
    question = state["messages"][0].content
    context = state["messages"][-1].content
    prompt = GENERATE_PROMPT.format(question=question, context=context)
    response = llm.invoke([{"role": "user", "content": prompt}])
    return {"messages": [response]}


from langgraph.graph import StateGraph, START, END
from langgraph.prebuilt import tools_condition

checkpointer = InMemorySaver()
workflow = StateGraph(MessagesState)
workflow.add_node(generate_query_or_respond)
workflow.add_node("retrieve", ToolNode([retriever_tool]))
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
        END: END,
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
async def stream_response(prompt: str, id: str):
    async def generate(user_input: str, user_id: str):
        config = {"configurable": {"thread_id": user_id}}
        async for message_chunk, _ in graph.astream(
            {"messages": [("user", user_input)]},
            config=config,
            stream_mode="messages",
        ):
            print(message_chunk)
            # if(message_chunk.tool_calls is None):
            yield f"data: {message_chunk.content}\n\n"
            await sleep(0.04)  # simply for chat output smoothing

    res = StreamingResponse(generate(prompt, id), media_type="text/event-stream")
    return res
