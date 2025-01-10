package com.example.eventorias

import androidx.compose.ui.res.painterResource
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.eventorias.ui.detail.EventDescription
import com.example.eventorias.ui.detail.EventDetailHeader
import com.example.eventorias.ui.detail.EventImage
import com.example.eventorias.ui.detail.EventLocation
import com.example.eventorias.ui.detail.RoundedImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EventDetailUITest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testEventDetailHeader() {
        composeTestRule.setContent {
            EventDetailHeader(
                title = "Sample Event",
                onBackPressed = {}
            )
        }

        // Verify the back button is displayed
        composeTestRule.onNodeWithContentDescription("Back").assertIsDisplayed()

        // Verify the event title is displayed
        composeTestRule.onNodeWithText("Sample Event").assertIsDisplayed()
    }

    @Test
    fun testEventImage() {
        composeTestRule.setContent {
            EventImage(imageUrl = "https://example.com/image.jpg")
        }

        // Verify the event image is displayed
        composeTestRule.onNodeWithContentDescription("Event Image").assertIsDisplayed()
    }



    @Test
    fun testEventDescription() {
        composeTestRule.setContent {
            EventDescription(description = "This is a sample event description.")
        }

        // Verify the event description is displayed
        composeTestRule.onNodeWithText("This is a sample event description.").assertIsDisplayed()
    }

    @Test
    fun testEventLocation() {
        composeTestRule.setContent {
            EventLocation(
                address = "123 Sample St, Sample City",
                mapImageUrl = "https://example.com/map.jpg"
            )
        }

        // Verify the event address is displayed
        composeTestRule.onNodeWithText("123 Sample St, Sample City").assertIsDisplayed()

        // Verify the map image is displayed
        composeTestRule.onNodeWithContentDescription("Event Location Map").assertIsDisplayed()
    }

    @Test
    fun testRoundedImage() {
        composeTestRule.setContent {
            RoundedImage(
                painter = painterResource(id = R.drawable.calendar),
                contentDescription = "Event Image"
            )
        }

        // Verify the rounded image is displayed
        composeTestRule.onNodeWithContentDescription("Event Image").assertIsDisplayed()
    }
}