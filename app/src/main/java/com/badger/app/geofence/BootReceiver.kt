package com.badger.app.geofence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.badger.app.data.NagDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** Geofences are cleared on reboot; re-register them. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                GeofenceScheduler.sync(context, NagDatabase.getDatabase(context).nagDao().getLocated())
            } finally { pending.finish() }
        }
    }
}
