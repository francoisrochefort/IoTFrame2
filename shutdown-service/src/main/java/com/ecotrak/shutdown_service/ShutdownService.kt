package com.ecotrak.shutdown_service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.collect
import androidx.lifecycle.LifecycleService
import kotlinx.coroutines.launch

class ShutdownService : LifecycleService() {

    /**********************************************************************************************
     * Constants
     *********************************************************************************************/
    companion object {
        const val DEFAULT_DURATION = 10
        const val SHUTDOWN_SEQUENCE_STARTED = "com.example.SHUTDOWN_SEQUENCE_STARTED"
        const val SHUTDOWN_SEQUENCE_COUNTDOWN = "com.example.SHUTDOWN_SEQUENCE_COUNTDOWN"
        const val SHUTDOWN_SEQUENCE_CANCELED = "com.example.SHUTDOWN_SEQUENCE_CANCELED"
        const val EXTRA_DURATION = "com.example.EXTRA_DURATION"
        const val EXTRA_COUNTDOWN = "com.example.EXTRA_COUNTDOWN"
        const val CHANNEL_ID = "state_countdown"
        const val NOTIFICATION_ID = 1
    }

    enum class Action {
        Start,
        CancelShutdownSequence,
        Stop
    }

    /**********************************************************************************************
     * Variables
     *********************************************************************************************/
    private lateinit var timer: Timer
    private lateinit var gpio: Gpio

    /**********************************************************************************************
     * Helpers
     *********************************************************************************************/
    private fun getContentText(state: Timer.State, countdown: Int? = null) =
        if (state == Timer.State.Started && countdown != null)
            String.format(getString(R.string.shutting_down_system), countdown)
        else
            getString(R.string.monitoring_accessory_power)
    private fun getContentText() = getContentText(timer.state.value)

    private val builder: NotificationCompat.Builder by lazy {
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.power)
            .setContentTitle(getString(R.string.content_title))
            .setContentText(getContentText())
    }

    private fun updateNotification(state: Timer.State, countdown: Int? = null) {
        builder.setContentText(getContentText(state, countdown))
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, builder.build())
    }
    private fun updateNotification() = updateNotification(timer.state.value)

    /**********************************************************************************************
     * Command handlers
     *********************************************************************************************/
    private fun onStart(duration: Int) {
        // Init. the timer
        timer = Timer(duration)
        // Init. the accessory power gpio
        gpio = Gpio(number = "24")
        gpio.mode = Gpio.Mode.Input
        gpio.registerCallback(
            object : Gpio.GpioCallback {
                override fun onValueChanged(value: Gpio.State) {
                    // If the power is off then start the timer
                    if (value == Gpio.State.Low) timer.start()
                    // If the power goes on then stop the timer
                    else if(value == Gpio.State.High) timer.stop()
                }
            }
        )
        // Collect the state of the timer
        lifecycleScope.launch {
            timer.state.collect { state ->
                when (state) {
                    // If the time is up then shutdown the system
                    Timer.State.TimeUp -> {
                        val runtimeObject = Runtime.getRuntime().exec(arrayOf("su", "-c","svc power shutdown"))
                        runtimeObject.waitFor()
                    }
                    // If the timer has started then broadcast SHUTDOWN_SEQUENCE_STARTED
                    Timer.State.Started -> {
                        sendBroadcast(Intent(SHUTDOWN_SEQUENCE_STARTED))
                        updateNotification()
                    }
                    // If the timer has stopped then broadcast SHUTDOWN_SEQUENCE_CANCELED
                    Timer.State.Stopped -> {
                        sendBroadcast(Intent(SHUTDOWN_SEQUENCE_CANCELED))
                        updateNotification()
                    }
                }
            }
        }
        // Collect the countdown of the timer
        lifecycleScope.launch {
            timer.countdown.collect { countdown ->
                // Broadcast the countdown
                sendBroadcast(Intent(SHUTDOWN_SEQUENCE_COUNTDOWN).putExtra(EXTRA_COUNTDOWN, countdown))
                updateNotification(timer.state.value, countdown)
            }
        }
        // Create the notification channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Running notifications",
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        // Init. the service
        startForeground(1, builder.build())
    }

    private fun onCancelShutdownSequence() {

        // Stop the timer if the user has clicked the cancel button
        timer.stop()
    }

    private fun onStop() {
        timer.stop()
        gpio.unregisterCallback()
    }

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return null
    }

    /**********************************************************************************************
     * Service onStartCommand function
     *********************************************************************************************/
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        when (intent?.action) {
            Action.Start.name -> onStart(intent.getIntExtra(EXTRA_DURATION, DEFAULT_DURATION))
            Action.CancelShutdownSequence.name -> onCancelShutdownSequence()
            Action.Stop.name -> onStop()
        }
        return super.onStartCommand(intent, flags, startId)
    }
}
