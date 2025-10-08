package com.example.nav3recipes.decorators.nowinandroid

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberSavedStateNavEntryDecorator
import org.jetbrains.annotations.VisibleForTesting
import javax.inject.Provider
import kotlin.collections.forEach

// TODO refine back behavior - perhaps take a lambda so that each screen / use site can customize back behavior?
// https://github.com/android/nowinandroid/issues/1934
class NiaNavigator(
    private val startKey: NiaNavKey,
    private val entryProviderBuilders: Provider<Set<EntryProviderScope<NiaNavKey>.() -> Unit>>,
) {
    internal var backStackStore: LinkedHashMap<NiaNavKey, SnapshotStateList<NiaNavKey>> =
        linkedMapOf(
            startKey to mutableStateListOf(startKey),
        )

    internal val activeTopLeveLKeys: SnapshotStateList<NiaNavKey> = mutableStateListOf(startKey)

    var currentActiveTopLevelKey: NiaNavKey by mutableStateOf(activeTopLeveLKeys.last())
        private set

    @get:VisibleForTesting
    val currentKey: NiaNavKey
        get() = backStackStore[currentActiveTopLevelKey]!!.last()

    @Composable
    fun getCurrentEntries(): List<NavEntry<NiaNavKey>> = activeTopLeveLKeys.fold(emptyList()) { entries, key ->
        entries + generateRememberedDecoratedNavEntries(backStackStore[key]!!)
    }

    fun navigate(key: NiaNavKey) {
        println("cfok navigate:$key")
        val currentActiveSubStacks = linkedSetOf<NiaNavKey>()
        currentActiveSubStacks.addAll(activeTopLeveLKeys)
        when {
            // top level singleTop -> clear substack
            key == currentActiveTopLevelKey -> {
                backStackStore[key] = mutableStateListOf(key)
                // no change to currentActiveTabs
            }
            // top level non-singleTop
            key.isTopLevel -> {
                // if navigating back to start destination, then only show the starting substack
                if (key == startKey) {
                    currentActiveSubStacks.clear()
                    currentActiveSubStacks.add(key)
                } else {
                    // else either restore an existing substack or initiate new one
                    backStackStore[key] = backStackStore.remove(key) ?: mutableStateListOf(key)
                    // move this top level key to the top of active substacks
                    currentActiveSubStacks.remove(key)
                    currentActiveSubStacks.add(key)
                }
            }
            // not top level - add to current substack
            else -> {
                val currentStack = backStackStore[currentActiveTopLevelKey]!!
                // single top
                if (currentStack.lastOrNull() == key) {
                    currentStack.removeLastOrNull()
                }
                currentStack.add(key)
                // no change to currentActiveTabs
            }
        }
        updateActiveTopLevelKeys(currentActiveSubStacks.toList())
    }

    fun pop() {
        var currentSubstack = backStackStore[currentActiveTopLevelKey]!!
        if (currentSubstack.size == 1) {
            // if current sub-stack only has one key, remove the sub-stack from the map
            backStackStore.remove(currentActiveTopLevelKey)
            updateActiveTopLevelKeys(activeTopLeveLKeys.dropLast(1))
        } else {
            currentSubstack.removeLastOrNull()
        }
    }

    private fun updateActiveTopLevelKeys(activeKeys: List<NiaNavKey>) {
        require(activeKeys.isNotEmpty()) { "List of active top-level keys should not be empty" }
        activeTopLeveLKeys.clear()
        activeTopLeveLKeys.addAll(activeKeys)
        currentActiveTopLevelKey = activeTopLeveLKeys.last()
    }

    internal fun restore(activeKeys: List<NiaNavKey>, map: LinkedHashMap<NiaNavKey, SnapshotStateList<NiaNavKey>>?) {
        map ?: return
        backStackStore.clear()
        map.forEach { entry ->
            backStackStore[entry.key] = entry.value.toMutableStateList()
        }
        updateActiveTopLevelKeys(activeKeys)
    }

    @Composable
    private fun generateRememberedDecoratedNavEntries(
        backStack: SnapshotStateList<NiaNavKey>,
    ): List<NavEntry<NiaNavKey>> {
        require(backStack.isNotEmpty()) { "Cannot create decoratedNavEntries from empty backStack" }
        require(backStack.first().isTopLevel) {
            "decoratedNavEntries should only be created from a top level key"
        }
        return rememberDecoratedNavEntries(
            backStack = backStack,
            entryDecorators = listOf(
                rememberSavedStateNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
            ),
            entryProvider = entryProvider {
                entryProviderBuilders.get().forEach { builder ->
                    builder()
                }
            },
        )
    }
}

interface NiaNavKey {
    val isTopLevel: Boolean
}