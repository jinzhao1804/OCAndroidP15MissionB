package com.example.eventorias.ui.list

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.util.Log
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.example.eventorias.R
import com.example.eventorias.data.Event
import com.example.eventorias.ui.theme.app_white
import com.example.eventorias.ui.theme.dark
import com.example.eventorias.ui.theme.grey

@SuppressLint("UnsafeOptInUsageError")
@Composable
fun EventListScreen(navController: NavController) {
    val viewModel: EventListViewModel = viewModel()
    val state by viewModel.state.collectAsState()
    //var isSortedDescending by remember { mutableStateOf(false) }


    val isLoading = state.isLoading
    val hasError = state.hasError
    val filteredEvents = state.filteredEvents
    val searchText = state.searchText

    LaunchedEffect(Unit) {
        viewModel.fetchEvents()
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
            .background(dark)
    ) {
        EventListHeader(
            searchText = searchText,
            onSearchTextChange = { text ->
                Log.e("EventListScreen", "Updated Text: ${text.text}")
                Log.e("EventListScreen", "Updated Cursor Position: ${text.selection}")
                viewModel.onSearchTextChange(text)
            },
            isSortedDescending = state.isSortedDescending,
            onSortToggle = {
                viewModel.onSortToggle()
            }
        )

        when {
            isLoading -> LoadingIndicator()
            hasError -> ErrorMessage(onRetry = { viewModel.fetchEvents() })
            else -> EventList(events = filteredEvents) { event ->
                navController.navigate("detail/${event.id}")
            }
        }
    }
}



@SuppressLint("UnsafeOptInUsageError")
@Composable
fun SearchBar(
    searchText: TextFieldValue, // Use TextFieldValue for text input
    onSearchTextChange: (TextFieldValue) -> Unit, // Callback for text changes
    modifier: Modifier = Modifier // Correct modifier type
) {
    var isFocused by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current // Keyboard controller
    val focusManager = LocalFocusManager.current // Focus manager

    // Toggle focus when isFocused changes
    LaunchedEffect(isFocused) {
        if (isFocused) {
            focusRequester.requestFocus()
        } else {
            focusRequester.freeFocus()
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .padding(8.dp) // Add padding for better spacing
    ) {
        TextField(
            value = searchText, // Pass TextFieldValue
            onValueChange = { newValue ->
                // Update the searchText state with the new TextFieldValue

                Log.e("SearchBar", "New Text: ${newValue.text}")
                Log.e("SearchBar", "Cursor Position: ${newValue.selection}")

                onSearchTextChange(
                    TextFieldValue(
                        text = newValue.text,
                        selection = TextRange(newValue.text.length)
                    )
                )
            },
            modifier = Modifier
                .testTag("SearchInputField") // Add this line
                .weight(1f) // Take up remaining space
                .padding(end = 8.dp)
                .focusRequester(focusRequester) // Attach FocusRequester
                .onFocusChanged { focusState ->
                    isFocused = focusState.isFocused
                }
                .semantics {
                    // Add accessibility properties
                    this.contentDescription = "Search input field" // Describe the purpose of the field
                    this.stateDescription = if (isFocused) "Focused" else "Not focused" // Describe the state
                   // this.isTraversalGroup = true // Helps with focus traversal
                },
            placeholder = {
                if (isFocused) {
                    Text(
                        text = "Search...",
                        modifier = Modifier.semantics {
                            // Add accessibility properties for the placeholder text
                            this.contentDescription = "Search placeholder"
                        }
                    )
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = {
                    // Toggle focus and hide the keyboard
                    isFocused = !isFocused
                    keyboardController?.hide() // Hide the keyboard
                    focusManager.clearFocus() // Unfocus the TextField

                }
            ),
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.LightGray,
                unfocusedContainerColor = dark,
                disabledContainerColor = dark,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = Color.White, // Customize cursor color
                selectionColors = TextSelectionColors(
                    handleColor = Color.White,
                    backgroundColor = Color.Gray
                )
            )
        )
        IconButton(
            onClick = {
                // Toggle focus and hide the keyboard
                isFocused = !isFocused
                keyboardController?.hide() // Hide the keyboard
                focusManager.clearFocus() // Unfocus the TextField

            },
            modifier = Modifier.size(48.dp), // Set explicit size for the IconButton
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = Color.Transparent, // Transparent background
                contentColor = MaterialTheme.colorScheme.onSurface // Ensure the icon color is visible
            )
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                modifier = Modifier.size(24.dp), // Set explicit size for the Icon
                tint = Color.White // Customize icon color
            )
        }
    }
}

