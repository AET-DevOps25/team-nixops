# coding: utf-8

from fastapi.testclient import TestClient


from pydantic import Field, StrictInt, StrictStr  # noqa: F401
from typing_extensions import Annotated  # noqa: F401


def test_stream_chat(client: TestClient):
    """Test case for stream_chat

    Communicate with the ChatBot
    """
    params = [("prompt", 'Write a poem'),     ("id", 420)]
    headers = {
    }
    # uncomment below to make a request
    #response = client.request(
    #    "GET",
    #    "/chat",
    #    headers=headers,
    #    params=params,
    #)

    # uncomment below to assert the status code of the HTTP response
    #assert response.status_code == 200

