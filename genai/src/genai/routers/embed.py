from fastapi import APIRouter
from decouple import config
from ollama import Client

from pymilvus import IndexType, MilvusClient, DataType
from langchain_ollama import OllamaEmbeddings

# NOTE: https://milvus.io/docs/use-async-milvus-client-with-asyncio.md#Create-index

router = APIRouter()

llm_api_url = config("LLM_API_URL", default="https://gpu.aet.cit.tum.de/ollama")
llm_api_key = config("LLM_API_KEY")
# mxbai-embed-large performs really well, but not available
# deepseek is the best of the available ones
llm_embedding_model = config("LLM_EMBEDDING_MODEL", default="deepseek-r1:70b") 

ollama_client = Client(
    host=llm_api_url, headers={"Authorization": f"Bearer {llm_api_key}"}
)

milvus_client = MilvusClient(uri="http://localhost:19530", token="root:Milvus")


def emb_text(text):
    # from pymilvus import model
    # ef = model.DefaultEmbeddingFunction()
    # return ef(text)[0]
    response = ollama_client.embeddings(
        model=llm_embedding_model,
        prompt=text,
    )
    return response["embedding"]


def embedding_len():
    return len(emb_text("foo"))


schema = MilvusClient.create_schema(
    auto_id=False,
    enable_dynamic_field=True,
)

collection_name = "informatics"

# use FloatVector for embedded data
schema.add_field(field_name="my_id", datatype=DataType.INT64, is_primary=True)
schema.add_field(
    field_name="my_vector", datatype=DataType.FLOAT_VECTOR, dim=embedding_len()
)
schema.add_field(field_name="my_varchar", datatype=DataType.VARCHAR, max_length=512)

if milvus_client.has_collection(collection_name):
    milvus_client.drop_collection(collection_name)
milvus_client.create_collection(
    collection_name=collection_name,
    schema=schema,
)

index_params = milvus_client.prepare_index_params()
index_params.add_index(field_name="my_vector", index_type="AUTOINDEX", metric_type="COSINE")
milvus_client.create_index(collection_name, index_params)

milvus_client.load_collection(collection_name=collection_name)
res = milvus_client.get_load_state(collection_name=collection_name)
print(res)
data = [
    {"my_id": 422, "my_vector": emb_text("ich reise gerne"), "my_varchar": "ich reise gerne"},
    {"my_id": 423, "my_vector": emb_text("mein hobby ist reisen"), "my_varchar": "mein hobby ist reisen"},
    {"my_id": 424, "my_vector": emb_text("ich studiere informatik"), "my_varchar": "ich studiere informatik"},
    {"my_id": 425, "my_vector": emb_text("mein studiengang ist informatik"), "my_varchar": "mein studiengang ist informatik"},
    {"my_id": 426, "my_vector": emb_text("ich studiere"), "my_varchar": "ich studiere"},
]
res = milvus_client.insert(collection_name=collection_name, data=data)
milvus_client.flush(collection_name=collection_name)
print(res)

search_res = milvus_client.search(
    collection_name=collection_name,
    data=[
        emb_text("turingmaschine")
    ],
    anns_field="my_vector",
    limit=3,  # Return top 3 results
    output_fields=["my_varchar"]
)
print(search_res)
