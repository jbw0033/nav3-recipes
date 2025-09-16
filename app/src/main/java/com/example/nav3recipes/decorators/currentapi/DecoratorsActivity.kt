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

package com.example.nav3recipes.decorators.currentapi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.nav3recipes.content.ContentRed
import com.example.nav3recipes.ui.setEdgeToEdgeConfig
import kotlinx.serialization.Serializable
import kotlin.collections.removeLastOrNull

/**
 * Saving state example:
 *
 * - A1: Tap on the counter button a few times, note the current A1 value
 * - Tap on A2
 * - A2: Tap on the counter button a few times, note the current A2 value
 * - Go back
 * - Note that A1 has retained the previous value
 * - Tap on A2
 * - Note that A2 has *lost* the previous value
 */

@Serializable
private data object A1
@Serializable
private data object A2


class DecoratorsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        setEdgeToEdgeConfig()
        super.onCreate(savedInstanceState)
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
                        entryProvider = { key ->
                            NavEntry(key) {
                                ContentRed(key.javaClass.simpleName) { Counter() }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun Counter() {
    var count by rememberSaveable { mutableIntStateOf(0) }
    Button(onClick = { count++ }) {
        Text("$count")
    }
}
