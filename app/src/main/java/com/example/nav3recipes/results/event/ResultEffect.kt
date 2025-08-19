package com.example.nav3recipes.results.event

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

@Composable
inline fun <reified T> ResultEffect(
    resultStore: ResultStore = LocalResultStore.current,
    resultKey: String = T::class.toString(),
    crossinline onResult: suspend (T) -> Unit
) {
    LaunchedEffect(resultKey, resultStore.channelMap[resultKey]) {
        resultStore.getResultFlow<T>()?.collect { result ->
            onResult.invoke(result as T)
        }
    }
}