package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.data.repository.AudioPlayerManager
import com.example.data.repository.QuranAudioDownloadManager
import com.example.data.repository.QuranTextPrefetchWorker
import com.example.data.service.AdhanScheduler
import com.example.ui.NoorMuslimApp
import com.example.update.MandatoryUpdateGate
import com.example.auth.NoorAuthenticationGate

class MainActivity : ComponentActivity() {
    override fun onResume() {
        super.onResume()
        AdhanScheduler.scheduleAll(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AudioPlayerManager.initialize(applicationContext)
        QuranAudioDownloadManager.initialize(applicationContext)
        QuranTextPrefetchWorker.enqueue(applicationContext)
        AdhanScheduler.scheduleAll(applicationContext)
        enableEdgeToEdge()
        setContent {
            MandatoryUpdateGate {
                NoorAuthenticationGate { onManageAccount ->
                    NoorMuslimApp(onManageAccount = onManageAccount)
                }
            }
        }
    }
}
