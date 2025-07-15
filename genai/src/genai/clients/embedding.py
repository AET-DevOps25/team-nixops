from langchain_openai.embeddings import OpenAIEmbeddings
from langchain_ollama.embeddings import OllamaEmbeddings

from ..config import env

if env.embedding_ai_provider == "openai":
    if not env.embedding_openai_org:
        raise Exception("Missing EMBEDDING_OPENAI_ORG")
    embedding_client = OpenAIEmbeddings(
        model=env.embedding_model,
        api_key=env.embedding_api_key,
        organization=env.embedding_openai_org,
    )
elif env.embedding_ai_provider == "ollama":
    if not env.embedding_ollama_base_url:
        raise Exception("Missing EMBEDDING_OLLAMA_BASE_URL")
    embedding_client = OllamaEmbeddings(
        model=env.embedding_model,
        base_url=env.embedding_ollama_base_url,
        api_key=env.embedding_api_key,
    )
else:
    raise Exception(
        f'Invalid AI provider "{env.env.embedding_ai_provider}", expected "openai" or "ollama"'
    )
