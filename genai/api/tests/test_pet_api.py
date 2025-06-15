# coding: utf-8

from fastapi.testclient import TestClient


from pydantic import Field, StrictStr, field_validator  # noqa: F401
from typing import Any, List, Optional  # noqa: F401
from typing_extensions import Annotated  # noqa: F401
from openapi_server.models.error import Error  # noqa: F401
from openapi_server.models.pet import Pet  # noqa: F401


def test_find_pets_by_status(client: TestClient):
    """Test case for find_pets_by_status

    Finds Pets by status.
    """
    params = [("status", available)]
    headers = {}
    # uncomment below to make a request
    # response = client.request(
    #    "GET",
    #    "/pet/findByStatus",
    #    headers=headers,
    #    params=params,
    # )

    # uncomment below to assert the status code of the HTTP response
    # assert response.status_code == 200
