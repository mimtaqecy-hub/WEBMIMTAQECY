package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.*

data class PrayerTimes(
    val dateString: String,
    val dayName: String,
    val hijriString: String,
    val location: String,
    val imsak: String,
    val subuh: String,
    val terbit: String,
    val dhuha: String,
    val dzuhur: String,
    val ashar: String,
    val maghrib: String,
    val isya: String
)

object PrayerTimesCalculator {
    // Coordinates for Way Bungur, Lampung Timur
    const val DEFAULT_LAT = -5.0445
    const val DEFAULT_LNG = 105.5188
    const val TIMEZONE = 7.0 // WIB (UTC+7)

    fun calculate(date: Date = Date(), lat: Double = DEFAULT_LAT, lng: Double = DEFAULT_LNG): PrayerTimes {
        val cal = Calendar.getInstance()
        cal.time = date
        val dayOfYear = cal.get(Calendar.DAY_OF_YEAR)

        val dayFormat = SimpleDateFormat("EEEE", Locale("id", "ID"))
        val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
        val dayName = dayFormat.format(date)
        val dateString = dateFormat.format(date)

        // Solar Declination & Equation of Time calculations (Standard Astronomical Method)
        val b = 2 * Math.PI * (dayOfYear - 81) / 365.0
        val eot = 9.87 * sin(2 * b) - 7.53 * cos(b) - 1.5 * sin(b) // Equation of time in minutes
        val declination = 23.45 * sin(2 * Math.PI * (284 + dayOfYear) / 365.0) // degrees

        val declinationRad = Math.toRadians(declination)
        val latRad = Math.toRadians(lat)

        // Solar Noon in hours (WIB)
        val solarNoon = 12.0 + (TIMEZONE * 15.0 - lng) / 15.0 - (eot / 60.0)

        // Fajr angle for Kemenag RI is 20 degrees, Isha is 18 degrees
        val fajrAngleRad = Math.toRadians(-20.0)
        val ishaAngleRad = Math.toRadians(-18.0)
        val sunriseAngleRad = Math.toRadians(-0.833 - (0.0347 * sqrt(max(0.0, 30.0)))) // ~0.833 with 30m altitude

        fun hourAngle(angleRad: Double): Double {
            val cosHA = (sin(angleRad) - sin(latRad) * sin(declinationRad)) / (cos(latRad) * cos(declinationRad))
            val clamped = cosHA.coerceIn(-1.0, 1.0)
            return Math.toDegrees(acos(clamped)) / 15.0
        }

        // Asr calculation (Shafi'i: shadow length factor = 1)
        val asrAltitudeRad = atan(1.0 / (1.0 + tan(abs(latRad - declinationRad))))
        val asrHA = hourAngle(asrAltitudeRad)

        val sunriseHA = hourAngle(sunriseAngleRad)
        val fajrHA = hourAngle(fajrAngleRad)
        val ishaHA = hourAngle(ishaAngleRad)

        // Times in hours
        val dzuhurHour = solarNoon + (2.0 / 60.0) // +2 mins ihtiyat
        val asharHour = solarNoon + asrHA + (2.0 / 60.0)
        val maghribHour = solarNoon + sunriseHA + (2.0 / 60.0)
        val isyaHour = solarNoon + ishaHA + (2.0 / 60.0)
        val subuhHour = solarNoon - fajrHA + (2.0 / 60.0)
        val terbitHour = solarNoon - sunriseHA
        val imsakHour = subuhHour - (10.0 / 60.0)
        val dhuhaHour = terbitHour + (25.0 / 60.0)

        fun formatTime(hourDecimal: Double): String {
            var h = floor(hourDecimal).toInt()
            var m = floor((hourDecimal - h) * 60).toInt()
            if (m >= 60) {
                h += 1
                m -= 60
            }
            if (h >= 24) h -= 24
            if (h < 0) h += 24
            return String.format(Locale.getDefault(), "%02d:%02d WIB", h, m)
        }

        return PrayerTimes(
            dateString = dateString,
            dayName = dayName,
            hijriString = "1447 H / Kalender Hijriyah",
            location = "Way Bungur, Kab. Lampung Timur",
            imsak = formatTime(imsakHour),
            subuh = formatTime(subuhHour),
            terbit = formatTime(terbitHour),
            dhuha = formatTime(dhuhaHour),
            dzuhur = formatTime(dzuhurHour),
            ashar = formatTime(asharHour),
            maghrib = formatTime(maghribHour),
            isya = formatTime(isyaHour)
        )
    }
}
