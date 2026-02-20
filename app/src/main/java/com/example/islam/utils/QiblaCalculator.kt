package com.example.islam.utils

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.toRadians

/**
 * Küresel trigonometri kullanarak Kıble yönünü hesaplar.
 *
 * Formül: Great Circle Bearing
 *   θ = atan2( sin(Δλ)·cos(φ₂),
 *              cos(φ₁)·sin(φ₂) − sin(φ₁)·cos(φ₂)·cos(Δλ) )
 *
 * Sonuç kuzeye göre saat yönünde derece cinsinden (0–360°) döner.
 */
object QiblaCalculator {

    private const val KAABA_LAT = 21.422487
    private const val KAABA_LON = 39.826206

    /**
     * Kullanıcının konumundan Kabe'ye olan yönü (Kıble açısını) hesaplar.
     *
     * @param userLat Kullanıcının enlemi (derece)
     * @param userLng Kullanıcının boylamı (derece)
     * @return Kuzeye göre saat yönünde Kıble açısı (0–360°)
     */
    fun calculateQiblaDirection(userLat: Double, userLng: Double): Float {
        val φ1 = userLat.toRadians()
        val φ2 = KAABA_LAT.toRadians()
        val Δλ = (KAABA_LON - userLng).toRadians()

        val y = sin(Δλ) * cos(φ2)
        val x = cos(φ1) * sin(φ2) - sin(φ1) * cos(φ2) * cos(Δλ)

        val bearingRad = atan2(y, x)
        val bearingDeg = Math.toDegrees(bearingRad).toFloat()

        return (bearingDeg + 360f) % 360f
    }
}
