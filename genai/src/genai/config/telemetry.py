from logging import info
from fastapi import FastAPI, Request
from opentelemetry import metrics
from opentelemetry.sdk.metrics import MeterProvider
from opentelemetry.sdk.resources import Resource
import importlib.metadata

from prometheus_client import CollectorRegistry, Counter, multiprocess, make_asgi_app

import os

os.environ["PROMETHEUS_MULTIPROC_DIR"] = "/tmp/metrics"
os.makedirs("/tmp/metrics", exist_ok=True)


def init_telemetry(app: FastAPI):
    resource = Resource.create({"service.name": "genai"})
    provider = MeterProvider(resource=resource)
    metrics.set_meter_provider(provider)

    registry = CollectorRegistry()
    multiprocess.MultiProcessCollector(registry)

    http_requests_total = Counter(
        "http_requests_total",
        "Counts of HTTP requests by method and path",
        ["method", "path"],
        registry=registry,
    )

    @app.middleware("http")
    async def prometheus_request_counter(request: Request, call_next):
        path = request.url.path
        method = request.method

        http_requests_total.labels(method=method, path=path).inc()

        response = await call_next(request)
        return response

    global genai_release_version
    genai_release_version = Counter(
        "genai_release_version",
        "Counter for GenAI release version",
        ["version"],
        registry=registry,
    )
    __genai_version = importlib.metadata.version("genai")
    genai_release_version.labels(version=__genai_version).inc()

    global vecdb_query_counter
    vecdb_query_counter = Counter(
        "vecdb_query_total",
        "The number of vector db tool calls",
        registry=registry,
    )

    global vecdb_rephrase_query_counter
    vecdb_rephrase_query_counter = Counter(
        "vecdb_rephrase_query_total",
        "The number of rephrases during a vector db tool call",
        registry=registry,
    )

    global input_tokens_counter
    input_tokens_counter = Counter(
        "langchain_input_tokens_total",
        "Total number of input (prompt) tokens consumed by LangChain LLMs",
        registry=registry,
    )

    global output_tokens_counter
    output_tokens_counter = Counter(
        "langchain_output_tokens_total",
        "Total number of output (completion) tokens returned by LangChain LLMs",
        registry=registry,
    )

    metrics_app = make_asgi_app(registry=registry)
    app.mount("/metrics", metrics_app)


from langchain.callbacks.base import BaseCallbackHandler
from typing import Any, Dict, Optional, override


class PrometheusTokenCallback(BaseCallbackHandler):
    @override
    def on_chain_end(self, outputs: Dict[str, Any], **kwargs: Any) -> None:
        llm_output = outputs.get("llm_output", {})
        print(outputs)
        token_usage = llm_output.get("token_usage") or llm_output.get("usage")

        if token_usage:
            prompt_tokens = token_usage.get("prompt_tokens", 0)
            completion_tokens = token_usage.get("completion_tokens", 0)
            total_tokens = token_usage.get(
                "total_tokens", prompt_tokens + completion_tokens
            )

            print(f"  → prompt_tokens: {prompt_tokens}")
            print(f"  → completion_tokens: {completion_tokens}")
            print(f"  → total_tokens: {total_tokens}")

            if prompt_tokens:
                input_tokens_counter.inc(prompt_tokens)
            if completion_tokens:
                output_tokens_counter.inc(completion_tokens)
        else:
            print("  → No token usage found in chain outputs.")
