package com.example.util

import com.example.data.local.ContactMessageEntity
import com.example.data.local.PpdbEntity
import com.example.data.local.StudentEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

sealed class SheetResult<out T> {
    data class Success<T>(val data: T, val message: String) : SheetResult<T>()
    data class Error(val message: String, val throwable: Throwable? = null) : SheetResult<Nothing>()
}

object GoogleSheetsHelper {

    private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(25, TimeUnit.SECONDS)
            .readTimeout(25, TimeUnit.SECONDS)
            .writeTimeout(25, TimeUnit.SECONDS)
            .followRedirects(true)
            .followSslRedirects(true)
            .build()
    }

    /**
     * Extracts pure Spreadsheet ID from a link or raw string.
     * Example: https://docs.google.com/spreadsheets/d/1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms/edit#gid=0
     * Returns: 1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms
     */
    fun extractSpreadsheetId(input: String): String {
        val trimmed = input.trim()
        if (trimmed.isBlank()) return ""
        val regex = Regex("/spreadsheets/d/([a-zA-Z0-9-_]+)")
        val match = regex.find(trimmed)
        if (match != null) {
            return match.groupValues[1]
        }
        // If it's already an alphanumeric ID without slashes
        if (!trimmed.contains("/") && !trimmed.contains(" ") && trimmed.length > 15) {
            return trimmed
        }
        return trimmed
    }

    fun getSpreadsheetWebUrl(spreadsheetIdOrUrl: String): String {
        val id = extractSpreadsheetId(spreadsheetIdOrUrl)
        return if (id.isNotBlank()) "https://docs.google.com/spreadsheets/d/$id/edit" else ""
    }

    fun getPublicCsvExportUrl(spreadsheetIdOrUrl: String, sheetName: String = "Siswa"): String {
        val id = extractSpreadsheetId(spreadsheetIdOrUrl)
        return "https://docs.google.com/spreadsheets/d/$id/gviz/tq?tqx=out:csv&sheet=$sheetName"
    }

    /**
     * Ping test to verify Google Apps Script web app endpoint connection.
     */
    suspend fun testConnection(gasUrl: String): SheetResult<String> = withContext(Dispatchers.IO) {
        val cleanUrl = gasUrl.trim()
        if (cleanUrl.isBlank()) {
            return@withContext SheetResult.Error("URL Google Apps Script belum diisi.")
        }
        try {
            val startTime = System.currentTimeMillis()
            val urlWithAction = if (cleanUrl.contains("?")) "$cleanUrl&action=ping" else "$cleanUrl?action=ping"
            val request = Request.Builder()
                .url(urlWithAction)
                .get()
                .build()

            val response = client.newCall(request).execute()
            val duration = System.currentTimeMillis() - startTime
            val body = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return@withContext SheetResult.Error("Gagal terhubung: HTTP ${response.code} - ${response.message}")
            }

            // Check if JSON response returned
            return@withContext try {
                val json = JSONObject(body)
                val status = json.optString("status", "unknown")
                val msg = json.optString("message", "Koneksi berhasil")
                val ssName = json.optString("spreadsheetName", "")
                if (status == "success" || status == "ok") {
                    val detail = if (ssName.isNotBlank()) " ke Spreadsheet: $ssName" else ""
                    SheetResult.Success(body, "Koneksi Google Sheets Aktif ($duration ms)$detail")
                } else {
                    SheetResult.Error("Server merespon: $msg")
                }
            } catch (e: Exception) {
                // If it returned HTML or non-JSON
                if (body.contains("Google Drive") || body.contains("script.google.com")) {
                    SheetResult.Success(body, "Terhubung ke endpoint Google Script ($duration ms)")
                } else {
                    SheetResult.Success(body, "Endpoint aktif ($duration ms): ${body.take(80)}")
                }
            }
        } catch (e: Exception) {
            SheetResult.Error("Gagal menghubungi Google Apps Script: ${e.localizedMessage ?: e.message}", e)
        }
    }

    /**
     * Push all local PPDB records to Google Sheet in batch.
     */
    suspend fun syncAllPpdb(gasUrl: String, list: List<PpdbEntity>): SheetResult<Int> = withContext(Dispatchers.IO) {
        val cleanUrl = gasUrl.trim()
        if (cleanUrl.isBlank()) return@withContext SheetResult.Error("URL Google Apps Script belum diisi.")
        if (list.isEmpty()) return@withContext SheetResult.Success(0, "Tidak ada data PPDB untuk disinkronkan.")

        try {
            val root = JSONObject()
            root.put("action", "syncAllPpdb")
            val array = JSONArray()
            list.forEach { p ->
                val obj = JSONObject().apply {
                    put("regNumber", p.regNumber)
                    put("studentName", p.studentName)
                    put("nisn", p.nisn)
                    put("nik", p.nik)
                    put("birthPlace", p.birthPlace)
                    put("birthDate", p.birthDate)
                    put("gender", p.gender)
                    put("address", p.address)
                    put("whatsapp", p.whatsapp)
                    put("prevSchool", p.prevSchool)
                    put("fatherName", p.fatherName)
                    put("motherName", p.motherName)
                    put("status", p.status)
                    put("regDate", p.regDate)
                    put("notes", p.adminNotes)
                }
                array.put(obj)
            }
            root.put("data", array)

            val body = root.toString().toRequestBody(JSON_MEDIA_TYPE)
            val request = Request.Builder().url(cleanUrl).post(body).build()
            val response = client.newCall(request).execute()
            val respBody = response.body?.string() ?: ""

            if (response.isSuccessful) {
                SheetResult.Success(list.size, "Berhasil menyinkronkan ${list.size} pendaftar PPDB ke Google Sheet!")
            } else {
                SheetResult.Error("Gagal sinkron PPDB: HTTP ${response.code} - $respBody")
            }
        } catch (e: Exception) {
            SheetResult.Error("Gagal sinkron PPDB ke Google Sheet: ${e.localizedMessage ?: e.message}", e)
        }
    }

    /**
     * Submit a single PPDB record to Google Sheet (e.g. when user submits form online).
     */
    suspend fun submitSinglePpdb(gasUrl: String, ppdb: PpdbEntity): SheetResult<String> = withContext(Dispatchers.IO) {
        val cleanUrl = gasUrl.trim()
        if (cleanUrl.isBlank()) return@withContext SheetResult.Error("URL Google Apps Script kosong.")

        try {
            val root = JSONObject()
            root.put("action", "submitPpdb")
            val d = JSONObject().apply {
                put("regNumber", ppdb.regNumber)
                put("studentName", ppdb.studentName)
                put("nisn", ppdb.nisn)
                put("nik", ppdb.nik)
                put("birthPlace", ppdb.birthPlace)
                put("birthDate", ppdb.birthDate)
                put("gender", ppdb.gender)
                put("address", ppdb.address)
                put("whatsapp", ppdb.whatsapp)
                put("prevSchool", ppdb.prevSchool)
                put("fatherName", ppdb.fatherName)
                put("motherName", ppdb.motherName)
                put("status", ppdb.status)
                put("regDate", ppdb.regDate)
            }
            root.put("data", d)

            val body = root.toString().toRequestBody(JSON_MEDIA_TYPE)
            val request = Request.Builder().url(cleanUrl).post(body).build()
            val response = client.newCall(request).execute()

            if (response.isSuccessful) {
                SheetResult.Success(ppdb.regNumber, "Data PPDB ${ppdb.regNumber} berhasil terkirim ke Google Sheet!")
            } else {
                SheetResult.Error("Respon Google Script: HTTP ${response.code}")
            }
        } catch (e: Exception) {
            SheetResult.Error("Gagal mengirim PPDB ke Google Sheet: ${e.localizedMessage ?: e.message}", e)
        }
    }

    /**
     * Push all local students to Google Sheet tab "Siswa".
     */
    suspend fun syncAllStudents(gasUrl: String, list: List<StudentEntity>): SheetResult<Int> = withContext(Dispatchers.IO) {
        val cleanUrl = gasUrl.trim()
        if (cleanUrl.isBlank()) return@withContext SheetResult.Error("URL Google Apps Script belum diisi.")
        if (list.isEmpty()) return@withContext SheetResult.Success(0, "Tidak ada data siswa untuk disinkronkan.")

        try {
            val root = JSONObject()
            root.put("action", "syncAllStudents")
            val array = JSONArray()
            list.forEach { s ->
                val obj = JSONObject().apply {
                    put("fullName", s.fullName)
                    put("nisn", s.nisn)
                    put("birthPlace", s.birthPlace)
                    put("birthDate", s.birthDate)
                    put("classLevel", s.classLevel)
                    put("rombel", s.rombel)
                    put("gender", s.gender)
                    put("address", s.address)
                }
                array.put(obj)
            }
            root.put("data", array)

            val body = root.toString().toRequestBody(JSON_MEDIA_TYPE)
            val request = Request.Builder().url(cleanUrl).post(body).build()
            val response = client.newCall(request).execute()

            if (response.isSuccessful) {
                SheetResult.Success(list.size, "Berhasil menyinkronkan ${list.size} siswa ke Google Sheet tab 'Siswa'!")
            } else {
                SheetResult.Error("Respon server: HTTP ${response.code}")
            }
        } catch (e: Exception) {
            SheetResult.Error("Gagal sinkron siswa ke Google Sheet: ${e.localizedMessage ?: e.message}", e)
        }
    }

    /**
     * Fetch students directly from a Google Sheet via public CSV export link.
     */
    suspend fun fetchStudentsFromPublicCsv(spreadsheetIdOrUrl: String, sheetName: String = "Siswa"): SheetResult<List<StudentEntity>> = withContext(Dispatchers.IO) {
        val url = getPublicCsvExportUrl(spreadsheetIdOrUrl, sheetName)
        try {
            val request = Request.Builder().url(url).get().build()
            val response = client.newCall(request).execute()
            val csvText = response.body?.string() ?: ""

            if (!response.isSuccessful || csvText.isBlank()) {
                return@withContext SheetResult.Error("Gagal mengunduh CSV dari Google Sheet: HTTP ${response.code}")
            }

            val parsed = CsvHelper.parseStudentsCsv(csvText)
            if (parsed.isEmpty()) {
                SheetResult.Error("File CSV dari Google Sheet berhasil diakses tetapi tidak ditemukan baris data siswa yang valid. Pastikan header dan nama tab sesuai.")
            } else {
                SheetResult.Success(parsed, "Berhasil membaca ${parsed.size} data siswa dari Google Sheet!")
            }
        } catch (e: Exception) {
            SheetResult.Error("Gagal membaca Google Sheet via CSV: ${e.localizedMessage ?: e.message}", e)
        }
    }

    /**
     * Fetch students from Google Apps Script Web App endpoint.
     */
    suspend fun fetchStudentsFromGas(gasUrl: String): SheetResult<List<StudentEntity>> = withContext(Dispatchers.IO) {
        val cleanUrl = gasUrl.trim()
        if (cleanUrl.isBlank()) return@withContext SheetResult.Error("URL Google Apps Script belum diisi.")
        try {
            val urlWithAction = if (cleanUrl.contains("?")) "$cleanUrl&action=getStudents" else "$cleanUrl?action=getStudents"
            val request = Request.Builder().url(urlWithAction).get().build()
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return@withContext SheetResult.Error("Respon server: HTTP ${response.code}")
            }

            val json = JSONObject(body)
            val dataArray = json.optJSONArray("data") ?: return@withContext SheetResult.Error("Data siswa tidak ditemukan di respon Google Sheet.")
            val list = mutableListOf<StudentEntity>()

            for (i in 0 until dataArray.length()) {
                val item = dataArray.opt(i)
                if (item is JSONObject) {
                    val name = item.optString("fullName", "").ifBlank { item.optString("nama", "") }
                    if (name.isNotBlank()) {
                        list.add(
                            StudentEntity(
                                fullName = name,
                                nisn = item.optString("nisn", ""),
                                birthPlace = item.optString("birthPlace", "Lampung Timur"),
                                birthDate = item.optString("birthDate", ""),
                                classLevel = item.optInt("classLevel", 1).coerceIn(1, 6),
                                rombel = item.optString("rombel", "Ar Rahman"),
                                gender = item.optString("gender", "Laki-laki"),
                                address = item.optString("address", "Tanjung Qencono")
                            )
                        )
                    }
                } else if (item is JSONArray && i > 0) { // Header skipped at 0
                    val name = item.optString(0, "")
                    if (name.isNotBlank() && !name.contains("Nama", ignoreCase = true)) {
                        list.add(
                            StudentEntity(
                                fullName = name,
                                nisn = item.optString(1, ""),
                                birthPlace = item.optString(2, "Lampung Timur"),
                                birthDate = item.optString(3, ""),
                                classLevel = item.optString(4, "1").toIntOrNull()?.coerceIn(1, 6) ?: 1,
                                rombel = item.optString(5, "Ar Rahman"),
                                gender = item.optString(6, "Laki-laki"),
                                address = item.optString(7, "Tanjung Qencono")
                            )
                        )
                    }
                }
            }

            SheetResult.Success(list, "Berhasil memuat ${list.size} siswa dari Google Sheet!")
        } catch (e: Exception) {
            SheetResult.Error("Gagal mengambil data siswa: ${e.localizedMessage ?: e.message}", e)
        }
    }

    /**
     * Submit contact message to Google Sheet.
     */
    suspend fun submitContactMessage(gasUrl: String, msg: ContactMessageEntity): SheetResult<String> = withContext(Dispatchers.IO) {
        val cleanUrl = gasUrl.trim()
        if (cleanUrl.isBlank()) return@withContext SheetResult.Error("URL Google Apps Script kosong.")
        try {
            val root = JSONObject()
            root.put("action", "submitContact")
            val d = JSONObject().apply {
                put("senderName", msg.senderName)
                put("email", msg.email)
                put("phone", msg.phone)
                put("subject", msg.subject)
                put("message", msg.message)
                put("date", msg.date)
            }
            root.put("data", d)

            val body = root.toString().toRequestBody(JSON_MEDIA_TYPE)
            val request = Request.Builder().url(cleanUrl).post(body).build()
            val response = client.newCall(request).execute()

            if (response.isSuccessful) {
                SheetResult.Success("Terkirim", "Pesan berhasil dicadangkan ke Google Sheet!")
            } else {
                SheetResult.Error("HTTP ${response.code}")
            }
        } catch (e: Exception) {
            SheetResult.Error("Gagal kirim pesan ke Google Sheet: ${e.localizedMessage ?: e.message}", e)
        }
    }
}
