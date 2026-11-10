from fastapi.testclient import TestClient

from main import app

client = TestClient(app)


def test_api_responses_include_baseline_security_headers():
    response = client.get("/health")

    assert response.headers["x-content-type-options"] == "nosniff"
    assert response.headers["x-frame-options"] == "DENY"
    assert response.headers["referrer-policy"] == "strict-origin-when-cross-origin"
    assert response.headers["permissions-policy"] == "camera=(), microphone=(), geolocation=()"


def test_api_does_not_advertise_server_implementation_details():
    response = client.get("/health")

    assert "server" not in response.headers
