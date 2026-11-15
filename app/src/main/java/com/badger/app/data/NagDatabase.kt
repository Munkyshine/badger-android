package com.badger.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.badger.app.model.NagItem

@Database(entities = [NagItem::class], version = 2, exportSchema = false)
abstract class NagDatabase : RoomDatabase() {
    abstract fun nagDao(): NagDao

    companion object {
        @Volatile
        private var INSTANCE: NagDatabase? = null

        fun getDatabase(context: Context): NagDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NagDatabase::class.java,
                    "nag_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
