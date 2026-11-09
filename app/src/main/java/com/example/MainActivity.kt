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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AudioPlayerManager.initialize(applicationContext)
        QuranAudioDownloadManager.initialize(applicationContext)
        QuranTextPrefetchWorker.enqueue(applicationContext)
        // Restore the next five prayer alarms whenever the app is opened.
        AdhanScheduler.scheduleAll(applicationContext)
        enableEdgeToEdge()
        setContent {
            NoorMuslimApp()
        }
    }
}
