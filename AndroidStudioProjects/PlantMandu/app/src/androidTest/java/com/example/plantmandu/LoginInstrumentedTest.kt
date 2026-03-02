package com.example.plantmandu

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.plantmandu.view.LoginActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginInstrumentedTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<LoginActivity>()

    @Test
    fun testLoginUI() {
        composeRule.onNodeWithTag("emailInput").assertExists()
        composeRule.onNodeWithTag("passwordInput").assertExists()
        composeRule.onNodeWithTag("loginButton").assertExists()
    }

    @Test
    fun testLoginInput() {
        composeRule.onNodeWithTag("emailInput").performTextInput("test@example.com")
        composeRule.onNodeWithTag("passwordInput").performTextInput("password123")
        
        composeRule.onNodeWithTag("loginButton").performClick()
    }
}
