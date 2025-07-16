from fastapi import FastAPI
import importlib.metadata
from opentelemetry import metrics
from opentelemetry.sdk.metrics import MeterProvider
from opentelemetry.sdk.resources import Resource
from opentelemetry.exporter.prometheus import PrometheusMetricReader

from prometheus_client import CollectorRegistry, multiprocess, make_asgi_app

from opentelemetry.instrumentation.fastapi import FastAPIInstrumentor
from opentelemetry.instrumentation.langchain import LangchainInstrumentor
from opentelemetry.instrumentation.system_metrics import SystemMetricsInstrumentor


def init_telemetry(app: FastAPI):
    FastAPIInstrumentor.instrument_app(app, excluded_urls="metrics,healthcheck")
    LangchainInstrumentor().instrument()
    SystemMetricsInstrumentor().instrument()

    resource = Resource.create({"service.name": "genai"})
    reader = PrometheusMetricReader()
    provider = MeterProvider(resource=resource, metric_readers=[reader])
    metrics.set_meter_provider(provider)

    registry = CollectorRegistry()
    multiprocess.MultiProcessCollector(registry)

    app.mount("/metrics", make_asgi_app(registry=registry))


meter = metrics.get_meter(__name__)
meter.create_counter("foo_total", "example counter")

genai_release_version = meter.create_counter(
    "genai_release_version", "Counter for GenAI release version"
)
__genai_version = importlib.metadata.version("genai")
genai_release_version.add(1, {"version": __genai_version})

vecdb_query_counter = meter.create_counter(
    "vecdb_query_total", "The number of vector db tool calls"
)

vecdb_rephrase_query_counter = meter.create_counter(
    "vecdb_rephrase_query_total", "The number of rephrases during a vector db tool call"
)
