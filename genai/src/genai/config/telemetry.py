from fastapi import FastAPI
from opentelemetry import metrics
from opentelemetry.exporter.prometheus import PrometheusMetricReader
from opentelemetry.sdk.metrics import MeterProvider
from opentelemetry.sdk.resources import Resource
from prometheus_client import make_asgi_app
from opentelemetry.instrumentation.langchain import LangchainInstrumentor
from opentelemetry.instrumentation.fastapi import FastAPIInstrumentor
import importlib.metadata


def init_telemetry(app: FastAPI):
    FastAPIInstrumentor.instrument_app(app, excluded_urls="metrics,healthcheck")
    LangchainInstrumentor().instrument()
    resource = Resource.create({"service.name": "your_service_name"})
    reader = PrometheusMetricReader()
    provider = MeterProvider(resource=resource, metric_readers=[reader])
    metrics.set_meter_provider(provider)
    meter = metrics.get_meter(__name__)
    genai_release_version_metric = meter.create_counter(
        "genai_release_version",
        description="Counter for GenAI release version",
    )

    version = importlib.metadata.version("genai")
    genai_release_version_metric.add(1, {"version": version})
    metrics_app = make_asgi_app()
    app.mount("/metrics", metrics_app)
