package com.badger.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.badger.app.data.NagDatabase
import com.badger.app.data.NagRepository
import com.badger.app.model.NagItem
import com.badger.app.ui.Priority
import com.badger.app.geofence.GeofenceScheduler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NagViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: NagRepository

    val openNags: StateFlow<List<NagItem>>
    val completedNags: StateFlow<List<NagItem>>
    val burrowedNags: StateFlow<List<NagItem>>

    init {
        val nagDao = NagDatabase.getDatabase(application).nagDao()
        repository = NagRepository(nagDao)
        val sharing = SharingStarted.WhileSubscribed(5000)
        openNags = repository.openNags.stateIn(viewModelScope, sharing, emptyList())
        completedNags = repository.completedNags.stateIn(viewModelScope, sharing, emptyList())
        burrowedNags = repository.burrowedNags.stateIn(viewModelScope, sharing, emptyList())
        viewModelScope.launch {
            if (repository.openNags.first().isEmpty() &&
                repository.completedNags.first().isEmpty() &&
                repository.burrowedNags.first().isEmpty()
            ) {
                repository.insert(NagItem(title = "Water the plants", priorityRaw = Priority.ToDo.raw))
                repository.insert(NagItem(title = "Check property listings", priorityRaw = Priority.High.raw))
                repository.insert(NagItem(title = "Review client messages", priorityRaw = Priority.Urgent.raw))
            }
        }
    }

    fun addNag(
        title: String,
        priority: Priority = Priority.ToDo,
        latitude: Double? = null,
        longitude: Double? = null,
        radiusMeters: Int = 360,
        trigger: String = "When I arrive",
        placeName: String? = null
    ) {
        viewModelScope.launch {
            repository.insert(
                NagItem(
                    title = title.trim(), priorityRaw = priority.raw,
                    latitude = latitude, longitude = longitude,
                    radiusMeters = radiusMeters, trigger = trigger,
                    placeName = placeName?.trim()?.ifBlank { null }
                )
            )
            if (latitude != null) syncGeofences()
        }
    }

    fun completeNag(nag: NagItem) {
        viewModelScope.launch { repository.update(nag.copy(isCompleted = true, burrowed = false)); syncGeofences() }
    }

    fun burrowNag(nag: NagItem) {
        viewModelScope.launch { repository.update(nag.copy(burrowed = true)); syncGeofences() }
    }

    fun unburrowNag(nag: NagItem) {
        viewModelScope.launch { repository.update(nag.copy(burrowed = false)); syncGeofences() }
    }

    fun deleteNag(nag: NagItem) {
        viewModelScope.launch { repository.delete(nag); GeofenceScheduler.remove(getApplication(), nag.id) }
    }

    /** Re-registers geofences for active location nags and drops the rest. */
    fun syncGeofences() {
        viewModelScope.launch {
            GeofenceScheduler.sync(getApplication(), repository.getLocated())
        }
    }
}
