from fastapi import FastAPI, Response
from opentelemetry import metrics
from opentelemetry.sdk.metrics import MeterProvider
from opentelemetry.sdk.resources import Resource
from opentelemetry.instrumentation.langchain import LangchainInstrumentor
from opentelemetry.instrumentation.fastapi import FastAPIInstrumentor
from opentelemetry.instrumentation.system_metrics import SystemMetricsInstrumentor
import importlib.metadata

from prometheus_client import CollectorRegistry, generate_latest, CONTENT_TYPE_LATEST
from prometheus_client.multiprocess import MultiProcessCollector


def init_telemetry(app: FastAPI):
    FastAPIInstrumentor.instrument_app(app, excluded_urls=["metrics", "healthcheck"])
    LangchainInstrumentor().instrument()
    SystemMetricsInstrumentor().instrument()

    @app.get("/metrics")
    async def metrics():
        registry = CollectorRegistry()
        MultiProcessCollector(registry)
        data = generate_latest(registry)
        return Response(content=data, media_type=CONTENT_TYPE_LATEST)

    @app.get("/healthcheck")
    async def healthcheck():
        return {"status": "ok"}
