/*
 * Copyright 2025 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.nav3recipes.decorators.newapi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavEntryDecorator
import androidx.navigation3.runtime.navEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.example.nav3recipes.content.ContentRed
import com.example.nav3recipes.ui.setEdgeToEdgeConfig
import kotlinx.serialization.Serializable

/**
 * Saving state example (DecoratedNavEntryProvider API):
 *
 * - A1: Tap on the counter button a few times, note the current A1 value
 * - Tap on A2
 * - A2: Tap on the counter button a few times, note the current A2 value
 * - Go back
 * - Note that A1 has retained the previous value
 * - Tap on A2
 * - Note that A2 has *retained* the previous value
 */

@Serializable
private data object A1

@Serializable
private data object A2

private val LocalCount = compositionLocalOf { 0 }

class RenderCountActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        setEdgeToEdgeConfig()
        super.onCreate(savedInstanceState)


        val renderedEntryCount = mutableIntStateOf(0)

        val entryProvider: (Any) -> NavEntry<Any> = { key ->
            NavEntry(key) {
                val renderedCount = LocalCount.current
                ContentRed(title = "Rendered entries: $renderedCount")
            }
        }

        val decorators: List<NavEntryDecorator<Any>> = listOf(
            myDecorator(renderedEntryCount)
        )

        setContent {

            val backStack = remember { mutableStateListOf<Any>(A1) }

            Scaffold { paddingValues ->
                Column(modifier = Modifier.padding(paddingValues)) {
                    Row {
                        Button(onClick = { backStack.add(A1) }) { Text("A1") }
                        Button(onClick = { backStack.add(A2) }) { Text("A2") }
                    }
                    NavDisplay(
                        backStack = backStack,
                        onBack = { backStack.removeLastOrNull() },
                        entryDecorators = decorators,
                        entryProvider = entryProvider
                    )
                }
            }
        }
    }
}

fun <T : Any> myDecorator(count: MutableState<Int>) =
    navEntryDecorator<T>(onPop = { count.value-- }) { entry ->
        LaunchedEffect(entry.contentKey) { count.value++ }
        CompositionLocalProvider(LocalCount provides count.value) {
            entry.Content()
        }
    }