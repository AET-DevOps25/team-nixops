# coding: utf-8

from fastapi.testclient import TestClient




def test_echo(client: TestClient):
    """Test case for echo

    Echo test
    """
    body = 'body_example'

    headers = {
        "Authorization": "BasicZm9vOmJhcg==",
        "api_key": "special-key",
    }
    # uncomment below to make a request
    #response = client.request(
    #    "POST",
    #    "/echo",
    #    headers=headers,
    #    json=body,
    #)

    # uncomment below to assert the status code of the HTTP response
    #assert response.status_code == 200

