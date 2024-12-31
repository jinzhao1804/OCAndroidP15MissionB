package com.example.eventorias.ui.add

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.location.Geocoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.eventorias.R
import com.example.eventorias.ui.theme.app_white
import com.example.eventorias.ui.theme.dark
import com.example.eventorias.ui.theme.red
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.*

class CreateEventActivity : ComponentActivity() {

    private lateinit var galleryLauncher: ActivityResultLauncher<Intent>
    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>
    private lateinit var requestPermissionsLauncher: ActivityResultLauncher<Array<String>>
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initializeLaunchers()
        checkAndRequestPermissions()
        setContent {
            CreateEventScreen(context = this) // Pass the activity context
        }
    }

    private fun checkAndRequestPermissions() {
        Log.e("CreateEventActivity", "Checking permissions")

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

    private fun initializeLaunchers() {
        Log.e("CreateEventActivity", "Initializing launchers")

        requestPermissionsLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val allGranted = permissions.all { it.value }
            if (allGranted) {
                Toast.makeText(this, "All permissions granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Some permissions are denied", Toast.LENGTH_SHORT).show()
            }
        }

        galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val imageUri = result.data?.data
                imageUri?.let {
                    handleImageUri(it)
                }
            }
        }

        cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val imageUri = result.data?.data
                imageUri?.let {
                    handleImageUri(it)
                }
            }
        }
        Log.e("CreateEventActivity", "Launchers initialized")
    }

    private fun handleImageUri(uri: Uri) {
        contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        imageUri = uri
    }


    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun CreateEventScreen(context: Context) { // Accept context as a parameter
        var title by remember { mutableStateOf("") }
        var date by remember { mutableStateOf("") }
        var time by remember { mutableStateOf("") }
        var address by remember { mutableStateOf("") }
        var description by remember { mutableStateOf("") }

        Scaffold(
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
                        IconButton(onClick = {
                            (context as? ComponentActivity)?.finish()
                        }) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
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
                    Spacer(modifier = Modifier.height(16.dp))

                    TextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Event Title") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    TextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Event Description") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        DateInputField(
                            date = date,
                            onDateChange = { date = it }
                        )

                        TimeInputField(
                            time = time,
                            onTimeChange = { time = it }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    TextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Enter full Address") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.width(150.dp)
                    ) {
                        Button(
                            onClick = { takePicture() },
                            colors = ButtonDefaults.buttonColors(containerColor = app_white) // Set background color to white
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.camera),
                                contentDescription = "Take Picture",
                                tint = dark // Set icon color to black
                            )
                        }

                        Button(
                            onClick = { openImageChooser() },
                            colors = ButtonDefaults.buttonColors(containerColor = red) // Set background color to red
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.file),
                                contentDescription = "Select Image from Gallery",
                                tint = app_white // Set icon color to white
                            )
                        }
                    }
                }

                // Button aligned at the bottom of the screen
                Button(
                    onClick = { onSaveEvent(title, date, time, address, description) },
                    modifier = Modifier
                        .align(Alignment.BottomCenter) // Align at the bottom center
                        .padding(32.dp) // Add padding for better spacing
                        .fillMaxWidth() // Make the button take the full width
                        ,
                    colors = ButtonDefaults.buttonColors(containerColor = red) // Set background color to red


                ) {
                    Text("Validate")
                }
            }
        }
    }

    @Composable
    fun DateInputField(date: String, onDateChange: (String) -> Unit) {
        var showDialog by remember { mutableStateOf(false) }

        // Handle Date Picker dialog visibility
        if (showDialog) {
            DatePickerDialog(onDateSelected = { selectedDate ->
                onDateChange(selectedDate)  // Use onDateChange to update the date state
                showDialog = false // Close dialog after selection
            })
        }

        TextField(
            value = date,
            onValueChange = { onDateChange(it) },
            label = { Text("Event Date") },
            readOnly = true,
            modifier = Modifier.fillMaxWidth(fraction = 0.45f),
            trailingIcon = {
                IconButton(onClick = {
                    showDialog = true // Show the Date Picker dialog when clicked
                }) {
                    Icon(Icons.Default.DateRange, contentDescription = "Select Date")
                }
            }
        )

    }

    @Composable
    fun DatePickerDialog(onDateSelected: (String) -> Unit) {
        val context = LocalContext.current

        // Create DatePickerDialog with a default date
        val datePickerDialog = remember {
            DatePickerDialog(
                context,
                { _, year, monthOfYear, dayOfMonth ->
                    // Format the selected date as "dd/MM/yyyy"
                    val selectedDate = String.format("%02d/%02d/%04d", dayOfMonth, monthOfYear + 1, year)
                    onDateSelected(selectedDate)
                },
                2024,  // Default year
                0,     // Default month (January)
                1      // Default day of the month
            )
        }

        LaunchedEffect(datePickerDialog) {
            datePickerDialog.show()
        }
    }

    @Composable
    fun TimeInputField(time: String, onTimeChange: (String) -> Unit) {
        var showDialog by remember { mutableStateOf(false) }

        // Handle Time Picker dialog visibility
        if (showDialog) {
            TimePickerDialog(onTimeSelected = { selectedTime ->
                onTimeChange(selectedTime)  // Use onTimeChange to update the time state
                showDialog = false // Close dialog after selection
            })
        }

        TextField(
            value = time,
            onValueChange = { onTimeChange(it) },
            label = { Text("Event Time") },
            readOnly = true,
            modifier = Modifier.fillMaxWidth(fraction = 0.90f),
            trailingIcon = {
                IconButton(onClick = {
                    showDialog = true // Show the Time Picker dialog when clicked
                }) {
                    Icon(Icons.Default.Edit, contentDescription = "Select Time")
                }
            }
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
                12, // Default hour
                0,  // Default minute
                true // Use 24-hour format
            )
        }

        LaunchedEffect(timePickerDialog) {
            timePickerDialog.show()
        }
    }



    private fun onSaveEvent(title: String, date: String, time: String, address: String, description: String) {
        /*if (title.isEmpty() || date.isEmpty() || time.isEmpty() || address.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }*/
        if(date.isEmpty()){
            Toast.makeText(this, "Please fill date fields", Toast.LENGTH_SHORT).show()

        }
        if(time.isEmpty()){
            Toast.makeText(this, "Please fill time fields", Toast.LENGTH_SHORT).show()

        }
        if(title.isEmpty()){
            Toast.makeText(this, "Please fill title fields", Toast.LENGTH_SHORT).show()

        }
        if(address.isEmpty()){
            Toast.makeText(this, "Please fill address fields", Toast.LENGTH_SHORT).show()

        }
        if(description.isEmpty()){
            Toast.makeText(this, "Please fill description fields", Toast.LENGTH_SHORT).show()

        }


        // Get coordinates from the provided address
        getCoordinatesFromAddress(address) { latitude, longitude ->
            // Once coordinates are retrieved, save the event data
            imageUri?.let { uri ->
                uploadImageToFirebase(uri) { downloadUrl ->
                    saveEvent(title, date, time, address, description, latitude, longitude, downloadUrl)
                }
            } ?: run {
                Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun uploadImageToFirebase(uri: Uri, onSuccess: (String) -> Unit) {
        val storageRef = FirebaseStorage.getInstance().reference
        val imageRef = storageRef.child("event_images/${UUID.randomUUID()}.jpg")

        imageRef.putFile(uri)
            .addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                    onSuccess(downloadUrl.toString())
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Image upload failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveEvent(
        title: String, date: String, time: String, address: String, description: String,
        latitude: Double, longitude: Double, imageUrl: String
    ) {
        val firestore = FirebaseFirestore.getInstance()
        val newEventDocument = firestore.collection("events")

        val event = hashMapOf(
            "title" to title,
            "date" to date,
            "time" to time,
            "address" to address,
            "description" to description,
            "imageUrl" to imageUrl,
            "latitude" to latitude,
            "longitude" to longitude
        )

        newEventDocument.add(event)
            .addOnSuccessListener {
                sendMessageToTopic(title, "all")
                Toast.makeText(this, "Event created successfully", Toast.LENGTH_SHORT).show()
                finish()

            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error creating event: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
    private fun sendMessageToTopic(message: String, topic: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val client = OkHttpClient()
                val jsonObject = JSONObject().apply {
                    put("message", message)
                    put("topic", topic)
                }

                Log.d("remoteMessage", "Sending message to topic: $topic")

                val requestBody = jsonObject.toString().toRequestBody("application/json".toMediaTypeOrNull())
                val request = Request.Builder()
                    .url("https://us-central1-p15eventorias.cloudfunctions.net/sendNotificationToTopic")
                   // .addHeader("Authorization", "Bearer $identityToken")  // Adding the Authorization header
                    .post(requestBody)
                    .build()

                Log.d("remoteMessage", "Request: $request")
                val response = client.newCall(request).execute()
                Log.d("remoteMessage", "Response: ${response.message}")

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val responseBody = response.body?.string()
                        Log.d("remoteMessage", "Message sent successfully: $responseBody")
                    } else {
                        val errorBody = response.body?.string()
                        Log.e("remoteMessage", "Message: $message, Topic: $topic")
                        Log.e("remoteMessage", "Failed to send message: ${response.message}, Error body: $errorBody")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("remoteMessage", "Exception occurred: ${e.message}")
                }
            }
        }
    }

    private fun openImageChooser() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        galleryLauncher.launch(Intent.createChooser(intent, "Select Image"))
    }

    private fun takePicture() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraLauncher.launch(takePictureIntent)
    }

    private fun getCoordinatesFromAddress(address: String, callback: (Double, Double) -> Unit) {
        val geocoder = Geocoder(this, Locale.getDefault())
        try {
            val addressList = geocoder.getFromLocationName(address, 1)
            if (addressList?.isNotEmpty() == true) {
                val location = addressList[0]
                val latitude = location.latitude
                val longitude = location.longitude
                Log.d("CreateEventActivity", "Coordinates: $latitude, $longitude")
                callback(latitude, longitude)
            } else {
                Toast.makeText(this, "No address found", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e("CreateEventActivity", "Geocoding error: ${e.message}")
            Toast.makeText(this, "Geocoding error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
