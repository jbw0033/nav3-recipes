package com.example.nav3recipes.navigator.basic

import androidx.compose.foundation.layout.add
import org.junit.Test
import kotlin.test.assertEquals

class NavigatorTest {

    private data object Home : Navigator.Route.TopLevel
    private data object ChatList : Navigator.Route.TopLevel
    private data object ChatDetail
    private data object Camera : Navigator.Route.TopLevel
    private data object Search : Navigator.Route.Unique

    @Test
    fun backStackContainsStartKey(){
        val navigator = Navigator<Any>(startRoute = Home)
        assert(navigator.backStack.contains(Home))
    }

    @Test
    fun navigatingToTopRoute_addsRouteToTopOfStack(){
        val navigator = Navigator<Any>(startRoute = Home)

        // Back stack start state [Home]
        // Navigate to ChatList
        // Expected back stack state [Home, ChatList]
        navigator.navigate(ChatList)
        assertEquals(listOf<Any>(Home, ChatList), navigator.backStack)
    }

    @Test
    fun addingNonTopLevelRoute_addsToCurrentTopLevelStack() {
        val navigator = Navigator<Any>(startRoute = Home) // Current: Home, Stack: [Home]

        // Navigate to ChatList, making it the current top-level route
        navigator.navigate(ChatList)
        // Current: ChatList, Stack: [Home, ChatList]
        assertEquals(listOf<Any>(Home, ChatList), navigator.backStack, "Backstack after adding ChatList")
        assertEquals(ChatList, navigator.topLevelRoute, "Current top level route should be ChatList")

        // Add ChatDetail (non-top-level) to the ChatList stack
        navigator.navigate(ChatDetail)
        // Current: ChatList, Stack: [Home, ChatList, ChatDetail]
        assertEquals(listOf<Any>(Home, ChatList, ChatDetail), navigator.backStack, "Backstack after adding ChatDetail")
    }

    @Test
    fun navigatingToNewTopLevel_withDefaultConfig_popsOtherTopLevelAndItsChildren() {
        // Default config: shouldPopOtherTopLevelRoutesWhenNavigatingToTopLevelRoute = true
        val navigator = Navigator<Any>(startRoute = Home)
        navigator.navigate(Search) // BackStack: [Home, Search]
        navigator.navigate(Camera) // BackStack: [Home, Search, Camera]
        navigator.navigate(ChatList)
        val expected = listOf(Home, Search, ChatList) // Camera is popped before ChatList is added
        assertEquals(expected, navigator.backStack)
    }

    @Test
    fun navigatingToNewTopLevel_whenStartRouteCannotMove_popsOtherTopLevelStacksExceptStartRoute() {
        val navigator = Navigator<Any>(startRoute = Home)
        navigator.navigate(Camera)
        val expected = listOf<Any>(Home, Camera) // Home is locked in place
        assertEquals(expected, navigator.backStack)
    }

    @Test
    fun navigatingToNewTopLevel_whenStartRouteCanMove_popsAllOtherTopLevelStacks() {
        val navigator = Navigator<Any>(startRoute = Home, canStartRouteMove = true)
        navigator.navigate(Camera)
        val expected = listOf<Any>(Camera) // Home is popped
        assertEquals(expected, navigator.backStack)
    }


}