package com.example.islam.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.islam.data.local.dao.DhikrDao
import com.example.islam.data.local.dao.PrayerTimeDao
import com.example.islam.data.local.entity.DhikrEntity
import com.example.islam.data.local.entity.PrayerTimeEntity

@Database(
    entities = [PrayerTimeEntity::class, DhikrEntity::class],
    version = 1,
    exportSchema = false
)
abstract class IslamDatabase : RoomDatabase() {
    abstract fun prayerTimeDao(): PrayerTimeDao
    abstract fun dhikrDao(): DhikrDao

    companion object {
        const val DATABASE_NAME = "islam_db"
    }
}
