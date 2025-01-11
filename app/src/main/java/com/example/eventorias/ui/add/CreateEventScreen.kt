package com.example.eventorias.ui.add

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.example.eventorias.R
import com.example.eventorias.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventScreen(
    navController: NavController,
    onBackPressed: () -> Unit,
    viewModel: CreateEventViewModel = viewModel()
) {
    val state = viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val imageUri by viewModel.imageUri.observeAsState()
    val eventSaved by viewModel.eventSaved.collectAsState()
    val cornerRadius = 8.dp

    LaunchedEffect(eventSaved) {
        if (eventSaved) {
            navController.popBackStack()
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageUri = result.data?.data
            imageUri?.let {
                viewModel.handleImageUri(it)
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageUri = result.data?.data
            imageUri?.let {
                viewModel.handleImageUri(it)
            }
        }
    }

    val requestPermissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.all { it.value }
        if (allGranted) {
            Toast.makeText(context, "All permissions granted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Some permissions are denied", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissionsLauncher.launch(
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA
                )
            )
        }
    }

    Scaffold(
        containerColor = dark,
        topBar = { CreateEventTopAppBar(onBackPressed) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                EventDetailsForm(
                    state = state.value,
                    onTitleChange = { viewModel.updateTitle(it) },
                    onDescriptionChange = { viewModel.updateDescription(it) },
                    onDateChange = { viewModel.updateDate(it) },
                    onTimeChange = { viewModel.updateTime(it) },
                    onAddressChange = { viewModel.updateAddress(it) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                imageUri?.let { uri ->
                    Image(
                        painter = rememberImagePainter(data = uri),
                        contentDescription = "Selected Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .padding(16.dp)
                    )
                }

                ImageSelectionButtons(
                    onCameraClick = {
                        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        cameraLauncher.launch(takePictureIntent)
                    },
                    onGalleryClick = {
                        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                            type = "image/*"
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        galleryLauncher.launch(Intent.createChooser(intent, "Select Image"))
                    },
                    cornerRadius = cornerRadius
                )
            }

            Button(
                onClick = { viewModel.onSaveEvent(context = context) },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(32.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(cornerRadius),
                colors = ButtonDefaults.buttonColors(containerColor = red)
            ) {
                Text("Validate")
            }
        }
    }
}

@Composable
fun EventDetailsForm(
    state: CreateEventState,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onDateChange: (String) -> Unit,
    onTimeChange: (String) -> Unit,
    onAddressChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(
            value = state.title,
            onValueChange = onTitleChange,
            label = { Text("Event Title", color = app_white) },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedTextColor = app_white,
                unfocusedTextColor = app_white,
                focusedContainerColor = grey,
                unfocusedContainerColor = grey
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = state.description,
            onValueChange = onDescriptionChange,
            label = { Text("Event Description", color = app_white) },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedTextColor = app_white,
                unfocusedTextColor = app_white,
                focusedContainerColor = grey,
                unfocusedContainerColor = grey
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            DateInputField(
                date = state.date,
                onDateChange = onDateChange
            )

            TimeInputField(
                time = state.time,
                onTimeChange = onTimeChange
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = state.address,
            onValueChange = onAddressChange,
            label = { Text("Enter full Address", color = app_white) },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedTextColor = app_white,
                unfocusedTextColor = app_white,
                focusedContainerColor = grey,
                unfocusedContainerColor = grey
            )
        )
    }
}

@Composable
fun ImageSelectionButtons(
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit,
    cornerRadius: Dp // Change to Dp type
) {
    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier.width(150.dp)
    ) {
        Button(
            onClick = onCameraClick,
            modifier = Modifier,
            shape = RoundedCornerShape(cornerRadius), // Use the passed cornerRadius
            colors = ButtonDefaults.buttonColors(containerColor = app_white)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.camera),
                contentDescription = "Take Picture",
                tint = dark
            )
        }

        Button(
            onClick = onGalleryClick,
            modifier = Modifier,
            shape = RoundedCornerShape(cornerRadius), // Use the passed cornerRadius
            colors = ButtonDefaults.buttonColors(containerColor = red) // Add a color for consistency
        ) {
            Icon(
                painter = painterResource(id = R.drawable.file),
                contentDescription = "Select Image from Gallery",
                tint = app_white
            )
        }
    }
}




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventTopAppBar(onBackPressed: () -> Unit) {
    TopAppBar(
        title = {
            Text(
                text = "Creation of an event",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackPressed) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = app_white)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = dark,
            titleContentColor = app_white,
            actionIconContentColor = app_white
        )
    )
}



@Composable
fun DateInputField(date: String, onDateChange: (String) -> Unit) {
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        DatePickerDialog(onDateSelected = { selectedDate ->
            onDateChange(selectedDate)
            showDialog = false
        })
    }

    TextField(
        value = date,
        onValueChange = { onDateChange(it) },
        label = { Text("Event Date", color = app_white) },
        readOnly = true,
        modifier = Modifier.fillMaxWidth(fraction = 0.45f),
        trailingIcon = {
            IconButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.DateRange, contentDescription = "Select Date", tint = app_white)
            }
        },
        colors = TextFieldDefaults.colors(
            focusedTextColor = app_white,
            unfocusedTextColor = app_white,
            focusedContainerColor = grey,
            unfocusedContainerColor = grey
        )
    )
}

@Composable
fun DatePickerDialog(onDateSelected: (String) -> Unit) {
    val context = LocalContext.current
    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _, year, monthOfYear, dayOfMonth ->
                val selectedDate = String.format("%02d/%02d/%04d", dayOfMonth, monthOfYear + 1, year)
                onDateSelected(selectedDate)
            },
            2025, 0, 1
        )
    }

    LaunchedEffect(datePickerDialog) {
        datePickerDialog.show()
    }
}

@Composable
fun TimeInputField(time: String, onTimeChange: (String) -> Unit) {
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        TimePickerDialog(onTimeSelected = { selectedTime ->
            onTimeChange(selectedTime)
            showDialog = false
        })
    }

    TextField(
        value = time,
        onValueChange = { onTimeChange(it) },
        label = { Text("Event Time", color = app_white) },
        readOnly = true,
        modifier = Modifier.fillMaxWidth(fraction = 0.90f),
        trailingIcon = {
            IconButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Edit, contentDescription = "Select Time", tint = app_white)
            }
        },
        colors = TextFieldDefaults.colors(
            focusedTextColor = app_white,
            unfocusedTextColor = app_white,
            focusedContainerColor = grey,
            unfocusedContainerColor = grey
        )
    )
}

@Composable
fun TimePickerDialog(onTimeSelected: (String) -> Unit) {
    val context = LocalContext.current
    val timePickerDialog = remember {
        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                val selectedTime = String.format("%02d:%02d", hourOfDay, minute)
                onTimeSelected(selectedTime)
            },
            12, 0, true
        )
    }

    LaunchedEffect(timePickerDialog) {
        timePickerDialog.show()
    }
}