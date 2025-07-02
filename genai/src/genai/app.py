"""
Module Name: GenAI server
Description:
    A basic FastAPI API that returns a "Hello, World!" message for the root endpoint.
    This module demonstrates a minimal FastAPI setup and endpoint implementation.
"""

import yaml
import uvicorn
import logging

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from contextlib import asynccontextmanager
from prometheus_fastapi_instrumentator import Instrumentator

from .routers import embed, stream
from .config import cors_origins
from .logging import LOGGING_CONFIG

logger = logging.getLogger("uvicorn.error")


@asynccontextmanager
async def lifespan(app: FastAPI):
    instrumentator.expose(app)
    yield


app = FastAPI(
    lifespan=lifespan,  # include the lifespan func in the FastAPI init call
)
instrumentator = Instrumentator().instrument(app)  # Initialize the instrumentator
app.include_router(stream.router)
app.include_router(embed.router)


origins = cors_origins.split(", ")

app.add_middleware(
    CORSMiddleware,
    allow_origins=origins,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


def custom_openapi():
    with open("openapi.yml", "r") as openapi:
        return yaml.safe_load(openapi)


app.openapi = custom_openapi


def run():
    uvicorn.run(app, host="0.0.0.0", log_level="trace", log_config=LOGGING_CONFIG)
