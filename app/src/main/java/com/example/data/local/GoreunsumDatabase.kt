package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.SessionDao
import com.example.data.local.entity.ActiveDraftEntity
import com.example.data.local.entity.SessionRecordEntity

@Database(
    entities = [SessionRecordEntity::class, ActiveDraftEntity::class],
    version = 1,
    exportSchema = false
)
abstract class GoreunsumDatabase : RoomDatabase() {
    abstract fun sessionDao(): SessionDao

    companion object {
        @Volatile
        private var INSTANCE: GoreunsumDatabase? = null

        fun getInstance(context: Context): GoreunsumDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GoreunsumDatabase::class.java,
                    "goreunsum_database.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
