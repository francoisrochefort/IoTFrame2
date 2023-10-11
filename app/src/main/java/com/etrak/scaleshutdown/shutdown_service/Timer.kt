package com.etrak.scaleshutdown.shutdown_service

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*

class Timer(private val duration: Int) {

    enum class State { Stopped, Started, TimeUp }

    private val _state = MutableStateFlow(State.Stopped)
    val state = _state.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val countdown by lazy {
        _state.flatMapLatest { state ->
            when (state) {
                State.Started -> (duration - 1 downTo 0)
                    .asFlow()
                    .onEach { delay(1000) }
                    .onCompletion { cause ->
                        if (cause == null) _state.value = State.TimeUp
                    }
                else -> flowOf(duration)
            }
        }
    }

    fun start() { _state.value = State.Started }
    fun stop() { _state.value = State.Stopped }
}