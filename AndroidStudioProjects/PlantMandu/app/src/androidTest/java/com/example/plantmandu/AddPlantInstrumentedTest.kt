package com.example.plantmandu

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.plantmandu.view.AddPlantActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AddPlantInstrumentedTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<AddPlantActivity>()

    @Test
    fun testAddPlantUI() {
        composeRule.onNodeWithTag("plantNameInput").assertExists()
        composeRule.onNodeWithTag("plantDescriptionInput").assertExists()
        composeRule.onNodeWithTag("plantPriceInput").assertExists()
        composeRule.onNodeWithTag("plantStockInput").assertExists()
        composeRule.onNodeWithTag("addPlantButton").assertExists()
    }

    @Test
    fun testAddPlantInput() {
        composeRule.onNodeWithTag("plantNameInput").performTextInput("Aloe Vera")
        composeRule.onNodeWithTag("plantDescriptionInput").performTextInput("Medicinal plant")
        composeRule.onNodeWithTag("plantPriceInput").performTextInput("15.5")
        composeRule.onNodeWithTag("plantStockInput").performTextInput("10")
        
        composeRule.onNodeWithTag("addPlantButton").performClick()
    }
}
