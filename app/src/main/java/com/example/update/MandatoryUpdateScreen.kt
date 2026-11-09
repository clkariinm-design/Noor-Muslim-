package com.example.update

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.os.Build
import kotlinx.coroutines.launch

@Composable
fun MandatoryUpdateScreen(
    context: Context,
    update: AppUpdateInfo
) {
    val scope = rememberCoroutineScope()
    var progress by remember { mutableIntStateOf(0) }
    var downloading by remember { mutableStateOf(false) }
    var downloadedUri by remember { mutableStateOf<Uri?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier.fillMaxSize().padding(28.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Noor Muslim", fontSize = 30.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(24.dp))
            Text("Update Required", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            Text("Noor Muslim version ${update.versionName} is ready. You must update the app before continuing.")
            if (update.releaseNotes.isNotBlank()) {
                Spacer(Modifier.height(12.dp))
                Text(update.releaseNotes, style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(Modifier.height(28.dp))

            if (downloading) {
                LinearProgressIndicator(progress = { progress / 100f }, modifier = Modifier.width(260.dp))
                Spacer(Modifier.height(10.dp))
                Text("Downloading update… $progress%")
            } else if (downloadedUri != null) {
                Text("Download complete. Install the update to continue.")
                Spacer(Modifier.height(12.dp))
                Button(onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
                        !context.packageManager.canRequestPackageInstalls()
                    ) {
                        AppUpdateManager.openInstallPermissionSettings(context)
                    } else {
                        AppUpdateManager.openInstaller(context, downloadedUri!!)
                    }
                }) { Text("Install Update") }
            } else {
                Button(onClick = {
                    scope.launch {
                        downloading = true
                        error = null
                        try {
                            downloadedUri = AppUpdateManager.downloadUpdate(context, update.apkUrl) {
                                progress = it
                            }
                        } catch (exception: Exception) {
                            error = exception.message ?: "Could not download the update."
                        } finally {
                            downloading = false
                        }
                    }
                }) { Text("Download Update") }
            }
            if (error != null) {
                Spacer(Modifier.height(16.dp))
                Text(error!!, color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(8.dp))
                Button(onClick = {
                    scope.launch {
                        downloading = true
                        error = null
                        try {
                            downloadedUri = AppUpdateManager.downloadUpdate(context, update.apkUrl) {
                                progress = it
                            }
                        } catch (exception: Exception) {
                            error = exception.message ?: "Could not download the update."
                        } finally {
                            downloading = false
                        }
                    }
                }) { Text("Try Again") }
            }
            Spacer(Modifier.height(24.dp))
            Text("This update is required to use Noor Muslim.", style = MaterialTheme.typography.bodySmall)
        }
    }
}
