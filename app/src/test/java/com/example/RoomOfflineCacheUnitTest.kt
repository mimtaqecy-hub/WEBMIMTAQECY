package com.example

import com.example.data.local.NewsEntity
import com.example.data.local.StudentEntity
import com.example.data.local.TeacherEntity
import com.example.util.CsvHelper
import com.example.util.PrayerTimesCalculator
import org.junit.Assert.*
import org.junit.Test

class RoomOfflineCacheUnitTest {

    @Test
    fun testStudentOfflineCacheEntityCreation() {
        val student = StudentEntity(
            fullName = "Ahmad Al-Fatih",
            nisn = "3154829101",
            birthPlace = "Lampung Timur",
            birthDate = "12 Mei 2017",
            classLevel = 1,
            rombel = "Ar Rahman",
            gender = "Laki-laki"
        )
        assertEquals("Ahmad Al-Fatih", student.fullName)
        assertEquals(1, student.classLevel)
        assertEquals("Ar Rahman", student.rombel)
        assertTrue("Cache timestamp must be valid", student.cachedAt > 0)
    }

    @Test
    fun testTeacherOfflineCacheEntityCreation() {
        val teacher = TeacherEntity(
            name = "Ust. Muhammad Ridwan, S.Ag.",
            role = "Guru PAI",
            subject = "Al-Qur'an Hadist"
        )
        assertEquals("Ust. Muhammad Ridwan, S.Ag.", teacher.name)
        assertTrue("Cache timestamp must be valid", teacher.cachedAt > 0)
    }

    @Test
    fun testNewsOfflineCacheEntityCreation() {
        val news = NewsEntity(
            title = "Penerimaan Santri Baru",
            category = "Pengumuman",
            summary = "Pendaftaran telah dibuka",
            content = "Informasi lengkap pendaftaran",
            date = "17 Okt 2026"
        )
        assertEquals("Pengumuman", news.category)
        assertTrue("Cache timestamp must be valid", news.cachedAt > 0)
    }

    @Test
    fun testCsvHelperExportAndParse() {
        val students = listOf(
            StudentEntity(fullName = "Muhammad Ali", nisn = "123456", birthPlace = "Way Bungur", birthDate = "01/01/2016", classLevel = 2, rombel = "Al Malik", gender = "Laki-laki")
        )
        val csv = CsvHelper.exportStudentsToCsv(students)
        assertTrue(csv.contains("Muhammad Ali"))
        assertTrue(csv.contains("123456"))

        val parsed = CsvHelper.parseStudentsCsv(csv)
        assertEquals(1, parsed.size)
        assertEquals("Muhammad Ali", parsed[0].fullName)
    }

    @Test
    fun testPrayerTimesCalculationOffline() {
        val prayerTimes = PrayerTimesCalculator.calculate()
        assertNotNull(prayerTimes.subuh)
        assertNotNull(prayerTimes.dzuhur)
        assertNotNull(prayerTimes.ashar)
        assertNotNull(prayerTimes.maghrib)
        assertNotNull(prayerTimes.isya)
        assertTrue(prayerTimes.subuh.contains("WIB"))
    }

    @Test
    fun testGoogleSheetsHelperExtractSpreadsheetId() {
        val fullUrl = "https://docs.google.com/spreadsheets/d/1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms/edit#gid=0"
        val extracted = com.example.util.GoogleSheetsHelper.extractSpreadsheetId(fullUrl)
        assertEquals("1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms", extracted)

        val directId = "1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms"
        assertEquals(directId, com.example.util.GoogleSheetsHelper.extractSpreadsheetId(directId))

        val webUrl = com.example.util.GoogleSheetsHelper.getSpreadsheetWebUrl(fullUrl)
        assertEquals("https://docs.google.com/spreadsheets/d/1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms/edit", webUrl)

        val csvUrl = com.example.util.GoogleSheetsHelper.getPublicCsvExportUrl(directId, "Siswa")
        assertTrue(csvUrl.contains("out:csv"))
        assertTrue(csvUrl.contains("sheet=Siswa"))
    }
}
