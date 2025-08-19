package com.example.nav3recipes.results.event

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.compositionLocalOf
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.BUFFERED
import kotlinx.coroutines.flow.receiveAsFlow


object LocalResultStore {
    private val LocalResultStore: ProvidableCompositionLocal<ResultStore?>
        get() {
            return compositionLocalOf { null }
        }

    val current: ResultStore
        @Composable
        get() = LocalResultStore.current ?: ResultStore()

    infix fun provides(
        store: ResultStore
    ): ProvidedValue<ResultStore?> {
        return LocalResultStore.provides(store)
    }
}

class ResultStore {
    val channelMap: MutableMap<String, Channel<Any?>> = mutableMapOf()

    inline fun <reified T> getResultFlow(resultKey: String = T::class.toString()) =
        channelMap[resultKey]?.receiveAsFlow()

    inline fun <reified T> sendResult(resultKey: String = T::class.toString(), result: T) {
        if (!channelMap.contains(resultKey)) {
            channelMap.put(resultKey, Channel(capacity = BUFFERED, onBufferOverflow = BufferOverflow.SUSPEND))
        }
        channelMap[resultKey]?.trySend(result)
    }
}