package com.example.eventorias

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.example.eventorias.R
import com.example.eventorias.ui.profile.LogoutButton
import com.example.eventorias.ui.profile.NotificationsSwitch
import com.example.eventorias.ui.profile.ProfileHeader
import com.example.eventorias.ui.profile.ProfileTextField
import org.junit.Rule
import org.junit.Test

class ProfileHeaderTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testProfileHeaderDisplaysCorrectTextAndImage() {
        // Arrange: Set up the ProfileHeader composable
        composeTestRule.setContent {
            ProfileHeader(avatarImage = painterResource(id = R.drawable.profile1))
        }

        // Assert: Check if the header text is displayed
        composeTestRule.onNodeWithText("User profile").assertIsDisplayed()

        // Assert: Check if the avatar image is displayed
        composeTestRule.onNodeWithContentDescription("User Avatar").assertIsDisplayed()
    }

    @Test
    fun testProfileTextFieldDisplaysLabelAndValue() {
        // Arrange: Set up the ProfileTextField composable
        composeTestRule.setContent {
            ProfileTextField(label = "Name", value = "John Doe")
        }

        // Assert: Check if the label and value are displayed
        composeTestRule.onNodeWithText("Name").assertIsDisplayed()
        composeTestRule.onNodeWithText("John Doe").assertIsDisplayed()
    }


    @Test
    fun testNotificationsSwitchToggles() {
        // Arrange: Use MutableState to manage the toggle state
        val notificationsEnabled = mutableStateOf(false)

        composeTestRule.setContent {
            NotificationsSwitch(
                notificationsEnabled = notificationsEnabled.value,
                onCheckedChange = { isChecked ->
                    notificationsEnabled.value = isChecked
                }
            )
        }

        // Assert: Check if the switch is displayed and initially off
        composeTestRule.onNodeWithTag("NotificationsSwitch").assertIsDisplayed()
        composeTestRule.onNodeWithTag("NotificationsSwitch").assertIsOff()

        // Act: Perform a click on the switch to toggle it on
        composeTestRule.onNodeWithTag("NotificationsSwitch").performClick()

        // Assert: Verify the switch is now on
        composeTestRule.onNodeWithTag("NotificationsSwitch").assertIsOn()

        // Act: Perform another click on the switch to toggle it off
        composeTestRule.onNodeWithTag("NotificationsSwitch").performClick()

        // Assert: Verify the switch is now off
        composeTestRule.onNodeWithTag("NotificationsSwitch").assertIsOff()
    }


    @Test
    fun testLogoutButtonClick() {
        // Arrange: Set up the LogoutButton composable
        var isClicked = false
        composeTestRule.setContent {
            LogoutButton(onClick = { isClicked = true })
        }

        // Assert: Check if the button is displayed
        composeTestRule.onNodeWithText("Logout").assertIsDisplayed()

        // Act: Perform a click on the button
        composeTestRule.onNodeWithText("Logout").performClick()

        // Assert: Verify the click action (e.g., check if a callback is triggered)
        assert(isClicked)
    }

}