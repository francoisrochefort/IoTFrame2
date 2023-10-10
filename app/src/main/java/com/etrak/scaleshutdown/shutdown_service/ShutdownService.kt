package com.etrak.scaleshutdown.shutdown_service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import android.os.PowerManager
import androidx.compose.ui.res.stringResource
import androidx.core.app.NotificationCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.collect
import androidx.lifecycle.LifecycleService
import com.etrak.scaleshutdown.R
import kotlinx.coroutines.launch

class ShutdownService : LifecycleService() {

    companion object {

        const val SHUTDOWN_SEQUENCE_STARTED = "com.example.SHUTDOWN_SEQUENCE_STARTED"
        const val SHUTDOWN_SEQUENCE_COUNTDOWN = "com.example.SHUTDOWN_SEQUENCE_COUNTDOWN"
        const val SHUTDOWN_SEQUENCE_CANCELED = "com.example.SHUTDOWN_SEQUENCE_CANCELED"
        const val CANCEL_SHUTDOWN_SEQUENCE = "com.example.CANCEL_SHUTDOWN_SEQUENCE"
        const val EXTRA_COUNTDOWN = "com.example.EXTRA_COUNTDOWN"
        const val SHUTDOWN_DURATION = 10
        const val CHANNEL_ID = "running_channel"

    }

    enum class Action { Start, Stop }

    private val timer = Timer(SHUTDOWN_DURATION)
    private lateinit var gpio: Gpio

    // Monitor the accessory power gpio
    private val callback = object : Gpio.GpioCallback {
        override fun onValueChanged(value: Gpio.State) {
            if (value == Gpio.State.Low) {
                if (timer.state.value == Timer.State.Stopped) timer.start()
            }
            else if(value == Gpio.State.High) {
                if (timer.state.value == Timer.State.Started) timer.stop()
            }
        }
    }

    // Stop the countdown if the user has canceled the shutdown sequence
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == CANCEL_SHUTDOWN_SEQUENCE)
                timer.stop()
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return null
    }

    private val contentTitle: String
        get() = getString(R.string.content_title)
    private val contentText: String
        get() = if (timer.state.value == Timer.State.Stopped)
            getString(R.string.monitoring_accessory_power)
        else
            getString(R.string.shutdown_sequence_started)

    private fun onStart() {

        gpio = Gpio(number = "24")
        gpio.mode = Gpio.Mode.Input
        gpio.registerCallback(callback)

        lifecycleScope.launch {
            timer.state.collect { state ->
                when (state) {
                    Timer.State.TimeUp -> {
                        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
                        powerManager.reboot(null)
                    }
                    Timer.State.Started -> sendBroadcast(Intent(SHUTDOWN_SEQUENCE_STARTED))
                    Timer.State.Stopped -> sendBroadcast(Intent(SHUTDOWN_SEQUENCE_CANCELED))
                }
            }
        }

        lifecycleScope.launch {
            timer.countdown.collect { countdown ->
                sendBroadcast(Intent(SHUTDOWN_SEQUENCE_COUNTDOWN).putExtra(EXTRA_COUNTDOWN, countdown))
            }
        }

        registerReceiver(receiver, IntentFilter(CANCEL_SHUTDOWN_SEQUENCE))

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.power)
            .setContentTitle(contentTitle)
            .setContentText(contentText)
            .build()
        startForeground(1, notification)
    }

    private fun onStop() {

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {


        when (intent?.action) {
            Action.Start.name -> onStart()
            Action.Stop.name -> onStop()
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        timer.stop()
        gpio.unregisterCallback()
        unregisterReceiver(receiver)
    }
}
