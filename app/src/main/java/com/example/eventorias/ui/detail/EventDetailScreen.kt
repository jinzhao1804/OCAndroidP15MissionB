package com.example.eventorias.ui.detail

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberImagePainter
import com.example.eventorias.R
import com.example.eventorias.data.Event
import com.example.eventorias.ui.theme.app_white
import com.example.eventorias.ui.theme.dark
@Composable
fun EventDetailScreen(
    eventId: String,
    onBackPressed: () -> Unit,
    viewModel: EventDetailViewModel = viewModel(factory = EventDetailViewModelFactory(LocalContext.current))
) {
    val context = LocalContext.current
    val event by viewModel.event.collectAsState()
    val mapImageUrl by viewModel.mapImageUrl.collectAsState()

    LaunchedEffect(eventId) {
        viewModel.fetchEventDetails(eventId, context)
    }

    event?.let { eventData ->
        EventDetailUI(
            event = eventData,
            mapImageUrl = mapImageUrl,
            onBackPressed = onBackPressed,
            viewModel = viewModel
        )
    }
}

@Composable
fun EventDetailUI(
    event: Event,
    mapImageUrl: String?,
    onBackPressed: () -> Unit,
    viewModel: EventDetailViewModel
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = dark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(56.dp))

            EventImage(imageUrl = event.imageUrl)

            Column(
                modifier = Modifier
                    .width(364.dp)
                    .fillMaxHeight()
                    .padding(40.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    EventDateTime(
                        date = event.date,
                        time = event.time,
                        viewModel = viewModel
                    )

                    event.imageUrl?.let {
                        RoundedImage(
                            painter = rememberImagePainter(it),
                            contentDescription = "Event Image"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                EventDescription(description = event.description)

                Spacer(modifier = Modifier.height(16.dp))

                EventLocation(
                    address = event.address,
                    mapImageUrl = mapImageUrl
                )
            }
        }

        EventDetailHeader(
            title = event.title,
            onBackPressed = onBackPressed
        )
    }
}

@Composable
fun EventDetailHeader(
    title: String,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackPressed) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back Button",
                tint = colorResource(id = R.color.app_white)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.weight(1f),
            color = app_white
        )
    }
}

@Composable
fun EventImage(
    imageUrl: String?,
    modifier: Modifier = Modifier
) {
    imageUrl?.let { url ->
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 48.dp),
            contentAlignment = Alignment.Center // Use Alignment.Center instead of Alignment.CenterHorizontally
        ) {
            Image(
                painter = rememberImagePainter(url),
                contentDescription = null,
                modifier = modifier
                    .width(364.dp)
                    .height(354.dp),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
fun EventDateTime(
    date: String,
    time: String,
    viewModel: EventDetailViewModel,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.calendar),
                contentDescription = null,
                tint = app_white
            )
            Spacer(modifier = Modifier.padding(8.dp))
            BasicText(
                text = viewModel.formatDate(date),
                style = MaterialTheme.typography.bodyMedium.copy(color = app_white),
                modifier = Modifier.clearAndSetSemantics {
                    contentDescription = "Date ${viewModel.formatDate(date)}"
                }
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.time),
                contentDescription = null,
                tint = app_white
            )
            Spacer(modifier = Modifier.padding(8.dp))
            BasicText(
                text = viewModel.formatTime(time),
                style = MaterialTheme.typography.bodyMedium.copy(color = app_white),
                modifier = Modifier.clearAndSetSemantics {
                    contentDescription = "Time ${viewModel.formatTime(time)}"
                }
            )
        }
    }
}

@Composable
fun EventDescription(
    description: String,
    modifier: Modifier = Modifier
) {
    BasicText(
        text = description,
        style = MaterialTheme.typography.bodyMedium.copy(color = app_white),
        maxLines = 3,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier.clearAndSetSemantics {
            contentDescription = "Description ${description}"
        }
    )
}

@Composable
fun EventLocation(
    address: String,
    mapImageUrl: String?,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        BasicText(
            text = address,
            style = MaterialTheme.typography.bodyMedium.copy(color = app_white),
            modifier = Modifier.clearAndSetSemantics {
                contentDescription = "Address ${address}"
            }
        )
        mapImageUrl?.let { url ->
            Image(
                painter = rememberImagePainter(url),
                contentDescription = "Event Location Map",
                modifier = Modifier
                    .width(180.dp)
                    .height(100.dp)
                    .padding(top = 16.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .clearAndSetSemantics {}
                    ,
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
fun RoundedImage(painter: Painter, contentDescription: String) {
    Image(
        painter = painter,
        contentDescription = contentDescription,
        modifier = Modifier
            .size(60.dp)
            .clip(RoundedCornerShape(50.dp))
            .clearAndSetSemantics {},
        contentScale = ContentScale.Crop
    )
}
