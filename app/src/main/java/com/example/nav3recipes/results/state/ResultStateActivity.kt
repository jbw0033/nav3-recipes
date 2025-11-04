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

package com.example.nav3recipes.results.state

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.nav3recipes.results.common.Home
import com.example.nav3recipes.results.common.HomeScreen
import com.example.nav3recipes.results.common.Person
import com.example.nav3recipes.results.common.PersonDetailsForm
import com.example.nav3recipes.results.common.PersonDetailsScreen

/**
 * This recipe demonstrates passing an state result to a previous screen. It does this by:
 *
 * - Providing a [ResultStore]
 * - Calling [ResultStore.getResultState] in the receiving screen
 * - Calling [ResultStore.setResult] from the sending screen.
 */

class ResultStateActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val resultStore = rememberResultStore()
            CompositionLocalProvider(LocalResultStore.provides(resultStore)) {
                Scaffold { paddingValues ->
                    val backStack = rememberNavBackStack(Home)

                    NavDisplay(
                        backStack = backStack,
                        modifier = Modifier.padding(paddingValues),
                        onBack = { backStack.removeLastOrNull() },
                        entryProvider = { key ->
                            when (key) {
                                is Home -> NavEntry(key) {
                                    val person = resultStore.getResultState<Person?>()
                                    HomeScreen(backStack, person)
                                }

                                is PersonDetailsForm -> NavEntry(key) {
                                    PersonDetailsScreen(
                                        onSubmit = { person ->
                                            resultStore.setResult<Person>(result = person)
                                            backStack.removeLastOrNull()
                                        }
                                    )
                                }

                                else -> NavEntry(key) { Text("Unknown route") }
                            }
                        }
                    )
                }
            }
        }
    }
}

typealias Name = String
