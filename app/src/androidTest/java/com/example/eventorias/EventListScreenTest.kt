package com.example.eventorias.ui.list

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.text.input.TextFieldValue
import com.example.eventorias.data.Event
import com.example.eventorias.ui.theme.dark
import org.junit.Rule
import org.junit.Test

class EventListComponentsTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testSearchBar_Interaction() {
        var searchText by mutableStateOf(TextFieldValue(""))
        composeTestRule.setContent {
            SearchBar(
                searchText = searchText,
                onSearchTextChange = { searchText = it },
                modifier = Modifier
            )
        }

        // Perform a text input in the search bar's input field
        composeTestRule.onNodeWithTag("SearchInputField").performTextInput("Event 1")

        // Wait for the UI to become idle
        composeTestRule.waitForIdle()

        // Verify that the search bar has the correct text
        composeTestRule.onNodeWithTag("SearchInputField").assertTextEquals("Event 1")
    }

    @Test
    fun testEventListHeader_InitialState() {
        // Set up the EventListHeader composable
        composeTestRule.setContent {
            EventListHeader(
                searchText = TextFieldValue(""),
                onSearchTextChange = {},
                isSortedDescending = true,
                onSortToggle = {}
            )
        }

        // Verify that the initial UI elements are displayed using test tags
        composeTestRule.onNode(hasTestTag("EventListHeaderTitle")).assertExists()
        composeTestRule.onNode(hasTestTag("EventListHeaderSearchBar")).assertExists()
        composeTestRule.onNode(hasTestTag("EventListHeaderSortButton")).assertExists()
    }

    @Test
    fun testSortButton_Interaction() {
        // Set up the SortButton composable
        composeTestRule.setContent {
            SortButton(
                isSortedDescending = true,
                onClick = {}
            )
        }

        // Perform a click on the sort button using the test tag
        composeTestRule.onNode(hasTestTag("SortButton")).performClick()

        // Verify that the sort button is clickable
        composeTestRule.onNode(hasTestTag("SortButton")).assertIsEnabled()
    }


    @Test
    fun testLoadingIndicator_Display() {
        // Set up the LoadingIndicator composable
        composeTestRule.setContent {
            LoadingIndicator()
        }

        // Verify that the loading indicator is displayed
        composeTestRule.onNodeWithTag("Loading Indicator").assertExists()
    }

    @Test
    fun testErrorMessage_DisplayAndRetry() {
        // Set up the ErrorMessage composable
        composeTestRule.setContent {
            ErrorMessage(onRetry = {})
        }

        // Verify that the error message is displayed
        composeTestRule.onNodeWithText("Failed to load events. Please check your network connection.").assertExists()

        // Perform a click on the retry button
        composeTestRule.onNodeWithText("Try again").performClick()

        // Verify that the retry button is clickable
    }
}