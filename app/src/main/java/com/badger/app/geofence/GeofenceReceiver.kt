package com.badger.app.geofence

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.badger.app.data.NagDatabase
import com.badger.app.ui.MainActivity
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GeofenceReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val event = GeofencingEvent.fromIntent(intent) ?: return
        if (event.hasError()) return
        val arrived = event.geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER
        val left = event.geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT
        if (!arrived && !left) return
        val ids = event.triggeringGeofences?.mapNotNull { it.requestId.toLongOrNull() } ?: return
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val dao = NagDatabase.getDatabase(context).nagDao()
                ids.forEach { id ->
                    val nag = dao.getById(id) ?: return@forEach
                    if (nag.isCompleted || nag.burrowed) return@forEach
                    val where = nag.placeName?.takeIf { it.isNotBlank() }
                    val body = when {
                        arrived && where != null -> "You're at $where"
                        arrived -> "You're at your nag location"
                        where != null -> "You left $where"
                        else -> "You left your nag location"
                    }
                    notify(context, nag.id, nag.title, body)
                }
            } finally {
                pending.finish()
            }
        }
    }

    private fun notify(c: Context, id: Long, title: String, body: String) {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(c, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return
        val nm = c.getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= 26) {
            nm.createNotificationChannel(
                NotificationChannel(CHANNEL, "Location nags", NotificationManager.IMPORTANCE_HIGH)
            )
        }
        val open = PendingIntent.getActivity(
            c, id.toInt(), Intent(c, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val n = NotificationCompat.Builder(c, CHANNEL)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(open)
            .build()
        NotificationManagerCompat.from(c).notify(id.toInt(), n)
    }

    companion object { const val CHANNEL = "location_nags" }
}
