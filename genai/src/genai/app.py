"""
Module Name: GenAI server
Description:
    A basic FastAPI API that returns a "Hello, World!" message for the root endpoint.
    This module demonstrates a minimal FastAPI setup and endpoint implementation.
"""

import yaml
import uvicorn
import logging
from decouple import config

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from .routers import embed, stream


logger = logging.getLogger("uvicorn.error")

app = FastAPI()
app.include_router(stream.router)
app.include_router(embed.router)

cors_env = config(
    "CORS_DOMAINS",
    default="http://localhost, http://localhost:8000, http://localhost:3000",
)
origins = cors_env.split(", ")

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
    uvicorn.run(app, host="0.0.0.0", log_level="trace")
