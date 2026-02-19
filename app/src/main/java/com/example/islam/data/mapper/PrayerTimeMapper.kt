package com.example.islam.data.mapper

import com.example.islam.core.util.DateUtil.cleanTime
import com.example.islam.data.local.entity.PrayerTimeEntity
import com.example.islam.data.remote.dto.PrayerTimeResponse
import com.example.islam.domain.model.PrayerTime

fun PrayerTimeEntity.toDomain() = PrayerTime(
    id = id,
    date = date,
    imsak = imsak,
    fajr = fajr,
    sunrise = sunrise,
    dhuhr = dhuhr,
    asr = asr,
    maghrib = maghrib,
    isha = isha,
    hijriDate = hijriDate,
    city = city,
    country = country
)

fun PrayerTime.toEntity() = PrayerTimeEntity(
    id = id,
    date = date,
    imsak = imsak,
    fajr = fajr,
    sunrise = sunrise,
    dhuhr = dhuhr,
    asr = asr,
    maghrib = maghrib,
    isha = isha,
    hijriDate = hijriDate,
    city = city,
    country = country
)

fun PrayerTimeResponse.toEntity(city: String, country: String): PrayerTimeEntity {
    val t = data.timings
    val hijri = data.date.hijri
    return PrayerTimeEntity(
        date = data.date.gregorian.date,    // "19-02-2026"
        imsak = t.imsak.cleanTime(),
        fajr = t.fajr.cleanTime(),
        sunrise = t.sunrise.cleanTime(),
        dhuhr = t.dhuhr.cleanTime(),
        asr = t.asr.cleanTime(),
        maghrib = t.maghrib.cleanTime(),
        isha = t.isha.cleanTime(),
        hijriDate = "${hijri.day} ${hijri.month.ar} ${hijri.year}",
        city = city,
        country = country
    )
}
