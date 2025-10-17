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

package com.example.nav3recipes.scenes.listdetail

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.nav3recipes.content.ContentBase
import com.example.nav3recipes.ui.setEdgeToEdgeConfig
import com.example.nav3recipes.ui.theme.colors
import kotlinx.serialization.Serializable

/**
 * This example shows how to create a list-detail layout using the Scenes API.
 *
 * A `ListDetailScene` will render content in two panes if:
 *
 * - the window width is over 600dp
 * - A `Detail` entry is the last item in the back stack
 * - A `List` entry is in the back stack
 *
 * @see `ListDetailScene`
 */
@Serializable
data object ConversationList : NavKey

@Serializable
data class ConversationDetail(
    val id: Int,
    val colorId: Int
) : NavKey

@Serializable data object Profile : NavKey

class ListDetailActivity : ComponentActivity() {

    @OptIn(ExperimentalSharedTransitionApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        setEdgeToEdgeConfig()
        super.onCreate(savedInstanceState)

        setContent {

            Scaffold { paddingValues ->

                val backStack = rememberNavBackStack(ConversationList)
                val listDetailStrategy = rememberListDetailSceneStrategy<NavKey>()

                NavDisplay(
                    backStack = backStack,
                    onBack = { backStack.removeLastOrNull() },
                    sceneStrategy = listDetailStrategy,
                    modifier = Modifier.padding(paddingValues),
                    entryProvider = entryProvider {
                        entry<ConversationList>(
                            metadata = ListDetailScene.listPane()
                        ) {
                            ConversationListScreen(
                                onConversationClicked = { detailRoute ->
                                    backStack.addDetail(detailRoute)
                                }
                            )
                        }
                        entry<ConversationDetail>(
                            metadata = ListDetailScene.detailPane()
                        ) { conversationDetail ->
                            ConversationDetailScreen(
                                conversationDetail = conversationDetail,
                                onProfileClicked = { backStack.add(Profile) }
                            )
                        }
                        entry<Profile> {
                            ProfileScreen()
                        }
                    }
                )
            }
        }
    }
}

private fun NavBackStack<NavKey>.addDetail(detailRoute: ConversationDetail) {

    // Remove any existing detail routes
    removeIf { it is ConversationDetail }

    // Avoid adding the same detail route to the back stack twice.
    if (!contains(detailRoute)) {
        add(detailRoute)
    }
}