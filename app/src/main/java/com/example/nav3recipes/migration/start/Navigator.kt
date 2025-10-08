package com.example.nav3recipes.migration.start

import android.annotation.SuppressLint
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.toRoute
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Navigator that mirrors `NavController`'s back stack
 *
 * @param navController - The NavController whose back stack to mirror
 * @param coroutineScope - Scope used to collect changes from the NavController
 * @param entryToRouteMapper - Lambda that converts an entry in NavController's back stack
 * into a route that is stored in the `Navigator`'s back stack.
 *
 * For example: { entry -> entry.toRouteOrNull<RouteA>() ?: entry.toRouteOrNull<RouteB>() }
 *
 * @param shouldPrintDebugInfo - Prints debug info to logcat
 */
@SuppressLint("RestrictedApi")
class Navigator(
    private val navController: NavHostController,
    coroutineScope: CoroutineScope,
    entryToRouteMapper: (NavBackStackEntry) -> Any? = { null },
    private val shouldPrintDebugInfo: Boolean = false
) {

    private val startRoute = Unit

    val backStack = mutableStateListOf<Any>(startRoute)
    var topLevelRoute by mutableStateOf<Any>(startRoute)
        private set

    // Maintain a stack for each top level route
    private lateinit var topLevelStacks : MutableMap<Any, MutableList<Any>>

    // Maintain a map of shared routes to their parent stacks
    private var sharedRoutes: MutableMap<Any, Any> = mutableMapOf()

    init {
        inititalizeTopLevelStacks()
        coroutineScope.launch {
            navController.currentBackStack.collect { nav2BackStack ->
                inititalizeTopLevelStacks()
                navlog("Top level stacks reset, parsing Nav2 back stack $nav2BackStack")
                printTopLevelStacks()

                nav2BackStack.forEach { entry ->
                    val destination = entry.destination

                    if (destination.navigatorName == "composable" || destination.navigatorName == "dialog"){
                        val route = entryToRouteMapper(entry) ?: entry
                        add(route)
                    } else {
                        navlog("Ignoring $entry")
                    }
                }
                printTopLevelStacks()
                updateBackStack()
            }
        }
    }

    private fun updateBackStack() {
        backStack.apply {
            clear()
            val entries = topLevelStacks.flatMap { it.value }
            addAll(entries)
        }
        printBackStack()
    }
    
    fun navlog(message: String){
        if (shouldPrintDebugInfo){
            println(message)
        }
    }

    private fun printBackStack() {
        navlog("Back stack: ${backStack.getDebugString()}")
    }

    private fun printTopLevelStacks() {

        navlog("Top level stacks: ")
        topLevelStacks.forEach { topLevelStack ->
            navlog("  ${topLevelStack.key} => ${topLevelStack.value.getDebugString()}")
        }
    }

    private fun List<Any>.getDebugString() : String {
        val message = StringBuilder("[")
        forEach { entry ->
            if (entry is NavBackStackEntry){
                message.append("Unmigrated route: ${entry.destination.route}, ")
            } else {
                message.append("Migrated route: $entry, ")
            }
        }
        message.append("]\n")
        return message.toString()
    }

    private fun addTopLevel(route: Any) {
        if (route == startRoute) {
            clearAllExceptStartStack()
        } else {

            // Get the existing stack or create a new one.
            val topLevelStack = topLevelStacks.remove(route) ?: mutableListOf(route)
            clearAllExceptStartStack()

            topLevelStacks.put(route, topLevelStack)
            navlog("Added top level route $route")
        }
        topLevelRoute = route
    }

    private fun clearAllExceptStartStack() {
        // Remove all other top level stacks, except the start stack
        val startStack = topLevelStacks[startRoute] ?: mutableListOf<Any>(startRoute)
        topLevelStacks.clear()
        topLevelStacks.put(startRoute, startStack)
    }

    private fun inititalizeTopLevelStacks() {
        topLevelStacks = mutableMapOf(startRoute to mutableListOf(startRoute))
        topLevelRoute = startRoute
    }

    private fun add(route: Any) {
        navlog("Attempting to add $route")
        if (route is Route.TopLevel) {
            navlog("$route is a top level route")
            addTopLevel(route)
        } else {
            if (route is Route.Shared) {
                navlog("$route is a shared route")
                // If the key is already in a stack, remove it
                val oldParent = sharedRoutes[route]
                if (oldParent != null) {
                    topLevelStacks[oldParent]?.remove(route)
                }
                sharedRoutes[route] = topLevelRoute
            } else {
                navlog("$route is a normal route")
            }
            val hasBeenAdded = topLevelStacks[topLevelRoute]?.add(route) ?: false
            navlog("Added $route to $topLevelRoute stack: $hasBeenAdded")
        }
    }

    /**
     * Navigate to the given route.
     */
    fun navigate(route: Any, navOptions: NavOptions? = null) {
        navController.navigate(route, navOptions)

        // Uncomment the code below and delete the line above as per migration guide
        /*
        add(route)
        updateBackStack()
        */
    }

    /**
     * Go back to the previous route.
     */
    fun goBack() {
        navController.popBackStack()

        // Uncomment the code below and delete the line above as per migration guide
        /*
        if (backStack.size <= 1) {
            return
        }
        val removedKey = topLevelStacks[topLevelRoute]?.removeLastOrNull()
        // If the removed key was a top level key, remove the associated top level stack
        topLevelStacks.remove(removedKey)
        topLevelRoute = topLevelStacks.keys.last()
        updateBackStack()
        */
    }
}

/**
 * Tries to convert a [NavBackStackEntry] to a route of type [T].
 * Returns the typed route if the entry's destination matches [T], otherwise returns null.
 */
inline fun <reified T : Any> NavBackStackEntry.toRouteOrNull(): T? =
    if (this.destination.hasRoute<T>()) {
        this.toRoute<T>()
    } else {
        null
    }

/**
 * A marker interface for navigation routes.
 *
 * - Implementing the base interface, `Route`, indicates that this is a standard navigation route
 * - Implementing the `TopLevel` interface indicates that this is a top level route, and has a
 * sub stack of `Route`s
 * - Implementing the `Shared` interface indicates that this route can be placed onto any sub stack,
 * it is shared between top level stacks.
 *
 * @see `Navigator` for how these types are handled.
 */
sealed interface Route {
    interface TopLevel : Route
    interface Shared : Route
}
