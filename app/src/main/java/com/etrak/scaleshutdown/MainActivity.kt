package com.etrak.scaleshutdown

import android.Manifest
import android.content.Intent
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.core.app.ActivityCompat
import com.etrak.scaleshutdown.ScaleApp.Companion.appModule
import com.etrak.scaleshutdown.shutdown_service.ShutdownServiceFacade
import com.etrak.scaleshutdown.shutdown_service.ShutdownSequence
import com.etrak.scaleshutdown.shutdown_service.ShutdownService
import com.etrak.scaleshutdown.ui.theme.ScaleShutdownTheme
import kotlinx.coroutines.flow.collect

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
                    // Collect shutdown service events to show, update or hide the countdown dialog
                    var shutdownSequenceStarted by rememberSaveable { mutableStateOf(false) }
                    var countdown by rememberSaveable { mutableStateOf(10) }
                    LaunchedEffect(key1 = true) {
                        appModule.shutdownService.events.collect { event ->
                            when (event) {
                                is ShutdownServiceFacade.Event.OnShutdownStarted -> shutdownSequenceStarted = true
                                is ShutdownServiceFacade.Event.OnCountdown -> countdown = event.countdown
                                is ShutdownServiceFacade.Event.OnShutdownCanceled -> shutdownSequenceStarted = false
                            }
                        }
                    }

                    // Show the main screen
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Button(
                            onClick = {
                                Intent(applicationContext, ShutdownService::class.java).also {
                                    it.action = ShutdownService.Action.Start.name
                                    startService(it)
                                }
                            }
                        ) {
                            Text(text = stringResource(id = R.string.start))
                        }
                    }

                    // Show the countdown dialog
                    if (shutdownSequenceStarted) {
                        val service =  appModule.shutdownService
                        ShutdownSequence(
                            onCancelClick = service::cancelShutdownSequence,
                            countdown = countdown
                        )
                    }
                }
            }
        }
    }
}
