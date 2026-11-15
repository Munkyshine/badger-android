package com.badger.app.data

import com.badger.app.model.NagItem
import kotlinx.coroutines.flow.Flow

class NagRepository(private val nagDao: NagDao) {
    val openNags: Flow<List<NagItem>> = nagDao.getOpenNags()
    val completedNags: Flow<List<NagItem>> = nagDao.getCompletedNags()
    val burrowedNags: Flow<List<NagItem>> = nagDao.getBurrowedNags()

    suspend fun insert(nag: NagItem) { nagDao.insertNag(nag) }
    suspend fun update(nag: NagItem) { nagDao.updateNag(nag) }
    suspend fun delete(nag: NagItem) { nagDao.deleteNag(nag) }
}
