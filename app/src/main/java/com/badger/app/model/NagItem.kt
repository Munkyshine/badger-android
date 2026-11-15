package com.badger.app.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "nags")
data class NagItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val isCompleted: Boolean = false,
    val dueDate: Long? = null,
    val priorityRaw: Int = 1,
    val burrowed: Boolean = false,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val radiusMeters: Int = 360,
    val trigger: String = "When I arrive",
    val placeName: String? = null
)
