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

package com.example.nav3recipes.results.event

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.nav3recipes.content.ContentBlue
import com.example.nav3recipes.content.ContentGreen
import kotlinx.serialization.Serializable

/**
 * This recipe demonstrates passing an event result to a previous screen. It does this by:
 *
 * - Providing a [ResultEventBus]
 * - Implementing a [ResultEffect] in the receiving screen
 * - Calling [ResultEventBus.sendResult] from the sending screen.
 */


@Serializable
data object Home : NavKey

@Serializable
class PersonDetailsForm : NavKey

data class Person(val name: String, val favoriteColor: String)

class ResultEventActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val resultBus = remember { ResultEventBus() }
            CompositionLocalProvider(LocalResultEventBus.provides(resultBus)) {
                Scaffold { paddingValues ->

                    val backStack = rememberNavBackStack(Home)

                    NavDisplay(
                        backStack = backStack,
                        modifier = Modifier.padding(paddingValues),
                        onBack = { backStack.removeLastOrNull() },
                        entryProvider = { key ->
                            when (key) {
                                is Home -> NavEntry(key) {
                                    val viewModel = viewModel<HomeViewModel>(key = Home.toString())
                                    ResultEffect<Person> { person ->
                                        viewModel.person = person
                                    }

                                    val person = viewModel.person

                                    ContentBlue("Hello ${person?.name ?: "unknown person"}"){

                                        if (person != null){
                                            Text("Your favorite color is ${person.favoriteColor}\n")
                                        }

                                        Button(onClick = {
                                            backStack.add(PersonDetailsForm())
                                        }) {
                                            Text("Tell us about yourself")
                                        }
                                    }
                                }

                                is PersonDetailsForm -> NavEntry(key) {
                                    ContentGreen("About you"){

                                        val nameTextState = rememberTextFieldState()
                                        OutlinedTextField(
                                            state = nameTextState,
                                            label = { Text("Please enter your name") }
                                        )

                                        val favoriteColorTextState = rememberTextFieldState()
                                        OutlinedTextField(
                                            state = favoriteColorTextState,
                                            label = { Text("Please enter your favorite color") }
                                        )

                                        Button(onClick = {
                                            val person = Person(
                                                name = nameTextState.text.toString(),
                                                favoriteColor = favoriteColorTextState.text.toString()
                                            )

                                            resultBus.sendResult<Person>(result = person)
                                            backStack.removeLastOrNull()
                                        }) {
                                            Text("Submit")
                                        }
                                    }
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

class HomeViewModel : ViewModel() {
    var person by mutableStateOf<Person?>(null)
}
