package com.example.nav3recipes.decorators.newcounter

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

import androidx.compose.runtime.compositionLocalOf
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavEntryDecorator
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

private val LocalCounterState = compositionLocalOf { CounterState() }

class CounterPerEntryActivity : ComponentActivity() {

    val counterStateMap = mutableStateMapOf<Any, CounterState>()

    override fun onCreate(savedInstanceState: Bundle?) {
        setEdgeToEdgeConfig()
        super.onCreate(savedInstanceState)
        setContent {

            val backStack = remember { mutableStateListOf<Any>(A1) }

            val entryProvider: (Any) -> NavEntry<Any> = { key ->
                NavEntry(key) {
                    ContentRed(key.javaClass.simpleName) { CounterUI() }
                }
            }

            val decorators = listOf(
                counterStateDecorator<Any>(counterStateMap),
                //rememberSavedStateNavEntryDecorator()
            )

            Scaffold { paddingValues ->
                Column(modifier = Modifier.padding(paddingValues)) {
                    Row {
                        Column {
                            Row {
                                val counterStateA1 = counterStateMap[A1.toString()]
                                Text("A1")
                                Button(onClick = { backStack.add(A1) }) { Text("Go") }
                                Text("Count value: ${counterStateA1?.value}")
                                Button(onClick = { counterStateA1?.value = 0 }) { Text("Reset") }
                                Checkbox(
                                    checked = counterStateA1?.shouldClearOnPop ?: false,
                                    onCheckedChange = { isChecked ->
                                        counterStateA1?.shouldClearOnPop = isChecked
                                    }
                                )
                            }
                            Row {
                                val counterStateA2 = counterStateMap[A2.toString()]
                                Text("A2")
                                Button(onClick = { backStack.add(A2) }) { Text("Go") }
                                Text("Count value: ${counterStateA2?.value}")
                                Button(onClick = { counterStateA2?.value = 0 } ) { Text("Reset") }
                                Checkbox(
                                    checked = counterStateA2?.shouldClearOnPop ?: false,
                                    onCheckedChange = { isChecked ->
                                        counterStateA2?.shouldClearOnPop = isChecked
                                    }
                                )
                            }
                        }
                    }
                    Row {
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
}


@Composable
fun <T : Any> counterStateDecorator(counterStates: MutableMap<Any, CounterState>): NavEntryDecorator<T> =
    NavEntryDecorator(
        onPop = { contentKey ->
            val countState = counterStates[contentKey]
            if (countState?.shouldClearOnPop == true){
                counterStates.remove(contentKey)
            }
        },
        decorate = { entry ->
            val countState = counterStates.getOrPut(entry.contentKey) { CounterState() }
            CompositionLocalProvider(LocalCounterState provides countState) {
                entry.Content()
            }
        }
    )



@Composable
fun CounterUI() {
    val state = LocalCounterState.current
    Button(onClick = { state.value++ }) {
        Text("${state.value}")
    }
}

data class CounterState(
    private val initialValue: Int = 0,
    private val initialShouldClearOnPop: Boolean = true
){
    var value: Int by mutableIntStateOf(initialValue)
    var shouldClearOnPop: Boolean by mutableStateOf(initialShouldClearOnPop)
}