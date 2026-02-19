package com.example.islam.domain.model

data class UserPreferences(
    val city: String = "Istanbul",
    val country: String = "Turkey",
    val latitude: Double = 41.0082,
    val longitude: Double = 28.9784,
    val calculationMethod: Int = 13,    // 13 = Diyanet İşleri Başkanlığı
    val notificationsEnabled: Boolean = true,
    val useGps: Boolean = false,
    val darkTheme: Boolean = false
)
