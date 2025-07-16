from fastapi import FastAPI
from opentelemetry import metrics
from opentelemetry.sdk.metrics import MeterProvider
from opentelemetry.sdk.resources import Resource
from opentelemetry.instrumentation.langchain import LangchainInstrumentor
from opentelemetry.instrumentation.fastapi import FastAPIInstrumentor
from opentelemetry.instrumentation.system_metrics import SystemMetricsInstrumentor
import importlib.metadata

from prometheus_client import CollectorRegistry, Counter, multiprocess, make_asgi_app


def init_telemetry(app: FastAPI):
    FastAPIInstrumentor.instrument_app(app, excluded_urls="metrics,healthcheck")
    LangchainInstrumentor().instrument()
    SystemMetricsInstrumentor().instrument()

    resource = Resource.create({"service.name": "genai"})
    provider = MeterProvider(resource=resource)
    metrics.set_meter_provider(provider)

    registry = CollectorRegistry()
    multiprocess.MultiProcessCollector(registry)

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
