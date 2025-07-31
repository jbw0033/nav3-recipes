package com.example.nav3recipes

import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isSelectable
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.nav3recipes.migration.start.MigrationActivity
import org.junit.Rule
import org.junit.Test


class MigrationActivityNavigationTest {

    @get:Rule(order = 0)
    val composeTestRule = createAndroidComposeRule<MigrationActivity>()

    @Test
    fun firstScreen_isA() {
        composeTestRule.apply {
            onNode(hasText("Route A") and isSelectable()).assertIsSelected()
            onNodeWithText("Route A title").assertExists()
        }
    }

    @Test
    fun navigateToB_selectsB() {
        composeTestRule.apply {
            onNode(hasText("Route B") and isSelectable()).performClick()
            onNode(hasText("Route B") and isSelectable()).assertIsSelected()
            onNodeWithText("Route B title").assertExists()
        }
    }

    @Test
    fun navigateToA1_keepsASelected() {
        composeTestRule.apply {
            onNode(hasText("Route A") and isSelectable()).assertIsSelected()
            onNodeWithText("Route A title").assertExists()
            onNodeWithText("Go to A1").performClick()
            onNodeWithText("Route A1 title").assertExists()
            onNode(hasText("Route A") and isSelectable()).assertIsSelected()
        }
    }

    @Test
    fun navigateAtoBtoC_selectsCAndShowsContent() {
        composeTestRule.apply {
            onNode(hasText("Route B") and isSelectable()).performClick()
            onNode(hasText("Route B") and isSelectable()).assertIsSelected()
            onNodeWithText("Route B title").assertExists()

            onNode(hasText("Route C") and isSelectable()).performClick()
            onNode(hasText("Route C") and isSelectable()).assertIsSelected()
            onNodeWithText("Route C title").assertExists()
        }
    }

    @Test
    fun navigateAtoB_pressBack_showsA() {
        composeTestRule.apply {
            onNode(hasText("Route B") and isSelectable()).performClick()
            onNode(hasText("Route B") and isSelectable()).assertIsSelected()
            onNodeWithText("Route B title").assertExists()

            composeTestRule.runOnUiThread {
                composeTestRule.activity.onBackPressedDispatcher.onBackPressed()
            }

            onNode(hasText("Route A") and isSelectable()).assertIsSelected()
            onNodeWithText("Route A title").assertExists()
        }
    }

    @Test
    fun navigateAtoA1_pressBack_showsAContent() {
        composeTestRule.apply {
            onNodeWithText("Go to A1").performClick()
            onNodeWithText("Route A1 title").assertExists()
            onNode(hasText("Route A") and isSelectable()).assertIsSelected()

            composeTestRule.runOnUiThread {
                composeTestRule.activity.onBackPressedDispatcher.onBackPressed()
            }

            onNodeWithText("Route A title").assertExists()
            onNode(hasText("Route A") and isSelectable()).assertIsSelected()
        }
    }

    @Test
    fun navigateAtoBtoC_thenBack_showsA() {
        composeTestRule.apply {
            onNode(hasText("Route B") and isSelectable()).performClick()
            onNode(hasText("Route B") and isSelectable()).assertIsSelected()
            onNodeWithText("Route B title").assertExists()

            onNode(hasText("Route C") and isSelectable()).performClick()
            onNode(hasText("Route C") and isSelectable()).assertIsSelected()
            onNodeWithText("Route C title").assertExists()

            composeTestRule.runOnUiThread {
                composeTestRule.activity.onBackPressedDispatcher.onBackPressed()
            }

            onNode(hasText("Route A") and isSelectable()).assertIsSelected()
            onNodeWithText("Route A title").assertExists()
            onNodeWithText("Route B title").assertDoesNotExist()
        }
    }
}
