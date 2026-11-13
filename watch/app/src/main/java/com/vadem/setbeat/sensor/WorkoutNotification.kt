package com.vadem.setbeat.sensor

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.wear.ongoing.OngoingActivity
import androidx.wear.ongoing.Status
import com.vadem.setbeat.R
import com.vadem.setbeat.ui.MainActivity

object WorkoutNotification {
    const val ID = 1
    private const val CHANNEL_ID = "exercise"

    fun build(context: Context): Notification {
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(
            NotificationChannel(CHANNEL_ID, context.getString(R.string.channel_name), NotificationManager.IMPORTANCE_DEFAULT)
        )
        val openApp = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_IMMUTABLE,
        )
        val title = context.getString(R.string.notification_title)
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_heart)
            .setContentTitle(title)
            .setCategory(NotificationCompat.CATEGORY_WORKOUT)
            .setOngoing(true)
            .setContentIntent(openApp)
        OngoingActivity.Builder(context.applicationContext, ID, builder)
            .setStaticIcon(R.drawable.ic_heart)
            .setTouchIntent(openApp)
            .setStatus(Status.Builder().addTemplate(title).build())
            .build()
            .apply(context.applicationContext)
        return builder.build()
    }
}
