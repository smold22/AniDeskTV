package com.anidesk.tv.player

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flow

/** Flow событий: повторные одинаковые значения не доставляются. */
class EventFlow<T> : Flow<T> {

    private val flow = MutableStateFlow<Event<T>?>(null)

    fun observe(): Flow<T> = flow
        .filterNotNull()
        .mapEvent()

    fun set(value: T) {
        flow.value = Event(value)
    }

    fun emit(value: T) {
        set(value)
    }

    override suspend fun collect(collector: FlowCollector<T>) {
        observe().collect(collector)
    }
}

private data class Event<T>(val data: T)

private fun <T> Flow<Event<T>>.mapEvent(): Flow<T> = flow {
    var prev: Any? = Event(Any())
    collect { event ->
        if (event.data != prev) {
            emit(event.data)
            prev = event.data
        }
    }
}