package com.example

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

data class AuthTokens(
    val accessToken: String,
    val refreshToken: String
)

data class UserProfile(
    val id: String,
    val email: String,
    val emailVerified: Boolean
)

class AuthApiException(message: String, val statusCode: Int? = null) : IOException(message)

object AuthApi {
    private const val BASE_URL = "https://noor-muslim-python-api-production.up.railway.app"
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .callTimeout(25, TimeUnit.SECONDS)
        .build()

    fun register(email: String, password: String): AuthTokens =
        tokenRequest("/api/v1/auth/register", email, password)

    fun login(email: String, password: String): AuthTokens =
        tokenRequest("/api/v1/auth/login", email, password)

    fun refresh(refreshToken: String): AuthTokens {
        val payload = JSONObject().put("refresh_token", refreshToken)
        return parseTokens(post("/api/v1/auth/refresh", payload))
    }

    fun me(accessToken: String): UserProfile {
        val request = Request.Builder()
            .url("$BASE_URL/api/v1/auth/me")
            .header("Authorization", "Bearer $accessToken")
            .get()
            .build()
        val json = execute(request)
        return UserProfile(
            id = json.optString("id"),
            email = json.optString("email"),
            emailVerified = json.optBoolean("email_verified", false)
        )
    }

    fun logout(refreshToken: String) {
        val payload = JSONObject().put("refresh_token", refreshToken)
        runCatching { post("/api/v1/auth/logout", payload) }
    }

    private fun tokenRequest(path: String, email: String, password: String): AuthTokens {
        val payload = JSONObject()
            .put("email", email.trim())
            .put("password", password)
        return parseTokens(post(path, payload))
    }

    private fun post(path: String, payload: JSONObject): JSONObject {
        val body = payload.toString().toRequestBody(jsonMediaType)
        val request = Request.Builder()
            .url("$BASE_URL$path")
            .post(body)
            .build()
        return execute(request)
    }

    private fun execute(request: Request): JSONObject {
        try {
            client.newCall(request).execute().use { response ->
                val raw = response.body?.string().orEmpty()
                val json = runCatching { JSONObject(raw) }.getOrElse { JSONObject() }
                if (!response.isSuccessful) {
                    val detail = json.optString("detail").ifBlank {
                        "Server error (${response.code}). Please try again."
                    }
                    throw AuthApiException(humanize(detail), response.code)
                }
                return json
            }
        } catch (error: AuthApiException) {
            throw error
        } catch (error: IOException) {
            throw AuthApiException(
                "Could not reach Noor Muslim server. Check your internet connection."
            )
        }
    }

    private fun parseTokens(json: JSONObject): AuthTokens {
        val access = json.optString("access_token")
        val refresh = json.optString("refresh_token")
        if (access.isBlank() || refresh.isBlank()) {
            throw AuthApiException("The server returned an incomplete sign-in response.")
        }
        return AuthTokens(access, refresh)
    }

    private fun humanize(detail: String): String = when (detail) {
        "invalid_email_or_password" -> "Email or password is incorrect."
        "email_already_registered" -> "This email address is already registered."
        "invalid_email" -> "Enter a valid email address."
        "authentication_not_configured" -> "Account sign-in is being configured. Please try again later."
        "database_not_ready" -> "The account service is temporarily unavailable."
        "not_authenticated", "invalid_access_token" -> "Please sign in again."
        else -> detail.replace('_', ' ')
    }
}
