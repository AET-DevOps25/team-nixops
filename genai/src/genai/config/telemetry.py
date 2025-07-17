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

    global foo
    foo = Counter(
        "foo_total",
        "An example counter",
        registry=registry,
    )

    metrics_app = make_asgi_app(registry=registry)
    app.mount("/metrics", metrics_app)
