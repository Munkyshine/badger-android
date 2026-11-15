package com.badger.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.badger.app.model.NagItem

@Database(entities = [NagItem::class], version = 3, exportSchema = false)
abstract class NagDatabase : RoomDatabase() {
    abstract fun nagDao(): NagDao

    companion object {
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE nags ADD COLUMN latitude REAL")
                db.execSQL("ALTER TABLE nags ADD COLUMN longitude REAL")
                db.execSQL("ALTER TABLE nags ADD COLUMN radiusMeters INTEGER NOT NULL DEFAULT 360")
                db.execSQL("ALTER TABLE nags ADD COLUMN trigger TEXT NOT NULL DEFAULT 'When I arrive'")
                db.execSQL("ALTER TABLE nags ADD COLUMN placeName TEXT")
            }
        }

        @Volatile
        private var INSTANCE: NagDatabase? = null

        fun getDatabase(context: Context): NagDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NagDatabase::class.java,
                    "nag_database"
                ).addMigrations(MIGRATION_2_3).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
