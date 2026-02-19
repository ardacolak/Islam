package com.example.islam.data.local.dao

import androidx.room.*
import com.example.islam.data.local.entity.PrayerTimeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PrayerTimeDao {

    @Query("SELECT * FROM prayer_times WHERE date = :date AND city = :city LIMIT 1")
    suspend fun getPrayerTimeByDate(date: String, city: String): PrayerTimeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrayerTime(entity: PrayerTimeEntity)

    @Query("SELECT * FROM prayer_times WHERE city = :city ORDER BY date DESC")
    fun getPrayerTimesFlow(city: String): Flow<List<PrayerTimeEntity>>

    @Query("DELETE FROM prayer_times WHERE date < :date")
    suspend fun deleteOldPrayerTimes(date: String)
}
