package com.example.myfridge2.Tools

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.myfridge2.R


const val notificationId=1
const val channelId="channel"
const val titleExtra="titleExtra"
const val messageExtra="messageExtra"
class NotificationService: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notification = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(intent.getStringExtra(titleExtra))
                .setContentText(intent.getStringExtra(messageExtra))
                .build()

            val manager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.notify(notificationId, notification)
        }}
}