package com.example.eventorias.ui.addEvent

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
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
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
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
            CreateEventScreen()
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


        // Initialize permissions request launcher
        requestPermissionsLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val allGranted = permissions.all { it.value }
            if (allGranted) {
                Toast.makeText(this, "All permissions granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Some permissions are denied", Toast.LENGTH_SHORT).show()
            }
        }
        // Initialize the gallery launcher
        galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val imageUri = result.data?.data
                imageUri?.let {
                    handleImageUri(it)
                }
            }
        }

        // Initialize the camera launcher
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

    @Composable
    fun CreateEventScreen() {
        var title by remember { mutableStateOf("") }
        var date by remember { mutableStateOf("") }
        var time by remember { mutableStateOf("") }
        var address by remember { mutableStateOf("") }
        var description by remember { mutableStateOf("") }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Create Event", style = MaterialTheme.typography.titleSmall)

            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Event Title") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = date,
                onValueChange = { date = it },
                label = { Text("Event Date") },
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = { showDatePicker() }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Select Date")
                    }
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = time,
                onValueChange = { time = it },
                label = { Text("Event Time") },
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = { showTimePicker() }) {
                        Icon(Icons.Default.Info, contentDescription = "Select Time")
                    }
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Event Address") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Event Description") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = { openImageChooser() }) {
                    Icon(Icons.Default.Person, contentDescription = "Select Image from Gallery")
                }

                IconButton(onClick = { takePicture() }) {
                    Icon(Icons.Default.Edit, contentDescription = "Take Picture")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = { onSaveEvent(title, date, time, address, description) }) {
                Text("Save Event")
            }
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            // Update date state in the Compose UI
        }, year, month, day).show()
    }

    private fun showTimePicker() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        TimePickerDialog(this, { _, selectedHour, selectedMinute ->
            // Update time state in the Compose UI
        }, hour, minute, true).show()
    }

    private fun onSaveEvent(title: String, date: String, time: String, address: String, description: String) {
        if (title.isEmpty() || date.isEmpty() || time.isEmpty() || address.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        imageUri?.let { uri ->
            uploadImageToFirebase(uri) { downloadUrl ->
                saveEvent(title, date, time, address, description, downloadUrl)
            }
        } ?: run {
            Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show()
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

    private fun saveEvent(title: String, date: String, time: String, address: String, description: String, imageUrl: String) {
        val firestore = FirebaseFirestore.getInstance()
        val newEventDocument = firestore.collection("events")

        val event = hashMapOf(
            "title" to title,
            "date" to date,
            "time" to time,
            "address" to address,
            "description" to description,
            "imageUrl" to imageUrl,
            "latitude" to 0.0,
            "longitude" to 0.0
        )

        newEventDocument.add(event)
            .addOnSuccessListener {
                Toast.makeText(this, "Event created successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error creating event: ${e.message}", Toast.LENGTH_SHORT).show()
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
}

