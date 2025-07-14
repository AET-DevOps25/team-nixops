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

# Schedule-Manager
schedule_manager_base_url = config(
    "SCHEDULE_MANAGER_BASE_URL", default="http://localhost:8042"
)

# Chat
chat_ai_provider = config("CHAT_AI_PROVIDER", "openai")
chat_model = config("CHAT_OPENAPI_MODEL", "gpt-4.1-mini")
chat_api_key = config("CHAT_API_KEY")

## Chat - OpenAI
chat_ollama_base_url = config("CHAT_OLLAMA_BASE_URL", "")

## Chat - Ollama
chat_openai_org = config("CHAT_OPENAI_ORG", "")


# Reasoning
reasoning_ai_provider = config("REASONING_AI_PROVIDER", "openai")
reasoning_model = config("REASONING_OPENAPI_MODEL", "gpt-4.1-mini")
reasoning_api_key = config("REASONING_API_KEY")

## Reasoning - OpenAI
reasoning_ollama_base_url = config("REASONING_OLLAMA_BASE_URL", "")

## Reasoning - Ollama
reasoning_openai_org = config("REASONING_OPENAI_ORG", "")


# Embedding
embedding_ai_provider = config("EMBEDDING_AI_PROVIDER", "openai")
embedding_model = config("EMBEDDING_OPENAPI_MODEL", "text-embedding-3-small")
embedding_api_key = config("EMBEDDING_API_KEY")

## Embedding - OpenAI
embedding_ollama_base_url = config("EMBEDDING_OLLAMA_BASE_URL", "")

## Embedding - Ollama
embedding_openai_org = config("EMBEDDING_OPENAI_ORG", "")
