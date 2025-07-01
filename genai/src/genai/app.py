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

from .data.db import create_db_and_tables
from .routers import embedding, generation

logger = logging.getLogger("uvicorn.error")

app = FastAPI()
app.include_router(generation.router)
app.include_router(embedding.router)

origins = [
    "http://localhost",
    "http://localhost:8000",
    "http://localhost:3000",
]

app.add_middleware(
    CORSMiddleware,
    allow_origins=origins,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# db init on startup
main_app_lifespan = app.router.lifespan_context


@asynccontextmanager
async def lifespan_wrapper(app):
    create_db_and_tables()
    async with main_app_lifespan(app) as maybe_state:
        yield maybe_state


app.router.lifespan_context = lifespan_wrapper


def custom_openapi():
    with open("openapi.yml", "r") as openapi:
        return yaml.safe_load(openapi)


app.openapi = custom_openapi


def run():
    uvicorn.run(app, host="0.0.0.0", log_level="trace")
