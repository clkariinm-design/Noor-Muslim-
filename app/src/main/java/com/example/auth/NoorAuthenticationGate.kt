package com.example.auth

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.AuthApi
import com.example.SecureTokenStore
import com.example.UserProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val PREFS_NAME = "noor_muslim_account"
private const val KEY_OFFLINE_MODE = "continue_without_account"

@Composable
fun NoorAuthenticationGate(
    content: @Composable (onManageAccount: () -> Unit) -> Unit
) {
    val context = LocalContext.current
    val preferences = remember(context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    val tokenStore = remember(context) { SecureTokenStore(context) }
    val scope = rememberCoroutineScope()

    var offlineMode by rememberSaveable {
        mutableStateOf(preferences.getBoolean(KEY_OFFLINE_MODE, false))
    }
    var manageAccount by rememberSaveable { mutableStateOf(false) }
    var isRegister by rememberSaveable { mutableStateOf(false) }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var profile by remember { mutableStateOf<UserProfile?>(null) }
    var loading by remember { mutableStateOf(true) }
    var busy by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var infoMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val accessToken = tokenStore.getAccessToken()
        val refreshToken = tokenStore.getRefreshToken()
        if (!accessToken.isNullOrBlank() && !refreshToken.isNullOrBlank()) {
            val restored = withContext(Dispatchers.IO) {
                runCatching { AuthApi.me(accessToken) }.getOrNull()
                    ?: runCatching {
                        val rotated = AuthApi.refresh(refreshToken)
                        tokenStore.save(rotated)
                        AuthApi.me(rotated.accessToken)
                    }.getOrNull()
            }
            profile = restored
            if (restored == null) {
                tokenStore.clear()
                if (!offlineMode) {
                    infoMessage = "Your saved session could not be restored. Sign in again or continue offline."
                }
            }
        }
        loading = false
    }

    if (loading) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
                Spacer(Modifier.height(12.dp))
                Text("Preparing Noor Muslim…")
            }
        }
        return
    }

    val needsAccountScreen = (profile == null && !offlineMode) || manageAccount
    if (!needsAccountScreen) {
        content {
            manageAccount = true
            errorMessage = null
            infoMessage = null
        }
        return
    }

    AccountScreen(
        email = email,
        onEmailChange = { email = it },
        password = password,
        onPasswordChange = { password = it },
        isRegister = isRegister,
        onToggleMode = {
            isRegister = !isRegister
            errorMessage = null
            infoMessage = null
        },
        profile = profile,
        busy = busy,
        errorMessage = errorMessage,
        infoMessage = infoMessage,
        onSubmit = {
            val cleanEmail = email.trim()
            val validation = validateCredentials(cleanEmail, password, isRegister)
            if (validation != null) {
                errorMessage = validation
            } else {
                scope.launch {
                    busy = true
                    errorMessage = null
                    infoMessage = null
                    try {
                        val tokens = withContext(Dispatchers.IO) {
                            if (isRegister) AuthApi.register(cleanEmail, password)
                            else AuthApi.login(cleanEmail, password)
                        }
                        tokenStore.save(tokens)
                        profile = withContext(Dispatchers.IO) {
                            AuthApi.me(tokens.accessToken)
                        }
                        offlineMode = false
                        preferences.edit().putBoolean(KEY_OFFLINE_MODE, false).apply()
                        manageAccount = false
                        password = ""
                    } catch (error: Exception) {
                        tokenStore.clear()
                        errorMessage = error.message ?: "Unable to connect. Please try again."
                    } finally {
                        busy = false
                    }
                }
            }
        },
        onContinueOffline = {
            offlineMode = true
            preferences.edit().putBoolean(KEY_OFFLINE_MODE, true).apply()
            manageAccount = false
            errorMessage = null
            password = ""
        },
        onSignOut = {
            scope.launch {
                busy = true
                withContext(Dispatchers.IO) {
                    tokenStore.getRefreshToken()?.let { AuthApi.logout(it) }
                }
                tokenStore.clear()
                profile = null
                offlineMode = true
                preferences.edit().putBoolean(KEY_OFFLINE_MODE, true).apply()
                manageAccount = false
                password = ""
                busy = false
                infoMessage = "You have signed out. Noor Muslim remains available offline."
            }
        },
        onBackToApp = {
            manageAccount = false
            errorMessage = null
            password = ""
        }
    )
}

@Composable
private fun AccountScreen(
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    isRegister: Boolean,
    onToggleMode: () -> Unit,
    profile: UserProfile?,
    busy: Boolean,
    errorMessage: String?,
    infoMessage: String?,
    onSubmit: () -> Unit,
    onContinueOffline: () -> Unit,
    onSignOut: () -> Unit,
    onBackToApp: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("نور مسلم", fontSize = 34.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text("Noor Muslim", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)

        if (profile != null) {
            Spacer(Modifier.height(14.dp))
            Text("Account", style = MaterialTheme.typography.titleLarge)
            Text(profile.email, style = MaterialTheme.typography.bodyLarge)
            Text(
                if (profile.emailVerified) "Email verified" else "Email verification is not configured yet",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(22.dp))
            Button(onClick = onSignOut, enabled = !busy, modifier = Modifier.fillMaxWidth()) {
                Text("Sign out")
            }
            TextButton(onClick = onBackToApp, enabled = !busy) { Text("Back to Noor Muslim") }
        } else {
            Spacer(Modifier.height(6.dp))
            Text(
                if (isRegister) "Create your account" else "Sign in to your account",
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(Modifier.height(24.dp))
            OutlinedTextField(
                value = email, onValueChange = onEmailChange,
                modifier = Modifier.fillMaxWidth(), label = { Text("Email address") },
                singleLine = true, enabled = !busy
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = password, onValueChange = onPasswordChange,
                modifier = Modifier.fillMaxWidth(), label = { Text("Password") },
                singleLine = true, visualTransformation = PasswordVisualTransformation(),
                enabled = !busy
            )
            if (isRegister) {
                Spacer(Modifier.height(8.dp))
                Text("Use at least 10 characters.", modifier = Modifier.fillMaxWidth(), style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.height(18.dp))
            Button(onClick = onSubmit, modifier = Modifier.fillMaxWidth(), enabled = !busy) {
                if (busy) {
                    CircularProgressIndicator(modifier = Modifier.width(18.dp).height(18.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(10.dp))
                }
                Text(if (isRegister) "Create account" else "Sign in")
            }
            TextButton(onClick = onToggleMode, enabled = !busy) {
                Text(if (isRegister) "Already registered? Sign in" else "New to Noor Muslim? Create account")
            }
            TextButton(onClick = onContinueOffline, enabled = !busy) {
                Text("Continue without an account")
            }
        }

        errorMessage?.let {
            Spacer(Modifier.height(10.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }
        infoMessage?.let {
            Spacer(Modifier.height(10.dp))
            Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.height(18.dp))
        Text(
            "Your Quran reading and core features remain available offline.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

internal fun validateCredentials(email: String, password: String, registering: Boolean): String? {
    val domain = email.substringAfterLast("@", "")
    if (!email.contains("@") || !domain.contains(".") || email.any { it.isWhitespace() }) {
        return "Enter a valid email address."
    }
    if (registering && password.length < 10) {
        return "Password must contain at least 10 characters."
    }
    if (!registering && password.isBlank()) {
        return "Enter your password."
    }
    return null
}
