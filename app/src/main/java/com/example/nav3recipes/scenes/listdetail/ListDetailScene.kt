package com.example.nav3recipes.scenes.listdetail

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SceneStrategyScope
import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_MEDIUM_LOWER_BOUND
import com.example.nav3recipes.scenes.listdetail.ListDetailScene.Companion.DETAIL_KEY
import com.example.nav3recipes.scenes.listdetail.ListDetailScene.Companion.LIST_KEY

/**
 * A [Scene] that displays a list and a detail [NavEntry] side-by-side in a 40/60 split.
 *
 */
class ListDetailScene<T : Any>(
    override val key: Any,
    override val previousEntries: List<NavEntry<T>>,
    val listEntry: NavEntry<T>,
    val detailEntry: NavEntry<T>,
) : Scene<T> {
    override val entries: List<NavEntry<T>> = listOf(listEntry, detailEntry)
    override val content: @Composable (() -> Unit) = {
        Row(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.weight(0.4f)) {
                listEntry.Content()
            }
            Column(modifier = Modifier.weight(0.6f)) {
                AnimatedContent(
                    targetState = detailEntry,
                    transitionSpec = {
                        // Slide new content up, keeping the old content in place underneath
                        slideInVertically(
                            initialOffsetY = { it },
                            animationSpec = tween(1000)
                        ) togetherWith ExitTransition.KeepUntilTransitionsFinished
                    }
                ){ entry ->
                    entry.Content()
                }
            }
        }
    }

    companion object {
        internal const val LIST_KEY = "ListDetailScene-List"
        internal const val DETAIL_KEY = "ListDetailScene-Detail"

        /**
         * Helper function to add metadata to a [NavEntry] indicating it can be displayed
         * as a list in the [ListDetailScene].
         */
        fun listPane() = mapOf(LIST_KEY to true)

        /**
         * Helper function to add metadata to a [NavEntry] indicating it can be displayed
         * as a list in the [ListDetailScene].
         */
        fun detailPane() = mapOf(DETAIL_KEY to true)
    }
}

@Composable
fun <T : Any> rememberListDetailSceneStrategy(): ListDetailSceneStrategy<T> {
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass

    return remember(windowSizeClass) {
        ListDetailSceneStrategy(windowSizeClass)
    }
}


/**
 * A [SceneStrategy] that returns a [ListDetailScene] if the window is wide enough
 * and the last two back stack entries are list and detail.
 */
class ListDetailSceneStrategy<T : Any>(val windowSizeClass: WindowSizeClass) : SceneStrategy<T> {

    override fun SceneStrategyScope<T>.calculateScene(entries: List<NavEntry<T>>): Scene<T>? {

        if (!windowSizeClass.isWidthAtLeastBreakpoint(WIDTH_DP_MEDIUM_LOWER_BOUND)) {
            return null
        }

        val detailEntry =
            entries.lastOrNull()?.takeIf { it.metadata.containsKey(DETAIL_KEY) } ?: return null
        val listEntry = entries.findLast { it.metadata.containsKey(LIST_KEY) } ?: return null

        // We use the list's contentKey to uniquely identify the scene.
        // This allows the detail panes to be animated in and out by the scene, rather than
        // having NavDisplay animate the whole scene out when the selected detail item changes.
        val sceneKey = listEntry.contentKey

        return ListDetailScene(
            key = sceneKey,
            previousEntries = entries.dropLast(1),
            listEntry = listEntry,
            detailEntry = detailEntry
        )
    }
}

