package com.example.nav3recipes.decorators.multiplestacks

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.example.nav3recipes.ui.setEdgeToEdgeConfig
import kotlinx.serialization.Serializable

// Define Keys for Top-Level Stacks and Sub-Screens
@Serializable data object HomeKey : NavKey
@Serializable data object FavoritesKey : NavKey
@Serializable data object HomeDetailKey : NavKey
@Serializable data object FavoritesDetailKey : NavKey

class MultipleStacksDecoratorActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        setEdgeToEdgeConfig()
        super.onCreate(savedInstanceState)
        setContent {
            // 1. Create individual back stacks
            val homeBackStack = remember { mutableStateListOf<NavKey>(HomeKey) }
            val favoritesBackStack = remember { mutableStateListOf<NavKey>(FavoritesKey) }

            // Decorator for saving state
            val savedStateDecorator = rememberSaveableStateHolderNavEntryDecorator<NavKey>()

            // Common entryProvider for all screens
            val commonEntryProvider = entryProvider {
                entry<HomeKey> {
                    HomeRootContent(
                        onNavigateToHomeDetail = { homeBackStack.add(HomeDetailKey) }
                    )
                }
                entry<HomeDetailKey> {
                    HomeDetailContent()
                }
                entry<FavoritesKey> {
                    FavoritesRootContent(
                        onNavigateToFavoritesDetail = { favoritesBackStack.add(FavoritesDetailKey) }
                    )
                }
                entry<FavoritesDetailKey> {
                    FavoritesDetailContent()
                }
            }

            // 2. Call rememberDecoratedNavEntries for each back stack
            val homeTabEntries = rememberDecoratedNavEntries(
                backStack = homeBackStack,
                entryDecorators = listOf(savedStateDecorator),
                entryProvider = commonEntryProvider
            )

            val favoritesTabEntries = rememberDecoratedNavEntries(
                backStack = favoritesBackStack,
                entryDecorators = listOf(savedStateDecorator),
                entryProvider = commonEntryProvider
            )

            // 3. Statically concatenate them
            // As per kdoc: val concatenatedEntries = homeTabEntries + favoritesTabEntries
            val concatenatedEntries = homeTabEntries + favoritesTabEntries

            // Simple onBack logic for this static concatenation. In a real app with dynamic tab switching,
            // this would need to know which tab is currently active to pop from its specific stack.
            val onBack: () -> Unit = {
                when {
                    favoritesBackStack.size > 1 -> favoritesBackStack.removeLastOrNull()
                    homeBackStack.size > 1 -> homeBackStack.removeLastOrNull()
                    else -> { /* No more entries, allow system back to finish activity */ }
                }
            }

            NavDisplay(
                entries = concatenatedEntries,
                onBack = onBack,
            )
        }
    }
}

// UI for Home Tab
@Composable
fun HomeRootContent(onNavigateToHomeDetail: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().background(Color.Cyan.copy(alpha = 0.3f)).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center
    ) {
        Text("Home Tab - Root")
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onNavigateToHomeDetail) {
            Text("Go to Home Detail")
        }
    }
}

@Composable
fun HomeDetailContent() {
    Column(
        modifier = Modifier.fillMaxSize().background(Color.Cyan.copy(alpha = 0.6f)).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center
    ) {
        Text("Home Tab - Detail")
    }
}

// UI for Favorites Tab
@Composable
fun FavoritesRootContent(onNavigateToFavoritesDetail: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().background(Color.Magenta.copy(alpha = 0.3f)).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center
    ) {
        Text("Favorites Tab - Root")
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onNavigateToFavoritesDetail) {
            Text("Go to Favorites Detail")
        }
    }
}

@Composable
fun FavoritesDetailContent() {
    Column(
        modifier = Modifier.fillMaxSize().background(Color.Magenta.copy(alpha = 0.6f)).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center
    ) {
        Text("Favorites Tab - Detail")
    }
}
