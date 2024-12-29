package com.example.eventorias


import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.example.eventorias.ui.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {

        private const val TAG = "remoteMessage"
    }

    // This is called when a message is successfully sent
    override fun onMessageSent(messageId: String) {
        super.onMessageSent(messageId)
        Log.d(TAG, "Message successfully sent: $messageId")

        // You can add additional logic here, such as informing the user
        // or updating UI if needed.
    }

    // This is called when there is an error sending a message
    override fun onSendError(messageId: String, exception: Exception) {
        super.onSendError(messageId, exception)
        Log.e(TAG, "Error sending message with ID $messageId", exception)

        // Handle the error, such as informing the user or logging it
    }

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        Log.d(TAG, "onMessageReceived called with message: ${remoteMessage.messageId}")

        Log.d(TAG, "From: ${remoteMessage.from}")



        // Handle FCM messages
        val message = remoteMessage.data["my_message"]
        val action = remoteMessage.data["my_action"]
        showNotification(message, action)


        // Check if message contains a data payload.
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")

            if (isLongRunningJob()) {
                scheduleJob()
            } else {
                handleNow()
            }
        }

        // Get the current FirebaseAuth instance and Firestore database
        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()

        if (auth.currentUser == null) {
            Log.e(TAG, "No authenticated user found.")
            return
        }


        auth.currentUser?.let { user ->
            val userDocRef = db.collection("users").document(user.uid)

            userDocRef.get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val document = task.result
                    if (document != null && document.exists()) {
                        // Retrieve the notification preference from Firestore
                        val receiveNotifications = document.getBoolean("receive_notifications")

                        if (receiveNotifications == true) {
                            // Check if message contains a notification payload.
                            remoteMessage.notification?.let {
                                Log.d(TAG, "Message Notification Body: ${it.body}")
                                it.body?.let { body -> sendNotification(body) }

                            }
                        } else {
                            Log.d(TAG, "Notifications are disabled for this user.")
                        }
                    } else {
                        Log.e(TAG, "User document does not exist.")
                    }
                } else {
                    Log.e(TAG, "Error getting user document", task.exception)
                }
            }
        } ?: run {
            Log.e(TAG, "No authenticated user found.")
        }
    }

    // [END receive_message]

    // This method is called when messages are deleted.
    override fun onDeletedMessages() {
        super.onDeletedMessages()

        // Handle message deletion here
        Log.d("FCM", "Messages were deleted from FCM servers")

        // You could trigger a sync or notification to users, etc.
        // For example, you might request fresh data from your server if needed.
    }

    private fun showNotification(message: String?, action: String?) {
        // Handle showing notification
        // You can use NotificationManager to display a notification in the system tray
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Implement logic to create and show a notification
        // Example:
        Log.d("FCM", "Notification message: $message, action: $action")
    }

    private fun isLongRunningJob() = true

    // [START on_new_token]
    /**
     * Called if the FCM registration token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the
     * FCM registration token is initially generated so this is where you would retrieve the token.
     */
    override fun onNewToken(token: String) {

        super.onNewToken(token)

        Log.d(TAG, "Refreshed token: $token")

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // FCM registration token to your app server.
        sendRegistrationToServer(token)


        Log.d("FCM", "New token: $token")

        // Save the new token to Firestore
        saveTokenToFirestore(token)
    }
    // [END on_new_token]

    /**
     * Schedule async work using WorkManager.
     */
    private fun scheduleJob() {
        // [START dispatch_job]
        val work = OneTimeWorkRequest.Builder(MyWorker::class.java).build()
        WorkManager.getInstance(this).beginWith(work).enqueue()
        // [END dispatch_job]
    }

    /**
     * Handle time allotted to BroadcastReceivers.
     */
    private fun handleNow() {
        Log.d(TAG, "Short lived task is done.")
    }

    /**
     * Persist token to third-party servers.
     *
     * Modify this method to associate the user's FCM registration token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private fun sendRegistrationToServer(token: String?) {
        // TODO: Implement this method to send token to your app server.
        Log.d(TAG, "sendRegistrationTokenToServer($token)")
    }

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageBody FCM message body received.
     */
    private fun sendNotification(messageBody: String) {
        val requestCode = 0
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            this,
            requestCode,
            intent,
            PendingIntent.FLAG_IMMUTABLE,
        )

        val channelId = getString(R.string.default_notification_channel_id)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.logo)
            .setContentTitle(getString(R.string.fcm_message))
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Channel human readable title",
                NotificationManager.IMPORTANCE_DEFAULT,
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notificationId = 0
        notificationManager.notify(notificationId, notificationBuilder.build())
    }

    private fun saveTokenToFirestore(token: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val db = FirebaseFirestore.getInstance()

        currentUser?.let {
            val userId = it.uid
            val userRef = db.collection("users").document(userId)

            userRef.update("fcmToken", token)
                .addOnSuccessListener {
                    Log.d("FCM", "New FCM token saved to Firestore successfully.")
                }
                .addOnFailureListener { e ->
                    Log.w("FCM", "Error saving new FCM token to Firestore", e)
                }
        }
    }


}