@Composable
fun LoadingIndicator() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = colorResource(id = R.color.red),
            modifier = Modifier.testTag("Loading Indicator") // Add a test tag
        )
    }
}

@Composable
fun ErrorMessage(onRetry: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Failed to load events. Please check your network connection.",
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Button(
                onClick = onRetry,
                modifier = Modifier.padding(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.red))
            ) {
                Text(
                    text = "Try again",
                    color = colorResource(id = R.color.app_white)
                )
            }
        }
    }
}


@Composable
fun EventListHeader(
    searchText: TextFieldValue,
    onSearchTextChange: (TextFieldValue) -> Unit,
    isSortedDescending: Boolean,
    onSortToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Event list",
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            modifier = Modifier.testTag("EventListHeaderTitle") // Add a test tag for the title
        )
        SearchBar(
            searchText = searchText,
            onSearchTextChange = onSearchTextChange,
            modifier = Modifier
                .weight(1f)
                .testTag("EventListHeaderSearchBar") // Add a test tag for the search bar
        )
        SortButton(
            isSortedDescending = isSortedDescending,
            onClick = onSortToggle,
            modifier = Modifier.testTag("EventListHeaderSortButton") // Add a test tag for the sort button
        )
    }
}

@Composable
fun SortButton(
    isSortedDescending: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Dynamic content description based on the sort state
    val sortDescription = if (isSortedDescending) {
        "Sorted most recent to oldest "
    } else {
        "Sorted oldest to most recent"
    }

    Button(
        onClick = onClick,
        modifier = modifier
            .testTag("SortButton") // Add a test tag
            .size(48.dp) // Adjust size of the button if needed
            .clearAndSetSemantics {
                // Set the content description for accessibility
                contentDescription = "Sort Button, $sortDescription"
            },
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent, // Transparent background
            contentColor = MaterialTheme.colorScheme.onSurface // Ensure the icon color is visible
        ),
        contentPadding = PaddingValues(8.dp), // Add some padding to ensure the icon is visible
        shape = CircleShape
    ) {
        Icon(
            imageVector = if (isSortedDescending) Icons.Default.ArrowDropDown else Icons.Default.KeyboardArrowUp,
            contentDescription = sortDescription, // Dynamic description for the icon
            tint = app_white
        )
    }
}

@Composable
fun EventList(events: List<Event>, onEventClick: (Event) -> Unit) {
    LazyColumn {
        items(events) { event ->
            EventItem(event, onEventClick)
        }
    }
}

@Composable
fun EventItem(event: Event, onEventClick: (Event) -> Unit) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = grey,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(112.dp)
            .padding(8.dp)
            .clickable(
                onClick = { onEventClick(event) },
                role = Role.Button, // Indicates that this is a button for accessibility
                interactionSource = remember { MutableInteractionSource() },
                indication = LocalIndication.current
            )
            .semantics(mergeDescendants = true) {
                // Provide a custom label for the entire card
                contentDescription = "Event: ${event.title}, Date: ${event.date}"
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.padding(8.dp))

            // User profile image
            Image(
                painter = rememberImagePainter(event.imageUrl),
                contentDescription = null, // Prevent screen reader from announcing this image
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            val whiteTextStyle = TextStyle(color = colorResource(id = R.color.app_white))

            Column(
                modifier = Modifier.padding(start = 16.dp)
            ) {
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleSmall.merge(whiteTextStyle),
                    modifier = Modifier.clearAndSetSemantics {}
                )
                Text(
                    text = event.date,
                    style = MaterialTheme.typography.bodyMedium.merge(whiteTextStyle),
                    modifier = Modifier.clearAndSetSemantics {}
                )
            }

            // Event image at the end of the row
            Image(
                painter = rememberImagePainter(event.imageUrl),
                contentDescription = null, // Prevent screen reader from announcing this image
                modifier = Modifier
                    .fillMaxHeight()
                    .width(180.dp)
                    .aspectRatio(1f),
                contentScale = ContentScale.Crop
            )
        }
    }
}
