package com.example.eventorias

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasTestTag
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

        // Assert: Check if the header text is displayed using the test tag
        composeTestRule.onNode(hasTestTag("ProfileHeaderText")).assertIsDisplayed()

        // Assert: Check if the avatar image is displayed using the test tag
        composeTestRule.onNode(hasTestTag("ProfileHeaderImage")).assertIsDisplayed()
    }

    @Test
    fun testProfileTextFieldDisplaysLabelAndValue() {
        // Arrange: Set up the ProfileTextField composable
        composeTestRule.setContent {
            ProfileTextField(label = "Name", value = "John Doe")
        }

        // Assert: Check if the label is displayed using the test tag
        composeTestRule.onNode(hasTestTag("ProfileTextField")).assertIsDisplayed()

    }


    @Test
    fun testNotificationsSwitch_contentDescription() {

        var notificationsEnabled by mutableStateOf(true)

        // Set up the composable with initial state
        composeTestRule.setContent {
            NotificationsSwitch(
                notificationsEnabled = notificationsEnabled,
                onCheckedChange = { newState ->
                    notificationsEnabled = newState // Update the state when the switch is toggled
                },
                modifier = Modifier
            )
        }

        // Verify the content description when the switch is on
        composeTestRule.onNodeWithTag("NotificationsSwitch")
            .assertContentDescriptionEquals("Notifications Switch is On")

        // Toggle the switch to off
        composeTestRule.onNodeWithTag("NotificationsSwitch")
            .performClick()

        // Verify the content description when the switch is off
        composeTestRule.onNodeWithTag("NotificationsSwitch")
            .assertContentDescriptionEquals("Notifications Switch is Off")
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