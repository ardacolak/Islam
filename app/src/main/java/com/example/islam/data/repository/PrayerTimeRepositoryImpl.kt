package com.example.islam.data.repository

import com.example.islam.core.util.DateUtil
import com.example.islam.core.util.Resource
import com.example.islam.data.local.dao.PrayerTimeDao
import com.example.islam.data.mapper.toDomain
import com.example.islam.data.mapper.toEntity
import com.example.islam.data.remote.api.AladhanApi
import com.example.islam.domain.model.PrayerTime
import com.example.islam.domain.repository.PrayerTimeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PrayerTimeRepositoryImpl @Inject constructor(
    private val dao: PrayerTimeDao,
    private val api: AladhanApi
) : PrayerTimeRepository {

    override suspend fun getPrayerTimes(
        city: String,
        country: String,
        method: Int,
        date: String?
    ): Resource<PrayerTime> {
        val queryDate = date ?: DateUtil.todayFormatted()

        // 1. Try local cache first
        val cached = dao.getPrayerTimeByDate(queryDate, city)
        if (cached != null) {
            return Resource.Success(cached.toDomain())
        }

        // 2. Fetch from network
        return try {
            val response = api.getPrayerTimesByCity(
                city = city,
                country = country,
                method = method,
                date = queryDate
            )
            val entity = response.toEntity(city, country)
            dao.insertPrayerTime(entity)
            Resource.Success(entity.toDomain())
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Namaz vakitleri alınamadı.")
        }
    }

    override fun getPrayerTimesFlow(city: String): Flow<List<PrayerTime>> {
        return dao.getPrayerTimesFlow(city).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun deleteOldPrayerTimes(beforeDate: String) {
        dao.deleteOldPrayerTimes(beforeDate)
    }
}
