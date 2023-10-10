package com.etrak.scaleshutdown.shutdown_service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

class ShutdownServiceFacade(private val context: Context) {

    sealed class Event {
        object OnShutdownStarted : Event()
        data class OnCountdown(val countdown: Int) : Event()
        object OnShutdownCanceled : Event()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val events by lazy {
        callbackFlow {

            val receiver = object: BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    when (intent.action) {
                        ShutdownService.SHUTDOWN_SEQUENCE_STARTED -> trySend(Event.OnShutdownStarted)
                        ShutdownService.SHUTDOWN_SEQUENCE_COUNTDOWN -> trySend(
                            Event.OnCountdown(
                                intent.getIntExtra(
                                    ShutdownService.EXTRA_COUNTDOWN, 0
                                )
                            )
                        )
                        ShutdownService.SHUTDOWN_SEQUENCE_CANCELED -> trySend(Event.OnShutdownCanceled)
                    }
                }
            }

            context.registerReceiver(
                receiver,
                IntentFilter().apply {
                    addAction(ShutdownService.SHUTDOWN_SEQUENCE_STARTED)
                    addAction(ShutdownService.SHUTDOWN_SEQUENCE_COUNTDOWN)
                    addAction(ShutdownService.SHUTDOWN_SEQUENCE_CANCELED)
                }
            )

            awaitClose {
                context.unregisterReceiver(receiver)
            }
        }
    }

    fun cancelShutdownSequence() {
        val intent = Intent(ShutdownService.CANCEL_SHUTDOWN_SEQUENCE)
        context.sendBroadcast(intent)
    }
}
