package com.example.nav3recipes.migration.step3

import android.annotation.SuppressLint
import androidx.compose.runtime.mutableStateListOf
import androidx.navigation.NavHostController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * Navigator that mirrors `NavController`'s back stack
 */
@SuppressLint("RestrictedApi")
class Navigator(
    private val navController: NavHostController
) {

    val coroutineScope = CoroutineScope(Job())

    init {
        coroutineScope.launch {
            navController.currentBackStack.collect { nav2BackStack ->
                if (nav2BackStack.isNotEmpty()){
                    backStack.clear()
                    val entriesToAdd = nav2BackStack.mapNotNull { entry ->
                        // We only care about navigable destinations
                        val navigatorName = entry.destination.navigatorName
                        if (navigatorName == "composable" || navigatorName == "dialog"){
                            entry
                        } else {
                            null
                        }
                    }
                    backStack.addAll(entriesToAdd)
                }
            }
        }
    }

    val backStack = mutableStateListOf<Any>(Unit)
}