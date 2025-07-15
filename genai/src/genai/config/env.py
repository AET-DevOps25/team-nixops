from decouple import config

# Web
cors_origins = config(
    "CORS_ORIGINS",
    default="http://localhost, http://localhost:8000, http://localhost:3000",
)
logging_filename = config("LOGGING_FILENAME", default="/var/log/genai.log")

# DB
db_user = config("POSTGRES_USER", default="pguser")
db_pass = config("POSTGRES_PASS", default="pgpass")
db_host = config("POSTGRES_HOST", default="localhost:5432")
db_name = config("POSTGRES_NAME", default="nixops")

milvus_uri = config("MILVUS_URI", default="http://localhost:19530")
milvus_token = config("MILVUS_TOKEN", default="root:Milvus")

redis_uri = config("REDIS_URI", default="redis://localhost:6379")

# Schedule-Manager
schedule_manager_base_url = config(
    "SCHEDULE_MANAGER_BASE_URL", default="http://localhost:8042"
)

# === Defaults ===
default_ai_provider = config("DEFAULT_AI_PROVIDER", default="openai")
default_model = config("DEFAULT_MODEL", default="gpt-4.1-mini")
default_embedding_model = config(
    "DEFAULT_EMBEDDING_MODEL", default="text-embedding-3-small"
)
default_api_key = config("DEFAULT_API_KEY", default="")
default_ollama_base_url = config("DEFAULT_OLLAMA_BASE_URL", default="")
default_openai_org = config("DEFAULT_OPENAI_ORG", default="")

# === Chat ===
chat_ai_provider = config("CHAT_AI_PROVIDER", default=default_ai_provider)
chat_model = config("CHAT_MODEL", default=default_model)
chat_api_key = config("CHAT_API_KEY", default=default_api_key)
chat_ollama_base_url = config("CHAT_OLLAMA_BASE_URL", default=default_ollama_base_url)
chat_openai_org = config("CHAT_OPENAI_ORG", default=default_openai_org)

# === Reasoning ===
reasoning_ai_provider = config("REASONING_AI_PROVIDER", default=chat_ai_provider)
reasoning_model = config("REASONING_MODEL", default=chat_model)
reasoning_api_key = config("REASONING_API_KEY", default=chat_api_key)
reasoning_ollama_base_url = config(
    "REASONING_OLLAMA_BASE_URL", default=chat_ollama_base_url
)
reasoning_openai_org = config("REASONING_OPENAI_ORG", default=chat_openai_org)

# === Embedding ===
embedding_ai_provider = config("EMBEDDING_AI_PROVIDER", default=default_ai_provider)
embedding_model = config("EMBEDDING_MODEL", default=default_embedding_model)
embedding_api_key = config("EMBEDDING_API_KEY", default=chat_api_key)
embedding_ollama_base_url = config(
    "EMBEDDING_OLLAMA_BASE_URL", default=chat_ollama_base_url
)
embedding_openai_org = config("EMBEDDING_OPENAI_ORG", default=chat_openai_org)
