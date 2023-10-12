package com.etrak.scaleshutdown

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.core.app.ActivityCompat
import com.etrak.scaleshutdown.ScaleApp.Companion.appModule
import com.etrak.scaleshutdown.shutdown_service.ShutdownSequence
import com.etrak.scaleshutdown.shutdown_service.ShutdownService.Companion.DEFAULT_DURATION
import com.etrak.scaleshutdown.ui.theme.ScaleShutdownTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request permission to post notifications
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.POST_NOTIFICATIONS
                ),
                0
            )
        }

        setContent {
            ScaleShutdownTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    // Observe the shutdown service
                    val service =  appModule.shutdownService
                    val show by service.showCountdownSequence.collectAsState(initial = false)
                    val countdown by service.countdown.collectAsState(initial = DEFAULT_DURATION)

                    // Show the main screen
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Button(
                            onClick = {

                                // Use setting repository to pass the selected duration
                                service.start(DEFAULT_DURATION)
                            }
                        ) {
                            Text(text = stringResource(id = R.string.start))
                        }
                    }

                    // Show the countdown dialog
                    if (show) {
                        ShutdownSequence(
                            onCancelClick = {
                                service.cancelShutdownSequence()
                            },
                            countdown = countdown
                        )
                    }
                }
            }
        }
    }
}
