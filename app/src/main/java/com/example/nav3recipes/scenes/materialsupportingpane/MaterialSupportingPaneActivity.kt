package com.example.nav3recipes.scenes.materialsupportingpane

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

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.navigation.BackNavigationBehavior
import androidx.compose.material3.adaptive.navigation3.SupportingPaneSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberSupportingPaneSceneStrategy
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.nav3recipes.content.ContentBlue
import com.example.nav3recipes.content.ContentGreen
import com.example.nav3recipes.content.ContentRed
import com.example.nav3recipes.ui.setEdgeToEdgeConfig
import kotlinx.serialization.Serializable

/**
 * This example uses the Material ListDetailSceneStrategy to create an adaptive scene. It has three
 * destinations: ConversationList, ConversationDetail and Profile. When the window width allows it,
 * the content for these destinations will be shown in a two pane layout.
 */
@Serializable
private object MainPane : NavKey

@Serializable
private data object SupportingPane : NavKey

@Serializable
private data object Profile : NavKey

class MaterialSupportingPaneActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3AdaptiveApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        setEdgeToEdgeConfig()
        super.onCreate(savedInstanceState)

        setContent {

            val backStack = rememberNavBackStack(MainPane)

            // Override the defaults so that there isn't a horizontal or vertical space between the panes.
            // See b/444438086
            val windowAdaptiveInfo = currentWindowAdaptiveInfo()
            val directive = remember(windowAdaptiveInfo) {
                calculatePaneScaffoldDirective(windowAdaptiveInfo)
                    .copy(horizontalPartitionSpacerSize = 0.dp, verticalPartitionSpacerSize = 0.dp)
            }

            val supportingPaneStrategy = rememberSupportingPaneSceneStrategy<NavKey>(
                backNavigationBehavior = BackNavigationBehavior.PopUntilCurrentDestinationChange,
                directive = directive
            )

            NavDisplay(
                backStack = backStack,
                onBack = { keysToRemove -> repeat(keysToRemove) { backStack.removeLastOrNull() } },
                sceneStrategy = supportingPaneStrategy,
                entryProvider = entryProvider {
                    entry<MainPane>(
                        metadata = SupportingPaneSceneStrategy.mainPane()
                    ) {
                        ContentRed("Welcome to Nav3") {
                            Button(onClick = {
                                backStack.add(SupportingPane)
                            }) {
                                Text("View Supporting Pane")
                            }
                        }
                    }
                    entry<SupportingPane>(
                        metadata = SupportingPaneSceneStrategy.supportingPane()
                    ) {
                        ContentBlue("Supporting Pane") {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Button(onClick = {
                                    backStack.add(Profile)
                                }) {
                                    Text("View profile")
                                }
                            }
                        }
                    }
                    entry<Profile>(
                        metadata = SupportingPaneSceneStrategy.extraPane()
                    ) {
                        ContentGreen("Profile")
                    }
                }
            )
        }
    }
}
