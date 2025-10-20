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

package com.example.nav3recipes.scenes.nonav3

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import com.example.nav3recipes.content.ContentBlue
import com.example.nav3recipes.ui.setEdgeToEdgeConfig

data object ConversationList

data class ConversationDetail(val id: Int, val colorId: Int)

class BasicActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        setEdgeToEdgeConfig()
        super.onCreate(savedInstanceState)
        setContent {
            val backStack = remember { mutableStateListOf<Any>(ConversationList) }

            Row {
                Column(modifier = Modifier.weight(0.4f)) {
                    ConversationListScreen(onConversationClicked = { backStack.addDetail(it)})
                }

                val lastEntry = backStack.last()
                if (lastEntry is ConversationDetail) {
                    Column(modifier = Modifier.weight(0.6f)) {
                        AnimatedContent(targetState = lastEntry){ route ->
                            ConversationDetailScreen(route, onProfileClicked = {})
                        }
                    }

                }
            }


            /*Box {
                val route = backStack.last()
                when (route) {
                    is ConversationList -> ConversationListScreen(
                        onConversationClicked = { detailRoute ->
                            backStack.addDetail(detailRoute)
                        }
                    )
                    is ConversationDetail -> ContentBlue("Route id: ${route.id} ")
                    else -> {
                        error("Unknown route: $route")
                    }
                }
            }*/
        }
    }
}

@Composable
fun ListDetailLayout(
    listContent: @Composable () -> Unit,
    detailContent: @Composable () -> Unit,
) {
    Row {
        Column(modifier = Modifier.weight(0.4f)) {
            listContent()
        }
        Column(modifier = Modifier.weight(0.6f)) {
            detailContent()
        }
    }
}

fun SnapshotStateList<Any>.addDetail(detailRoute: ConversationDetail) {

    // Avoid adding the same detail route to the back stack twice.
    if (!contains(detailRoute)) {
        add(detailRoute)
    }



}