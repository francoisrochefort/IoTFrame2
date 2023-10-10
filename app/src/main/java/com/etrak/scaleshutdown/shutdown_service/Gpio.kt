package com.etrak.scaleshutdown.shutdown_service

import java.io.DataOutputStream
import java.io.File
import java.util.*

class Gpio(private val number: String) {

    enum class State { Low, High }
    enum class Mode { Input, Output }

    interface GpioCallback {
        fun onValueChanged(value: State)
    }

    private val timer = Timer()
    private var callback: GpioCallback? = null
    private lateinit var _value: State

    private fun runAsRoot(cmd: String) {
        val process = Runtime.getRuntime().exec("su")
        val dos = DataOutputStream(process.outputStream)
        dos.write(cmd.toByteArray())
        dos.writeBytes("exit\n")
        dos.flush()
        dos.close()
        process.waitFor()
    }

    val value: State
        get() = if (File("/sys/class/gpio/gpio$number/value").readText().trim() == "1")
            State.High
        else
            State.Low

    var mode: Mode
        get() {
            return if (File("/sys/class/gpio/gpio$number/direction").readText().trim() == "in")
                Mode.Input
            else
                Mode.Output
        }
        set(value) {
            val cmd = "echo ${ if (value == Mode.Output) "out" else "in" } > /sys/class/gpio/gpio$number/direction\n"
            runAsRoot(cmd)
        }

    fun registerCallback(callback: GpioCallback) {
        this.callback = callback
        timer.scheduleAtFixedRate(
            object : TimerTask() {
                override fun run() {

                    // Read the current state of the gpio
                    val state = value

                    // If the lateinit variable has not been initialized then do it
                    if (!::_value.isInitialized)
                        _value = state

                    // Notifier the observer that the state has changed
                    if (_value != state) {
                        callback.onValueChanged(state)
                        _value = state
                    }
                }
            }, 0, 250
        )
    }

    fun unregisterCallback() {
        timer.cancel()
        this.callback = null
    }
}
