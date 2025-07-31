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

package com.example.nav3recipes.migration.start

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.dialog
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import androidx.navigation.toRoute
import com.example.nav3recipes.content.ContentBlue
import com.example.nav3recipes.content.ContentGreen
import com.example.nav3recipes.content.ContentMauve
import com.example.nav3recipes.content.ContentPink
import com.example.nav3recipes.content.ContentPurple
import com.example.nav3recipes.content.ContentRed
import com.example.nav3recipes.navigator.basic.Route
import com.example.nav3recipes.ui.setEdgeToEdgeConfig
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass

/**
 * Basic Navigation2 example with the following navigation graph:
 *
 * A -> A, A1, E
 * B -> B, B1, E
 * C -> C, E
 * D
 *
 * - The starting destination (or home screen) is A.
 * - A, B and C are top level destinations that appear in a navigation bar.
 * - D is a dialog destination.
 * - E is a shared destination that can appear under any of the top level destinations.
 * - Navigating to a top level destination pops all other top level destinations off the stack,
 * except for the start destination.
 * - Navigating back from the start destination exits the app.
 *
 * This will be the starting point for migration to Navigation 3.
 */

@Serializable private data object BaseRouteA : Route(isTopLevel = true)
@Serializable private data object RouteA : Route()
@Serializable private data object RouteA1 : Route()

@Serializable private data object BaseRouteB : Route(isTopLevel = true)
@Serializable private data object RouteB : Route()
@Serializable private data class RouteB1(val id: String)

@Serializable private data object BaseRouteC : Route(isTopLevel = true)
@Serializable private data object RouteC : Route()
@Serializable private data object RouteD : Route()
@Serializable private data object RouteE : Route()

private val TOP_LEVEL_ROUTES = mapOf(
    BaseRouteA to NavBarItem(icon = Icons.Default.Home, description = "Route A"),
    BaseRouteB to NavBarItem(icon = Icons.Default.Face, description = "Route B"),
    BaseRouteC to NavBarItem(icon = Icons.Default.Camera, description = "Route C"),
)

class NavBarItem(
    val icon: ImageVector,
    val description: String
)

class MigrationActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        setEdgeToEdgeConfig()
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            val currentBackStackEntry by navController.currentBackStackEntryAsState()

            Scaffold(bottomBar = {
                NavigationBar {
                    TOP_LEVEL_ROUTES.forEach { (key, value) ->
                        val isSelected = currentBackStackEntry?.destination.isRouteInHierarchy(key::class)
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                navController.navigate(key, navOptions {
                                    popUpTo(route = RouteA)
                                })
                            },
                            icon = {
                                Icon(
                                    imageVector = value.icon,
                                    contentDescription = value.description
                                )
                            },
                            label = { Text(value.description) }
                        )
                    }
                }
            })

            { paddingValues ->
                NavHost(
                    navController = navController,
                    startDestination = BaseRouteA,
                    modifier = Modifier.padding(paddingValues)
                ) {
                    featureASection(navController)
                    featureBSection(navController)
                    featureCSection(navController)
                    dialog<RouteD> { key ->
                        Text(modifier = Modifier.background(Color.White), text = "Route D title (dialog)")
                    }
                }
            }
        }
    }
}

private fun NavGraphBuilder.featureCSection(navController: NavHostController) {
    navigation<BaseRouteC>(startDestination = RouteC) {
        composable<RouteC> {
            ContentMauve("Route C title") {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Button(onClick = { navController.navigate(route = RouteD) }) {
                        Text("Open dialog D")
                    }
                    Button(onClick = { navController.navigate(route = RouteE) }) {
                        Text("Go to E")
                    }
                }
            }
        }
        composable<RouteE> { ContentBlue("Route E title") }
    }
}

private fun NavGraphBuilder.featureBSection(navController: NavHostController) {
    navigation<BaseRouteB>(startDestination = RouteB) {
        composable<RouteB> {
            ContentGreen("Route B title") {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Button(onClick = { navController.navigate(route = RouteB1(id = "ABC")) }) {
                        Text("Go to B1")
                    }
                    Button(onClick = { navController.navigate(route = RouteD) }) {
                        Text("Open dialog D")
                    }
                    Button(onClick = { navController.navigate(route = RouteE) }) {
                        Text("Go to E")
                    }
                }
            }
        }
        composable<RouteB1> { key ->
            ContentPurple("Route B1 title. ID: ${key.toRoute<RouteB1>().id}")
        }
        composable<RouteE> { ContentBlue("Route E title") }
    }
}

private fun NavGraphBuilder.featureASection(navController: NavHostController) {
    navigation<BaseRouteA>(startDestination = RouteA) {
        composable<RouteA> {
            ContentRed("Route A title") {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Button(onClick = { navController.navigate(route = RouteA1) }) {
                        Text("Go to A1")
                    }
                    Button(onClick = { navController.navigate(route = RouteD) }) {
                        Text("Open dialog D")
                    }
                    Button(onClick = { navController.navigate(route = RouteE) }) {
                        Text("Go to E")
                    }
                }
            }
        }
        composable<RouteA1> { ContentPink("Route A1 title") }
        composable<RouteE> { ContentBlue("Route E title") }
    }
}

private fun NavDestination?.isRouteInHierarchy(route: KClass<*>) =
    this?.hierarchy?.any {
        it.hasRoute(route)
    } ?: false