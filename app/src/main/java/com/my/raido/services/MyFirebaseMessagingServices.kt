package com.my.raido.services

import android.app.ActivityManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.text.HtmlCompat
import com.bumptech.glide.Glide
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.my.raido.R
import com.my.raido.ui.home.HomeActivity


class MyFirebaseMessagingServices : FirebaseMessagingService() {

    override fun handleIntent(intent: Intent) {

//        Log.d(TAG, "handleIntent: ${intent.getStringExtra("Time")}")
//        Log.d(TAG, "handleIntent: ${intent.getStringExtra("Title")}")
//        Log.d(TAG, "handleIntent: ${intent.getStringExtra("Action")}")
//        Log.d(TAG, "handleIntent: ${intent.getStringExtra("Message")}")
//        Log.d(TAG, "handleIntent: intent data => ${intent.extras}")
//        val db = DBHelper(this, null)
//        val contentValues = ContentValues()
//        contentValues.put(DBHelper.TITLE_COL, intent.getStringExtra("Title"))
//        contentValues.put(DBHelper.MESSAGE_COL, intent.getStringExtra("Message"))
//        contentValues.put(DBHelper.TIME_COL, intent.getStringExtra("Time"))
//        db.addNotificationRow(contentValues)

        super.handleIntent(intent)

    }

    private fun isAppRunning(context: Context, packageName: String): Boolean {
        val activityManager = context.getSystemService(ACTIVITY_SERVICE) as ActivityManager
        val procInfos = activityManager.runningAppProcesses
        if (procInfos != null) {
            for (processInfo in procInfos) {
                if (processInfo.processName == packageName) {
                    return true
                }
            }
        }
        return false
    }

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "onMessageReceived: message data is ${remoteMessage}")

        // Log and check the notification payload
        Log.d("FCM", "From: ${remoteMessage.from}")
        remoteMessage.data.let {
            Log.d("FCM", "Message data payload: $it")
        }

        remoteMessage.notification?.let {
            Log.d("FCM", "Message Notification Body: ${it.imageUrl}")
            Log.d("FCM", "Message Notification Body: ${it.body} and ${it.title}")
            it.imageUrl?.let { it1 ->
                generateNotification(it.title.toString(),it.body.toString(),
                    it1, this)
            }
        }


    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // FCM registration token to your app server.
        sendRegistrationToServer(token)
    }
    // [END on_new_token]

    private fun sendRegistrationToServer(token: String?) {
        // TODO: Implement this method to send token to your app server.
        Log.d(TAG, "sendRegistrationTokenToServer($token)")
    }

    fun generateNotification(
        title: String,
        message: String,
        documentUri: Uri,
        myFirebaseMessagingServices: MyFirebaseMessagingServices
    ){
//        Log.d(TAG, "generateNotification: norification title is $title and message is $message and action is $action")

//        val intent = Intent(this, MainActivity::class.java)
        val intent = Intent(this, HomeActivity::class.java)

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)


        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT) //PendingIntent.FLAG_ONE_SHOT
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val bitmap = try {
            Glide.with(myFirebaseMessagingServices)
                .asBitmap()
                .load(documentUri)
                .submit()
                .get()
        } catch (err: Exception){
            null
        }

        // Intent to open the document
        val openDocumentIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(documentUri, getMimeType(documentUri))
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        val openPendingIntent = try {
            PendingIntent.getActivity(
                myFirebaseMessagingServices,
                0,
                openDocumentIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }catch (err: Exception){
             null
        }



        val logoImage: Bitmap = BitmapFactory.decodeResource(
            applicationContext.resources,
            R.drawable.app_logo
        )

//        val bigPicStyle = NotificationCompat.BigPictureStyle()
//        .bigPicture(logoImage)
//        .bigLargeIcon(null)
        var builder: NotificationCompat.Builder = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.app_logo)
            .setContentTitle(HtmlCompat.fromHtml(title, HtmlCompat.FROM_HTML_MODE_COMPACT))
            .setContentText(HtmlCompat.fromHtml(message, HtmlCompat.FROM_HTML_MODE_COMPACT))
            .setAutoCancel(true)
            .setVibrate(longArrayOf(1000, 1000, 1000, 1000))
            .setOnlyAlertOnce(true)
            .setSound(defaultSoundUri)


        if(bitmap != null ){
            builder.setLargeIcon(bitmap)
                    .setStyle(
                        NotificationCompat.BigPictureStyle()
                            .bigPicture(bitmap)
                            .bigLargeIcon(logoImage)
                    )
            if(openPendingIntent != null){
                builder.setContentIntent(openPendingIntent)
            }else{
                builder.setContentIntent(pendingIntent)
            }
        }else{
            builder.setContentIntent(openPendingIntent)
        }


//        var builder: NotificationCompat.Builder =
//        if(bitmap != null) {
//            // channel id, channel name
//                NotificationCompat.Builder(applicationContext, channelId)
//                    .setSmallIcon(R.drawable.app_logo)
//                    .setLargeIcon(bitmap)
//                    .setStyle(
//                        NotificationCompat.BigPictureStyle()
//                            .bigPicture(bitmap)
//                            .bigLargeIcon(logoImage)
//                    )
//                    .setContentTitle(HtmlCompat.fromHtml(title, HtmlCompat.FROM_HTML_MODE_COMPACT))
//                    .setContentText(HtmlCompat.fromHtml(message, HtmlCompat.FROM_HTML_MODE_COMPACT))
//                    .setAutoCancel(true)
//                    .setVibrate(longArrayOf(1000, 1000, 1000, 1000))
//                    .setOnlyAlertOnce(true)
//                    .setSound(defaultSoundUri)
//                    .setContentIntent(pendingIntent)
//        }else{
//            NotificationCompat.Builder(applicationContext, channelId)
//                .setSmallIcon(R.drawable.app_logo)
//                .setContentTitle(HtmlCompat.fromHtml(title, HtmlCompat.FROM_HTML_MODE_COMPACT))
//                .setContentText(HtmlCompat.fromHtml(message, HtmlCompat.FROM_HTML_MODE_COMPACT))
//                .setAutoCancel(true)
//                .setVibrate(longArrayOf(1000, 1000, 1000, 1000))
//                .setOnlyAlertOnce(true)
//                .setSound(defaultSoundUri)
//                .setContentIntent(openPendingIntent)
//        }



//        builder = builder.setContent(getremoteView(title, message))

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val notificationChannel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(notificationChannel)
        }
        notificationManager.notify(0, builder.build())
    }

    private fun getMimeType(uri: Uri): String? {
        return when {
            uri.toString().endsWith(".pdf") -> "application/pdf"
            uri.toString().endsWith(".doc") || uri.toString().endsWith(".docx") -> "application/msword"
            uri.toString().endsWith(".xls") || uri.toString().endsWith(".xlsx") -> "application/vnd.ms-excel"
            else -> "*/*"
        }
    }

    companion object {

        private const val TAG = "MyFirebaseMsgService"

        private const val channelId = "notification_channel"
        private const val channelName = "com.bizwhatspro"
    }
}