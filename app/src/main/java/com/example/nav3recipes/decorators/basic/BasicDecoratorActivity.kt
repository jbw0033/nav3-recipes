package com.example.nav3recipes.decorators.basic

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberSavedStateNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.example.nav3recipes.content.ContentBlue
import com.example.nav3recipes.content.ContentGreen
import com.example.nav3recipes.ui.setEdgeToEdgeConfig


data object RouteA
data class RouteB(val id: String)

class BasicDecoratorActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        setEdgeToEdgeConfig()
        super.onCreate(savedInstanceState)
        setContent {

            val backStack = remember { mutableStateListOf<Any>(RouteA) }
            val decoratedEntries = rememberDecoratedNavEntries(
                backStack = backStack,
                entryDecorators = listOf(rememberSavedStateNavEntryDecorator()),
                entryProvider = entryProvider {
                    entry<RouteA> {
                        ContentGreen("Welcome to Nav3") {
                            Button(onClick = {
                                backStack.add(RouteB("123"))
                            }) {
                                Text("Click to navigate")
                            }
                        }
                    }
                    entry<RouteB> { key ->
                        ContentBlue("Route id: ${key.id} ")
                    }
                }
            )

            NavDisplay(
                entries = decoratedEntries,
                onBack = { backStack.removeLastOrNull() },
            )
        }
    }
}