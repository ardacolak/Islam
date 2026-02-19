package com.example.islam.data.remote.api

import com.example.islam.data.remote.dto.PrayerTimeResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface AladhanApi {

    /**
     * Get prayer times by city name.
     * Example: GET /v1/timingsByCity?city=Istanbul&country=Turkey&method=13
     */
    @GET("v1/timingsByCity")
    suspend fun getPrayerTimesByCity(
        @Query("city") city: String,
        @Query("country") country: String,
        @Query("method") method: Int = 13,
        @Query("date") date: String? = null   // "dd-MM-yyyy" format
    ): PrayerTimeResponse

    /**
     * Get prayer times by GPS coordinates.
     * Example: GET /v1/timings/1740009600?latitude=41.0082&longitude=28.9784&method=13
     */
    @GET("v1/timings/{timestamp}")
    suspend fun getPrayerTimesByCoordinates(
        @Path("timestamp") timestamp: Long,
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("method") method: Int = 13
    ): PrayerTimeResponse

    companion object {
        const val BASE_URL = "https://api.aladhan.com/"
    }
}
