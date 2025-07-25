from pymilvus import IndexType, MilvusClient, DataType, exceptions
from tenacity import (
    retry,
    stop_after_attempt,
    wait_fixed,
    retry_if_exception_type,
    before_log,
)
import logging

from ..config import env
from ..clients import embedding_client

logger = logging.getLogger(__name__)


@retry(
    stop=stop_after_attempt(5),
    wait=wait_fixed(3),
    retry=retry_if_exception_type((exceptions.MilvusException, ConnectionError)),
    before=before_log(logger, logging.INFO),
)
def create_milvus_client():
    return MilvusClient(uri=env.milvus_uri, token=env.milvus_token)


milvus_client = create_milvus_client()


def create_index(collection_name):
    index_params = milvus_client.prepare_index_params()
    index_params.add_index(
        field_name="description_vec", index_type="AUTOINDEX", metric_type="COSINE"
    )
    milvus_client.create_index(collection_name, index_params)


def embed_text(text):
    return embedding_client.embed_query(text)


def embedding_len():
    return len(embed_text("foo"))


schema = MilvusClient.create_schema(
    auto_id=False,
)
# each row is a module
schema.add_field(field_name="id", datatype=DataType.INT64, is_primary=True)
schema.add_field(field_name="code", datatype=DataType.VARCHAR, max_length=256)
schema.add_field(field_name="name", datatype=DataType.VARCHAR, max_length=256)
schema.add_field(field_name="description", datatype=DataType.VARCHAR, max_length=16192)
schema.add_field(
    field_name="description_vec", datatype=DataType.FLOAT_VECTOR, dim=embedding_len()
)
schema.add_field(field_name="courses", datatype=DataType.JSON)


def create_collection(collection_name):
    milvus_client.create_collection(
        collection_name=collection_name,
        schema=schema,
    )
    create_index(collection_name)
