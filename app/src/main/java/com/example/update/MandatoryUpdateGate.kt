package com.example.update

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun MandatoryUpdateGate(content: @Composable () -> Unit) {
    val context = LocalContext.current
    var update by remember { mutableStateOf<AppUpdateInfo?>(null) }
    var checked by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        update = try {
            withContext(Dispatchers.IO) {
                AppUpdateManager.checkForUpdate(BuildConfig.VERSION_CODE)
            }
        } catch (_: Exception) {
            // If the manifest cannot be reached, keep the installed app usable offline.
            null
        }
        checked = true
    }

    if (!checked) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    } else if (update != null) {
        MandatoryUpdateScreen(context = context, update = update!!)
    } else {
        content()
    }
}
