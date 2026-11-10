package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

/**
 * Intentionally empty launcher screen.
 *
 * This reset removes the former app features and leaves a clean Android shell.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // No UI or application features are included in this reset.
        }
    }
}
