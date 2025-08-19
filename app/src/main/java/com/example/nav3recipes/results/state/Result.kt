package com.example.nav3recipes.results.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

@Composable
fun <T> rememberResult(defaultValue: T) =
    remember { mutableStateOf(defaultValue) }