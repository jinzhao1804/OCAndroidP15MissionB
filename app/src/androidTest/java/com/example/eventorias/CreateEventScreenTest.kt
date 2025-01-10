package com.example.eventorias.ui.add

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.eventorias.ui.theme.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EventDetailsInstrumentedTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testEventDetailsForm() {
        composeTestRule.setContent {
            EventDetailsForm(
                state = CreateEventState(),
                onTitleChange = {},
                onDescriptionChange = {},
                onDateChange = {},
                onTimeChange = {},
                onAddressChange = {}
            )
        }

        // Test Title Input
        composeTestRule.onNodeWithText("Event Title").performTextInput("Test Event")

        // Test Description Input
        composeTestRule.onNodeWithText("Event Description").performTextInput("This is a test event description.")

        // Test Address Input
        composeTestRule.onNodeWithText("Enter full Address").performTextInput("123 Test Street")
    }

    @Test
    fun testImageSelectionButtons() {
        composeTestRule.setContent {
            ImageSelectionButtons(
                onCameraClick = {},
                onGalleryClick = {},
                cornerRadius = 8.dp
            )
        }

        // Test Camera Button Click
        composeTestRule.onNodeWithContentDescription("Take Picture").performClick()

        // Test Gallery Button Click
        composeTestRule.onNodeWithContentDescription("Select Image from Gallery").performClick()
    }

    @Test
    fun testCreateEventTopAppBar() {
        composeTestRule.setContent {
            CreateEventTopAppBar(onBackPressed = {})
        }

        // Test Back Button Click
        composeTestRule.onNodeWithContentDescription("Back").performClick()
    }

    @Test
    fun testDateInputField() {
        composeTestRule.setContent {
            DateInputField(
                date = "01/01/2025",
                onDateChange = {}
            )
        }

        // Test Date Picker Dialog
        composeTestRule.onNodeWithContentDescription("Select Date").performClick()
    }

    @Test
    fun testTimeInputField() {
        composeTestRule.setContent {
            TimeInputField(
                time = "12:00",
                onTimeChange = {}
            )
        }

        // Test Time Picker Dialog
        composeTestRule.onNodeWithContentDescription("Select Time").performClick()
    }
}