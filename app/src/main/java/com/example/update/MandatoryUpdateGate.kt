package com.example.update

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
            // If the manifest cannot be reached, keep the installed app usable.
            null
        }
        checked = true
    }

    if (!checked) {
        androidx.compose.material3.Surface(
            modifier = androidx.compose.ui.Modifier.fillMaxSize(),
            color = androidx.compose.material3.MaterialTheme.colorScheme.background
        ) {
            androidx.compose.foundation.layout.Box(
                modifier = androidx.compose.ui.Modifier.fillMaxSize(),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                androidx.compose.material3.CircularProgressIndicator()
            }
        }
    } else if (update != null) {
        MandatoryUpdateScreen(context = context, update = update!!) { }
    } else {
        content()
    }
}
