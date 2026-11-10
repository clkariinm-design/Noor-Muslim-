"""Authentication endpoints for Noor Muslim API.

Email/password and Google ID-token sign-in. Refresh tokens are opaque and
stored only as SHA-256 hashes; access tokens are short-lived JWTs.
"""
from __future__ import annotations

import hashlib
import secrets
from datetime import datetime, timedelta, timezone
from typing import Annotated

import jwt
from fastapi import APIRouter, Depends, HTTPException, status
from fastapi.security import HTTPAuthorizationCredentials, HTTPBearer
from google.auth.transport import requests as google_requests
from google.oauth2 import id_token
from jwt import InvalidTokenError
from pydantic import BaseModel, Field
from pwdlib import PasswordHash
from sqlalchemy import text
from sqlalchemy.exc import IntegrityError

from config import get_settings
from database import configured_engine

router = APIRouter(prefix="/api/v1/auth", tags=["authentication"])
bearer = HTTPBearer(auto_error=False)
password_hasher = PasswordHash.recommended()
ACCESS_TOKEN_MINUTES = 15
REFRESH_TOKEN_DAYS = 30


class RegisterInput(BaseModel):
    email: str = Field(min_length=5, max_length=320)
    password: str = Field(min_length=10, max_length=128)


class LoginInput(BaseModel):
    email: str = Field(min_length=5, max_length=320)
    password: str = Field(min_length=1, max_length=128)


class GoogleInput(BaseModel):
    id_token: str = Field(min_length=20, max_length=8192)


class RefreshInput(BaseModel):
    refresh_token: str = Field(min_length=40, max_length=256)


class TokenResponse(BaseModel):
    access_token: str
    refresh_token: str
    token_type: str = "bearer"
    expires_in: int = ACCESS_TOKEN_MINUTES * 60


def normalize_email(email: str) -> str:
    value = email.strip().lower()
    if "@" not in value or "." not in value.rsplit("@", 1)[-1]:
        raise HTTPException(status_code=422, detail="invalid_email")
    return value


def hash_password(password: str) -> str:
    return password_hasher.hash(password)


def verify_password(password: str, encoded_hash: str) -> bool:
    try:
        return password_hasher.verify(password, encoded_hash)
    except (ValueError, TypeError):
        return False


def hash_refresh_token(token: str) -> str:
    return hashlib.sha256(token.encode("utf-8")).hexdigest()


def _jwt_secret() -> str:
    secret = get_settings().auth_jwt_secret
    if not secret or len(secret) < 32:
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail="authentication_not_configured",
        )
    return secret


def create_access_token(user_id: str) -> str:
    now = datetime.now(timezone.utc)
    payload = {
        "sub": user_id,
        "iat": now,
        "exp": now + timedelta(minutes=ACCESS_TOKEN_MINUTES),
        "type": "access",
    }
    return jwt.encode(payload, _jwt_secret(), algorithm="HS256")


def _issue_session(connection, user_id: str) -> TokenResponse:
    refresh_token = secrets.token_urlsafe(48)
    now = datetime.now(timezone.utc)
    connection.execute(
        text(
            """
            INSERT INTO auth_sessions (user_id, refresh_token_hash, expires_at)
            VALUES (:user_id, :token_hash, :expires_at)
            """
        ),
        {
            "user_id": user_id,
            "token_hash": hash_refresh_token(refresh_token),
            "expires_at": now + timedelta(days=REFRESH_TOKEN_DAYS),
        },
    )
    return TokenResponse(
        access_token=create_access_token(user_id),
        refresh_token=refresh_token,
    )


def _bearer_user(
    credentials: Annotated[HTTPAuthorizationCredentials | None, Depends(bearer)],
) -> str:
    if credentials is None or credentials.scheme.lower() != "bearer":
        raise HTTPException(status_code=401, detail="not_authenticated")
    try:
        payload = jwt.decode(
            credentials.credentials,
            _jwt_secret(),
            algorithms=["HS256"],
            options={"require": ["sub", "exp", "iat", "type"]},
        )
        if payload.get("type") != "access":
            raise InvalidTokenError("wrong token type")
        return str(payload["sub"])
    except InvalidTokenError as exc:
        raise HTTPException(status_code=401, detail="invalid_access_token") from exc


