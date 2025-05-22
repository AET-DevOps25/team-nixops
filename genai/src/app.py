"""
Module Name: hello_world_api
Description:
    A basic FastAPI API that returns a "Hello, World!" message for the root endpoint.
    This module demonstrates a minimal FastAPI setup and endpoint implementation.

Endpoints:
    GET /
        Returns a simple "Hello, World!" message in JSON format.

Usage:
    1. Install FastAPI and Uvicorn if not already installed:
           pip install fastapi uvicorn

    2. Run the API using Uvicorn:
           uvicorn hello_world_api:app --reload

    3. Open your browser or use a tool like curl to access:
           http://127.0.0.1:8000/

Author: Your Name
Version: 1.0.0
License: MIT License
"""

from typing import Union

from fastapi import FastAPI
from openapi_server.apis.default_api_base import BaseDefaultApi

app = FastAPI()


@app.get(
    "/",
    responses={
        200: {"description": "Successful response"},
    },
    tags=["default"],
    response_model_by_alias=True,
)
async def read_root():
    return {"Hello": "World"}
