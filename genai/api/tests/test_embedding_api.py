# coding: utf-8

from fastapi.testclient import TestClient


from pydantic import Field, StrictInt  # noqa: F401
from typing import Any, List  # noqa: F401
from typing_extensions import Annotated  # noqa: F401
from openapi_server.models.error import Error  # noqa: F401
from openapi_server.models.study_program import StudyProgram  # noqa: F401


def test_create_study_program(client: TestClient):
    """Test case for create_study_program

    Create a new study program and embed the modules.
    """
    study_program = {"id":0,"modules":[6,6]}

    headers = {
    }
    # uncomment below to make a request
    #response = client.request(
    #    "POST",
    #    "/embed",
    #    headers=headers,
    #    json=study_program,
    #)

    # uncomment below to assert the status code of the HTTP response
    #assert response.status_code == 200


def test_delete_study_program(client: TestClient):
    """Test case for delete_study_program

    Delete a study program
    """

    headers = {
    }
    # uncomment below to make a request
    #response = client.request(
    #    "DELETE",
    #    "/embed/{id}".format(id=56),
    #    headers=headers,
    #)

    # uncomment below to assert the status code of the HTTP response
    #assert response.status_code == 200


def test_update_study_program(client: TestClient):
    """Test case for update_study_program

    Add new modules to a study program
    """
    request_body = [[56]]

    headers = {
    }
    # uncomment below to make a request
    #response = client.request(
    #    "POST",
    #    "/embed/{id}".format(id=56),
    #    headers=headers,
    #    json=request_body,
    #)

    # uncomment below to assert the status code of the HTTP response
    #assert response.status_code == 200

