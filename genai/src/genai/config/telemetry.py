import time
from fastapi import FastAPI, Request
from opentelemetry import metrics
from opentelemetry.sdk.metrics import MeterProvider
from opentelemetry.sdk.resources import Resource
import importlib.metadata

from prometheus_client import CollectorRegistry, Counter, multiprocess, make_asgi_app

import os

from prometheus_client.metrics import Histogram

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
        "Total HTTP requests",
        ["method", "path", "status_code"],
    )
    REQUEST_LATENCY = Histogram(
        "http_request_duration_seconds",
        "HTTP request duration in seconds",
        ["method", "path"],
        buckets=[0.005, 0.01, 0.025, 0.05, 0.1, 0.25, 0.5, 1, 2.5, 5, 10],
    )

    @app.middleware("http")
    async def prometheus_middleware(request: Request, call_next):
        method = request.method
        path = request.url.path

        start_ts = time.time()
        try:
            response = await call_next(request)
            status_code = response.status_code
        except Exception:
            status_code = 500
            raise
        finally:
            duration = time.time() - start_ts
            # Observe the duration in seconds
            REQUEST_LATENCY.labels(method=method, path=path).observe(duration)
            # Increment the counter
            http_requests_total.labels(
                method=method,
                path=path,
                status_code=str(status_code),
            ).inc()

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
