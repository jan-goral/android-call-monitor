package cc.jang.callmonitor.android

import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import cc.jang.callmonitor.R

internal fun Service.startForegroundNotification() {
    val channelId = createNotificationChannel(
        id = "call_monitor",
        channelName = "Call Monitor",
        importance = NotificationManagerCompat.IMPORTANCE_LOW,
    )

    val activityIntent = Intent(this, MainActivity::class.java)

    val pendingIntent: PendingIntent = PendingIntent
        .getActivity(this, 0, activityIntent, PendingIntent.FLAG_IMMUTABLE)

    val builder = NotificationCompat
        .Builder(this, channelId)
        .setSmallIcon(R.mipmap.ic_launcher)
        .setContentIntent(pendingIntent)
        .setOngoing(true)
        .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
        .setContentTitle("Call Monitor")

    startForeground(1, builder.build())
}

private fun Context.createNotificationChannel(
    id: String,
    channelName: String,
    importance: Int,
): String {
    return NotificationChannelCompat.Builder(id, importance).apply {
        setName(channelName)
    }.build().also { channel ->
        NotificationManagerCompat.from(this)
            .createNotificationChannel(channel)
    }.id
}
