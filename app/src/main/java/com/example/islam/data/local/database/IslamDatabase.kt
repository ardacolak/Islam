package com.example.islam.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.islam.data.local.dao.DhikrDao
import com.example.islam.data.local.dao.PrayerTimeDao
import com.example.islam.data.local.entity.DhikrEntity
import com.example.islam.data.local.entity.PrayerTimeEntity

@Database(
    entities = [PrayerTimeEntity::class, DhikrEntity::class],
    version = 2,          // 1 → 2: PrayerTimeEntity'e month ve year eklendi
    exportSchema = false
)
abstract class IslamDatabase : RoomDatabase() {
    abstract fun prayerTimeDao(): PrayerTimeDao
    abstract fun dhikrDao(): DhikrDao

    companion object {
        const val DATABASE_NAME = "islam_db"

        /**
         * Mevcut kullanıcıların verilerini silmeden şemayı günceller.
         * Eski kayıtlarda month/year = 0 olur; bu değerler countForMonth'ta
         * hiçbir zaman gerçek ay/yılla eşleşmez → ilk açılışta API'den taze veri çekilir.
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE prayer_times ADD COLUMN month INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE prayer_times ADD COLUMN year  INTEGER NOT NULL DEFAULT 0")
            }
        }
    }
}

