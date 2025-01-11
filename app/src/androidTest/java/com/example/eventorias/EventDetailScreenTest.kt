package com.example.eventorias

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
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
        composeTestRule.onNodeWithContentDescription("Back Button").assertIsDisplayed()

        // Verify the event title is displayed
        composeTestRule.onNodeWithText("Sample Event").assertIsDisplayed()
    }

    @Test
    fun testEventImage() {
        composeTestRule.setContent {
            EventImage(imageUrl = "https://example.com/image.jpg")
        }

        // Verify the event image is displayed using the test tag
        composeTestRule.onNode(hasTestTag("EventImage")).assertIsDisplayed()
    }



    @Test
    fun testEventDescription() {
        composeTestRule.setContent {
            EventDescription(description = "This is a sample event description.")
        }

        // Verify the event description is displayed using the test tag
        composeTestRule.onNode(hasTestTag("EventDescription")).assertIsDisplayed()
    }


    @Test
    fun testEventLocation() {
        composeTestRule.setContent {
            EventLocation(
                address = "123 Sample St, Sample City",
                mapImageUrl = "https://example.com/map.jpg"
            )
        }

        // Verify the event address is displayed using the test tag
        composeTestRule.onNode(hasTestTag("EventAddress")).assertIsDisplayed()

        // Verify the map image is displayed using the test tag
        composeTestRule.onNode(hasTestTag("EventMapImage")).assertIsDisplayed()
    }

    @Test
    fun testRoundedImageExists() {
        // Set the content of the Compose UI to the RoundedImage composable
        composeTestRule.setContent {
            RoundedImage(
                painter = painterResource(id = R.drawable.calendar),
                contentDescription = "Calendar Icon"
            )
        }

        // Verify the RoundedImage exists in the UI hierarchy using the test tag
        composeTestRule.onNode(hasTestTag("RoundedImageTestTag")).assertIsDisplayed()
    }
}