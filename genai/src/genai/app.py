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
from contextlib import asynccontextmanager

from .data.db import create_db_and_tables
from .routers import embedding, generation

logger = logging.getLogger("uvicorn.error")

app = FastAPI()
app.include_router(generation.router)
app.include_router(embedding.router)

cors_env = config(
    "CORS_ORIGINS",
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
    with open("openapi.yml", "r") as genai_file:
        with open("../scraper/openapi.yaml", "r") as scraper_file:
            if genai_file is not None and scraper_file is not None:
                genai_openapi = yaml.safe_load(genai_file)
                scraper_openapi = yaml.safe_load(scraper_file)
                genai_openapi["components"]["schemas"]["Semester"] = scraper_openapi[
                    "components"
                ]["schemas"]["Semester"]
                genai_openapi["components"]["schemas"]["StudyProgram"] = (
                    scraper_openapi["components"]["schemas"]["StudyProgram"]
                )
                genai_openapi["components"]["schemas"]["Module"] = scraper_openapi[
                    "components"
                ]["schemas"]["Module"]
                genai_openapi["components"]["schemas"]["ModuleCourses"] = (
                    scraper_openapi["components"]["schemas"]["ModuleCourses"]
                )
                genai_openapi["components"]["schemas"]["Course"] = scraper_openapi[
                    "components"
                ]["schemas"]["Course"]
                genai_openapi["components"]["schemas"]["Appointment"] = scraper_openapi[
                    "components"
                ]["schemas"]["Appointment"]
                return genai_openapi
            else:
                return {}


app.openapi = custom_openapi


def run():
    uvicorn.run(app, host="0.0.0.0", log_level="trace")
