"""
Module Name: GenAI server
Description:
    A basic FastAPI API that returns a "Hello, World!" message for the root endpoint.
    This module demonstrates a minimal FastAPI setup and endpoint implementation.
"""

import uvicorn
import logging

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from contextlib import asynccontextmanager

from .db.relational_db import create_db_and_tables
from .routers import embedding, generation
from .config.env import cors_origins
from .config.telemetry import init_telemetry
from .config.openapi import custom_openapi
from .config.logging import LOGGING_CONFIG

logger = logging.getLogger("uvicorn.error")


@asynccontextmanager
async def lifespan(app: FastAPI):
    create_db_and_tables()
    yield


app = FastAPI(lifespan=lifespan)
app.include_router(generation.router)
app.include_router(embedding.router)

init_telemetry(app)

origins = cors_origins.split(", ")

app.add_middleware(
    CORSMiddleware,
    allow_origins=origins,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.openapi = custom_openapi


def run():
    uvicorn.run(
        "genai.app:app",
        host="0.0.0.0",
        log_level="info",
        log_config=LOGGING_CONFIG,
        workers=4,
    )
