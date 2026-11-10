package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    NoorMuslimApp()
                }
            }
        }
    }
}

@Composable
private fun NoorMuslimApp() {
    val context = LocalContext.current
    val sessionStore = remember { SecureTokenStore(context) }
    val scope = rememberCoroutineScope()

    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var isRegister by rememberSaveable { mutableStateOf(false) }
    var profile by remember { mutableStateOf<UserProfile?>(null) }
    var message by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    var checkingSession by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val refreshToken = sessionStore.getRefreshToken()
        val accessToken = sessionStore.getAccessToken()
        if (!refreshToken.isNullOrBlank() && !accessToken.isNullOrBlank()) {
            val restoredProfile = withContext(Dispatchers.IO) {
                runCatching { AuthApi.me(accessToken) }.getOrNull()
                    ?: runCatching {
                        val refreshed = AuthApi.refresh(refreshToken)
                        sessionStore.save(refreshed)
                        AuthApi.me(refreshed.accessToken)
                    }.getOrNull()
            }
            profile = restoredProfile
            if (restoredProfile == null) sessionStore.clear()
        }
        checkingSession = false
    }

    if (checkingSession) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator()
            Spacer(Modifier.height(12.dp))
            Text("Preparing Noor Muslim…")
        }
        return
    }

    if (profile != null) {
        SignedInScreen(
            profile = profile!!,
            busy = busy,
            onSignOut = {
                scope.launch {
                    busy = true
                    withContext(Dispatchers.IO) {
                        sessionStore.getRefreshToken()?.let { AuthApi.logout(it) }
                    }
                    sessionStore.clear()
                    profile = null
                    password = ""
                    message = "You have signed out."
                    busy = false
                }
            }
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 36.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(28.dp))
        Text("نور مسلم", fontSize = 34.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(
            "Noor Muslim",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            if (isRegister) "Create your account" else "Sign in to your account",
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Email address") },
            singleLine = true,
            enabled = !busy
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            enabled = !busy
        )
        if (isRegister) {
            Spacer(Modifier.height(8.dp))
            Text(
                "Use at least 10 characters.",
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodySmall
            )
        }
        Spacer(Modifier.height(20.dp))

        Button(
            onClick = {
                val cleanEmail = email.trim()
                val validationMessage = validateCredentials(cleanEmail, password, isRegister)
                if (validationMessage != null) {
                    message = validationMessage
                } else {
                    scope.launch {
                        busy = true
                        message = null
                        try {
                            val tokens = withContext(Dispatchers.IO) {
                                if (isRegister) AuthApi.register(cleanEmail, password)
                                else AuthApi.login(cleanEmail, password)
                            }
                            sessionStore.save(tokens)
                            profile = withContext(Dispatchers.IO) {
                                AuthApi.me(tokens.accessToken)
                            }
                        } catch (error: Exception) {
                            sessionStore.clear()
                            message = error.message ?: "Unable to connect. Please try again."
                        } finally {
                            busy = false
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !busy
        ) {
            if (busy) {
                CircularProgressIndicator(
                    modifier = Modifier.width(18.dp).height(18.dp),
                    strokeWidth = 2.dp
                )
                Spacer(Modifier.width(10.dp))
            }
            Text(if (isRegister) "Create account" else "Sign in")
        }

        TextButton(
            onClick = {
                isRegister = !isRegister
                message = null
            },
            enabled = !busy
        ) {
            Text(
                if (isRegister) "Already have an account? Sign in"
                else "New to Noor Muslim? Create account"
            )
        }

        message?.let {
            Spacer(Modifier.height(12.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }
        Spacer(Modifier.height(24.dp))
        Text(
            "Your account is protected by Noor Muslim's secure API.",
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun SignedInScreen(
    profile: UserProfile,
    busy: Boolean,
    onSignOut: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Noor Muslim",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(12.dp))
        Text("You are signed in.")
        Spacer(Modifier.height(6.dp))
        Text(profile.email, style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(24.dp))
        Button(onClick = onSignOut, enabled = !busy, modifier = Modifier.fillMaxWidth()) {
            Text("Sign out")
        }
    }
}

internal fun validateCredentials(email: String, password: String, registering: Boolean): String? {
    val emailDomain = email.substringAfterLast("@", "")
    if (!email.contains("@") || !emailDomain.contains(".")) {
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
