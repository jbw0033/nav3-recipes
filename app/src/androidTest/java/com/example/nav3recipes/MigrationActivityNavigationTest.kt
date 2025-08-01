package com.example.nav3recipes

import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isSelectable
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.espresso.Espresso
import com.example.nav3recipes.migration.start.StartMigrationActivity
import com.example.nav3recipes.migration.step2.Step2MigrationActivity
import com.example.nav3recipes.migration.step3.Step3MigrationActivity
import com.example.nav3recipes.migration.step4.Step4MigrationActivity
import org.junit.Rule
import org.junit.Test


class MigrationActivityNavigationTest {

    // To test each step in the migration plan, change the Activity name below.
    @get:Rule(order = 0)
    val composeTestRule = createAndroidComposeRule<Step4MigrationActivity>()

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

            Espresso.pressBack()

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

            Espresso.pressBack()

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

            Espresso.pressBack()

            onNode(hasText("Route A") and isSelectable()).assertIsSelected()
            onNodeWithText("Route A title").assertExists()
            onNodeWithText("Route B title").assertDoesNotExist()
        }
    }

    @Test
    fun navigateToDialogD_onA_showsDialogContentAndDismisses() {
        composeTestRule.apply {

            onNodeWithText("Open dialog D").performClick()
            onNodeWithText("Route D title (dialog)").assertExists()
            Espresso.pressBack()
            onNodeWithText("Route A title").assertExists()
        }
    }

    /**
     * These 2 tests fail and they shouldn't
     */
    @Test
    fun navigateToDialogD_onB_showsDialogContentAndDismisses() {
        composeTestRule.apply {

            onNode(hasText("Route B") and isSelectable()).performClick()

            onNodeWithText("Open dialog D").performClick()
            onNodeWithText("Route D title (dialog)").assertExists()
            Espresso.pressBack()
            onNodeWithText("Route B title").assertExists()
        }
    }


    @Test
    fun navigateToDialogD_onC_showsDialogContentAndDismisses() {
        composeTestRule.apply {

            onNode(hasText("Route C") and isSelectable()).performClick()

            onNodeWithText("Open dialog D").performClick()
            onNodeWithText("Route D title (dialog)").assertExists()
            Espresso.pressBack()
            onNodeWithText("Route C title").assertExists()
        }
    }


}
