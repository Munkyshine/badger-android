package com.badger.app.data

import androidx.room.*
import com.badger.app.model.NagItem
import kotlinx.coroutines.flow.Flow

@Dao
interface NagDao {
    @Query("SELECT * FROM nags WHERE isCompleted = 0 AND burrowed = 0 ORDER BY priorityRaw DESC, id ASC")
    fun getOpenNags(): Flow<List<NagItem>>

    @Query("SELECT * FROM nags WHERE isCompleted = 1 ORDER BY id DESC")
    fun getCompletedNags(): Flow<List<NagItem>>

    @Query("SELECT * FROM nags WHERE burrowed = 1 AND isCompleted = 0 ORDER BY id DESC")
    fun getBurrowedNags(): Flow<List<NagItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNag(nag: NagItem)

    @Update
    suspend fun updateNag(nag: NagItem)

    @Delete
    suspend fun deleteNag(nag: NagItem)
}
