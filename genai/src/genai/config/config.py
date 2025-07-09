from decouple import config

# LLM
llm_api_url = config("LLM_API_URL", default="https://gpu.aet.cit.tum.de/ollama")
llm_api_key = config("LLM_API_KEY")
llm_chat_model = config("LLM_CHAT_MODEL", default="llama3.3:latest")
llm_embedding_model = config("LLM_EMBEDDING_MODEL", default="deepseek-r1:70b")
llm_chat_temp = config("LLM_CHAT_TEMP", default=0.5, cast=float)

# Web
cors_origins = config(
    "CORS_ORIGINS",
    default="http://localhost, http://localhost:8000, http://localhost:3000",
)

# DB
db_user = config("POSTGRES_USER", default="pguser")
db_pass = config("POSTGRES_PASS", default="pgpass")
db_host = config("POSTGRES_HOST", default="localhost:5432")
db_name = config("POSTGRES_NAME", default="nixops")

milvus_uri = config("MILVUS_URI", default="http://localhost:19530")
milvus_token = config("MILVUS_TOKEN", default="root:Milvus")
