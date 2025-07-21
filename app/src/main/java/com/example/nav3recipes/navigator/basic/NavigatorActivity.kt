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

package com.example.nav3recipes.navigator.basic

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.entry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.example.nav3recipes.content.ContentBlue
import com.example.nav3recipes.content.ContentGreen
import com.example.nav3recipes.content.ContentPurple
import com.example.nav3recipes.content.ContentRed
import com.example.nav3recipes.ui.setEdgeToEdgeConfig

import kotlin.collections.remove

private sealed interface NavBarItem : Navigator.Route.TopLevel {
    val icon: ImageVector
}
private data object Home : NavBarItem { override val icon = Icons.Default.Home }
private data object ChatList : NavBarItem { override val icon = Icons.Default.Face }
private data object ChatDetail
private data object Camera : NavBarItem { override val icon = Icons.Default.PlayArrow }

private val TOP_LEVEL_ROUTES : List<NavBarItem> = listOf(Home, ChatList, Camera)

class NavigatorActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        setEdgeToEdgeConfig()
        super.onCreate(savedInstanceState)
        setContent {
            val navigator = remember { Navigator<Any>(Home) }

            Scaffold(
                bottomBar = {
                    NavigationBar {
                        TOP_LEVEL_ROUTES.forEach { topLevelRoute ->

                            val isSelected = topLevelRoute == navigator.topLevelRoute
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = {
                                    navigator.navigate(topLevelRoute)
                                },
                                icon = {
                                    Icon(
                                        imageVector = topLevelRoute.icon,
                                        contentDescription = null
                                    )
                                }
                            )
                        }
                    }
                }
            ) { _ ->
                NavDisplay(
                    backStack = navigator.backStack,
                    onBack = { navigator.goBack() },
                    entryProvider = entryProvider {
                        entry<Home>{
                            ContentRed("Home screen")
                        }
                        entry<ChatList>{
                            ContentGreen("Chat list screen"){
                                Button(onClick = { navigator.navigate(ChatDetail) }) {
                                    Text("Go to conversation")
                                }
                            }
                        }
                        entry<ChatDetail>{
                            ContentBlue("Chat detail screen")
                        }
                        entry<Camera>{
                            ContentPurple("Camera screen")
                        }
                    },
                )
            }
        }
    }
}

class Navigator<T: Any>(
    startRoute: T,
    private val canStartRouteMove: Boolean = false,
    private val shouldPopOtherTopLevelRoutesWhenNavigatingToTopLevelRoute: Boolean = true,
    private val shouldRemoveChildRoutesWhenNavigatingBack: Boolean = false
) {

    // Maintain a stack for each top level route
    private var topLevelStacks : LinkedHashMap<T, SnapshotStateList<T>> = linkedMapOf(
        startRoute to mutableStateListOf(startRoute)
    )

    // Expose the current top level route for consumers
    var topLevelRoute by mutableStateOf(startRoute)
        private set

    // Expose the back stack so it can be rendered by the NavDisplay
    val backStack = mutableStateListOf(startRoute)

    private fun updateBackStack() =
        backStack.apply {
            clear()
            addAll(topLevelStacks.flatMap { it.value })
        }

    private fun navigateToTopLevel(key: T){

        // Remove any other top level stacks first
        if (shouldPopOtherTopLevelRoutesWhenNavigatingToTopLevelRoute)
            topLevelStacks.keys.filter { it != key }.forEach { topLevelStacks.remove(it) }

        val doesStackExist = topLevelStacks.keys.contains(key)

        if (doesStackExist){
            // Move it to the end of the stacks
            topLevelStacks.apply {
                remove(key)?.let {
                    put(key, it)
                }
            }
        } else {
            topLevelStacks.put(key, mutableStateListOf(key))
        }
        topLevelRoute = key
        updateBackStack()
    }

    fun navigate(key: T){
        if (key is Route.TopLevel){
            navigateToTopLevel(key)
        } else {
            topLevelStacks[topLevelRoute]?.add(key)
        }
        updateBackStack()
    }

    fun goBack(){
        val removedKey = topLevelStacks[topLevelRoute]?.removeLastOrNull()
        // If the removed key was a top level key, remove the associated top level stack
        topLevelStacks.remove(removedKey)
        topLevelRoute = topLevelStacks.keys.last()
        updateBackStack()
    }

    interface Route {
        interface TopLevel
        interface Unique // Non-top level route that is unique on the back stack (can move between top level stacks)
    }
}