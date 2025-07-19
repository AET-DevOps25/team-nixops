import yaml
from importlib import resources
from datetime import date, datetime
from fastapi.encoders import jsonable_encoder


def custom_openapi():
    schema = yaml.safe_load(resources.read_text("genai", "openapi.yml"))

    schema = jsonable_encoder(
        schema,
        custom_encoder={
            datetime: lambda dt: dt.isoformat(),
            date: lambda d: d.isoformat(),
        },
    )

    return schema
