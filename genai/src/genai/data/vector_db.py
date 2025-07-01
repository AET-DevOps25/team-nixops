from decouple import config
from ollama import Client

from pymilvus import IndexType, MilvusClient, DataType

milvus_client = MilvusClient(uri="http://localhost:19530", token="root:Milvus")

llm_api_url = config("LLM_API_URL", default="https://gpu.aet.cit.tum.de/ollama")
llm_api_key = config("LLM_API_KEY")
# mxbai-embed-large performs really well, but not available
# deepseek is the best of the available ones
llm_embedding_model = config("LLM_EMBEDDING_MODEL", default="deepseek-r1:70b")

ollama_client = Client(
    host=llm_api_url, headers={"Authorization": f"Bearer {llm_api_key}"}
)


def create_index(collection_name):
    index_params = milvus_client.prepare_index_params()
    index_params.add_index(
        field_name="description_vec", index_type="AUTOINDEX", metric_type="COSINE"
    )
    milvus_client.create_index(collection_name, index_params)


def embed_text(text):
    response = ollama_client.embeddings(
        model=llm_embedding_model,
        prompt=text,
    )
    return response["embedding"]


def embedding_len():
    return len(embed_text("foo"))


schema = MilvusClient.create_schema(
    auto_id=False,
)
# each row is a module
schema.add_field(
    field_name="id", datatype=DataType.VARCHAR, max_length=16, is_primary=True
)
schema.add_field(field_name="name", datatype=DataType.VARCHAR, max_length=128)
schema.add_field(field_name="description", datatype=DataType.VARCHAR, max_length=2048)
schema.add_field(
    field_name="description_vec", datatype=DataType.FLOAT_VECTOR, dim=embedding_len()
)
schema.add_field(field_name="timeslots", datatype=DataType.JSON)


def create_collection(collection_name):
    milvus_client.create_collection(
        collection_name=collection_name,
        schema=schema,
    )
    create_index(collection_name)
