package com.example.eventorias.ui.add

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.example.eventorias.R
import com.example.eventorias.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventScreen(
    navController: NavController,
    onBackPressed: () -> Unit, // Callback for back navigation
    viewModel: CreateEventViewModel = viewModel()
) {
    val state = viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val imageUri by viewModel.imageUri.observeAsState()
    val eventSaved by viewModel.eventSaved.collectAsState()

    LaunchedEffect(eventSaved) {
        if (eventSaved) {
            navController.popBackStack() // Navigate back on success
        }
    }


    // Gallery Launcher
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

    // Camera Launcher
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

    // Permissions Launcher
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

    // Check and request permissions when the screen is first launched
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
        topBar = {
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
                // TextFields and other UI components
                TextField(
                    value = state.value.title,
                    onValueChange = { viewModel.updateTitle(it) },
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
                    value = state.value.description,
                    onValueChange = { viewModel.updateDescription(it) },
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
                        date = state.value.date,
                        onDateChange = { viewModel.updateDate(it) }
                    )

                    TimeInputField(
                        time = state.value.time,
                        onTimeChange = { viewModel.updateTime(it) }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextField(
                    value = state.value.address,
                    onValueChange = { viewModel.updateAddress(it) },
                    label = { Text("Enter full Address", color = app_white) },
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
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.width(150.dp)
                ) {


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

                    Button(onClick = {
                        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        cameraLauncher.launch(takePictureIntent)
                    }){
                        Icon(
                            painter = painterResource(id = R.drawable.camera),
                            contentDescription = "Take Picture",
                            tint = dark
                        )
                    }

                    Button(onClick = {
                        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                            type = "image/*"
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        galleryLauncher.launch(Intent.createChooser(intent, "Select Image"))
                    }){
                        Icon(
                            painter = painterResource(id = R.drawable.file),
                            contentDescription = "Select Image from Gallery",
                            tint = app_white
                        )


                    }
                }
            }

            Button(
                onClick = { viewModel.onSaveEvent(
                    context = context
                )},
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(32.dp)
                    .fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = red)
            ) {
                Text("Validate")
            }
        }
    }
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
            2024, 0, 1
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