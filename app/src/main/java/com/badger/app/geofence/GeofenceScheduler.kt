package com.badger.app.geofence

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.badger.app.model.NagItem
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices

object GeofenceScheduler {
    fun hasForeground(c: Context) = ContextCompat.checkSelfPermission(c, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

    fun hasBackground(c: Context) =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ||
            ContextCompat.checkSelfPermission(c, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED

    private fun pendingIntent(c: Context): PendingIntent {
        val intent = Intent(c, GeofenceReceiver::class.java).setAction("com.badger.app.GEOFENCE_EVENT")
        // Geofencing needs a MUTABLE PendingIntent on API 31+.
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or
            (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_MUTABLE else 0)
        return PendingIntent.getBroadcast(c, 0, intent, flags)
    }

    private fun isActive(n: NagItem) = !n.isCompleted && !n.burrowed && n.latitude != null && n.longitude != null

    /** Registers geofences for active located nags and removes the rest. Safe to call repeatedly. */
    @SuppressLint("MissingPermission")
    fun sync(c: Context, located: List<NagItem>) {
        val client = LocationServices.getGeofencingClient(c)
        val stale = located.filterNot(::isActive).map { it.id.toString() }
        if (stale.isNotEmpty()) client.removeGeofences(stale)
        if (!hasForeground(c) || !hasBackground(c)) return
        // Android allows 100 geofences per app; take the highest-priority ones.
        val active = located.filter(::isActive).sortedByDescending { it.priorityRaw }.take(100)
        if (active.isEmpty()) return
        val fences = active.map { n ->
            val t = when (n.trigger) {
                "When I leave" -> Geofence.GEOFENCE_TRANSITION_EXIT
                "Both" -> Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT
                else -> Geofence.GEOFENCE_TRANSITION_ENTER
            }
            Geofence.Builder()
                .setRequestId(n.id.toString())
                .setCircularRegion(n.latitude!!, n.longitude!!, n.radiusMeters.toFloat().coerceAtLeast(100f))
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(t)
                .build()
        }
        val request = GeofencingRequest.Builder()
            // Fire right away if the phone is already inside the circle when "arrive" is set.
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofences(fences)
            .build()
        client.addGeofences(request, pendingIntent(c))
    }

    @SuppressLint("MissingPermission")
    fun remove(c: Context, id: Long) {
        LocationServices.getGeofencingClient(c).removeGeofences(listOf(id.toString()))
    }
}
