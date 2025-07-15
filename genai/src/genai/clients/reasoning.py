from langchain_openai.chat_models.base import ChatOpenAI
from langchain_ollama.chat_models import ChatOllama

from ..config import env

if env.reasoning_ai_provider == "openai":
    if not env.reasoning_openai_org:
        raise Exception("Missing REASONING_OPENAI_ORG")
    reasoning_client = ChatOpenAI(
        model=env.reasoning_model,
        api_key=env.reasoning_api_key,
        organization=env.reasoning_openai_org,
    )
elif env.reasoning_ai_provider == "ollama":
    if not env.reasoning_ollama_base_url:
        raise Exception("Missing REASONING_OLLAMA_BASE_URL")
    reasoning_client = ChatOllama(
        model=env.reasoning_model,
        base_url=env.reasoning_ollama_base_url,
        api_key=env.reasoning_api_key,
    )
else:
    raise Exception(
        f'Invalid AI provider "{env.env.reasoning_ai_provider}", expected "openai" or "ollama"'
    )