@router.post("/register", response_model=TokenResponse, status_code=201)
def register(payload: RegisterInput) -> TokenResponse:
    email = normalize_email(payload.email)
    password_hash = hash_password(payload.password)
    try:
        with configured_engine().begin() as connection:
            result = connection.execute(
                text(
                    """
                    INSERT INTO users (email, password_hash, email_verified)
                    VALUES (:email, :password_hash, FALSE)
                    RETURNING id
                    """
                ),
                {"email": email, "password_hash": password_hash},
            ).mappings().one()
            return _issue_session(connection, str(result["id"]))
    except IntegrityError as exc:
        raise HTTPException(status_code=409, detail="email_already_registered") from exc


@router.post("/login", response_model=TokenResponse)
def login(payload: LoginInput) -> TokenResponse:
    email = normalize_email(payload.email)
    with configured_engine().begin() as connection:
        user = connection.execute(
            text("SELECT id, password_hash FROM users WHERE email = :email AND is_active = TRUE"),
            {"email": email},
        ).mappings().first()
        if user is None or not user["password_hash"] or not verify_password(
            payload.password, user["password_hash"]
        ):
            raise HTTPException(status_code=401, detail="invalid_email_or_password")
        return _issue_session(connection, str(user["id"]))


@router.post("/google", response_model=TokenResponse)
def google_sign_in(payload: GoogleInput) -> TokenResponse:
    client_id = get_settings().google_client_id
    if not client_id:
        raise HTTPException(
            status_code=503,
            detail="google_sign_in_not_configured",
        )
    try:
        claims = id_token.verify_oauth2_token(
            payload.id_token,
            google_requests.Request(),
            client_id,
        )
    except ValueError as exc:
        raise HTTPException(status_code=401, detail="invalid_google_id_token") from exc

    email = claims.get("email")
    subject = claims.get("sub")
    if not email or not subject or claims.get("email_verified") is not True:
        raise HTTPException(status_code=401, detail="google_email_not_verified")

    normalized_email = normalize_email(email)
    with configured_engine().begin() as connection:
        user = connection.execute(
            text(
                """
                INSERT INTO users (email, google_subject, email_verified)
                VALUES (:email, :subject, TRUE)
                ON CONFLICT (email) DO UPDATE
                    SET google_subject = COALESCE(users.google_subject, EXCLUDED.google_subject),
                        email_verified = TRUE,
                        updated_at = NOW()
                WHERE users.google_subject IS NULL OR users.google_subject = EXCLUDED.google_subject
                RETURNING id
                """
            ),
            {"email": normalized_email, "subject": subject},
        ).mappings().first()
        if user is None:
            raise HTTPException(status_code=409, detail="google_account_conflict")
        return _issue_session(connection, str(user["id"]))


@router.post("/refresh", response_model=TokenResponse)
def refresh(payload: RefreshInput) -> TokenResponse:
    token_hash = hash_refresh_token(payload.refresh_token)
    now = datetime.now(timezone.utc)
    with configured_engine().begin() as connection:
        session = connection.execute(
            text(
                """
                SELECT id, user_id FROM auth_sessions
                WHERE refresh_token_hash = :token_hash
                  AND revoked_at IS NULL AND expires_at > :now
                FOR UPDATE
                """
            ),
            {"token_hash": token_hash, "now": now},
        ).mappings().first()
        if session is None:
            raise HTTPException(status_code=401, detail="invalid_refresh_token")
        connection.execute(
            text("UPDATE auth_sessions SET revoked_at = :now WHERE id = :id"),
            {"now": now, "id": session["id"]},
        )
        return _issue_session(connection, str(session["user_id"]))


@router.post("/logout", status_code=204)
def logout(payload: RefreshInput) -> None:
    with configured_engine().begin() as connection:
        connection.execute(
            text(
                """
                UPDATE auth_sessions SET revoked_at = NOW()
                WHERE refresh_token_hash = :token_hash AND revoked_at IS NULL
                """
            ),
            {"token_hash": hash_refresh_token(payload.refresh_token)},
        )
    return None


@router.get("/me")
def me(user_id: Annotated[str, Depends(_bearer_user)]) -> dict[str, str | bool]:
    with configured_engine().connect() as connection:
        user = connection.execute(
            text(
                """
                SELECT id, email, email_verified FROM users
                WHERE id = :user_id AND is_active = TRUE
                """
            ),
            {"user_id": user_id},
        ).mappings().first()
    if user is None:
        raise HTTPException(status_code=401, detail="user_not_found")
    return {
        "id": str(user["id"]),
        "email": user["email"],
        "email_verified": bool(user["email_verified"]),
    }
