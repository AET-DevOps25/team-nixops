from langchain_openai.chat_models.base import ChatOpenAI
from langchain_ollama.chat_models import ChatOllama

from ..config import env

if env.chat_ai_provider == "openai":
    if not env.chat_openai_org:
        raise Exception("Missing CHAT_OPENAI_ORG")
    chat_client = ChatOpenAI(
        model=env.chat_model,
        api_key=env.chat_api_key,
        organization=env.chat_openai_org,
    )
elif env.chat_ai_provider == "ollama":
    if not env.chat_ollama_base_url:
        raise Exception("Missing CHAT_OLLAMA_BASE_URL")
    chat_client = ChatOllama(
        model=env.chat_model,
        base_url=env.chat_ollama_base_url,
        api_key=env.chat_api_key,
    )
else:
    raise Exception(
        f'Invalid chat AI provider "{env.env.chat_ai_provider}", expected "openai" or "ollama"'
    )
