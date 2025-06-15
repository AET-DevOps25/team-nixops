"""
Module Name: GenAI server
Description:
    A basic FastAPI API that returns a "Hello, World!" message for the root endpoint.
    This module demonstrates a minimal FastAPI setup and endpoint implementation.
"""

from fastapi import FastAPI
import yaml
import uvicorn

from openapi_server.models.pet import Pet
from typing import List

app = FastAPI()


@app.get(
    "/pet/findByStatus",
    responses={
        200: {"model": List[Pet], "description": "successful operation"},
        400: {"description": "Invalid status value"},
    },
    tags=["pet"],
    summary="Finds Pets by status.",
    response_model_by_alias=True,
)
async def find_pets_by_status() -> List[Pet]:
    """Multiple status values can be provided with comma separated strings."""
    pet_dict = {
        "id": 1,
        "name": "Frany",
        "category": {"id": 10, "name": "Cats"},
        "photoUrls": ["http://example.com/photo1.jpg", "http://example.com/photo2.jpg"],
        "tags": [{"id": 101, "name": "cute"}, {"id": 102, "name": "small"}],
        "status": "available",
    }
    return [Pet.from_dict(pet_dict)]


def custom_openapi():
    with open("openapi.yml", "r") as openapi:
        return yaml.safe_load(openapi)


app.openapi = custom_openapi


def run():
    uvicorn.run(app, host="0.0.0.0", log_level="info")
