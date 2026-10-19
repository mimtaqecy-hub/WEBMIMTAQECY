package com.example.util

import com.example.data.local.StudentEntity
import com.example.data.local.TeacherEntity
import com.example.data.local.PpdbEntity
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.util.zip.ZipInputStream

object CsvHelper {

    fun generateStudentCsvTemplate(): String {
        return buildString {
            append("Nama Lengkap,NISN,Tempat Lahir,Tanggal Lahir,Kelas,Rombel,Jenis Kelamin,Alamat\n")
            append("Ahmad Al-Fatih,3150012345,Lampung Timur,15/05/2017,1,Ar Rahman,Laki-laki,Tanjung Qencono\n")
            append("Nurul Hasanah,3150012346,Way Bungur,20/08/2017,1,Ar Rahiim,Perempuan,Way Bungur\n")
        }
    }

    fun exportStudentsToCsv(students: List<StudentEntity>): String {
        return buildString {
            append("Nama Lengkap,NISN,Tempat Lahir,Tanggal Lahir,Kelas,Rombel,Jenis Kelamin,Alamat\n")
            students.forEach { s ->
                append("\"${s.fullName}\",\"${s.nisn}\",\"${s.birthPlace}\",\"${s.birthDate}\",${s.classLevel},\"${s.rombel}\",\"${s.gender}\",\"${s.address}\"\n")
            }
        }
    }

    fun exportPpdbToCsv(list: List<PpdbEntity>): String {
        return buildString {
            append("No,No Pendaftaran,Nama Siswa,NISN,NIK,TTL,JK,Nama Ayah,Nama Ibu,WhatsApp,Status,Tanggal Daftar\n")
            list.forEachIndexed { idx, p ->
                append("${idx + 1},\"${p.regNumber}\",\"${p.studentName}\",\"${p.nisn}\",\"${p.nik}\",\"${p.birthPlace}, ${p.birthDate}\",\"${p.gender}\",\"${p.fatherName}\",\"${p.motherName}\",\"${p.whatsapp}\",\"${p.status}\",\"${p.regDate}\"\n")
            }
        }
    }

    fun exportTeachersToCsv(list: List<TeacherEntity>): String {
        return buildString {
            append("No,Nama Lengkap,Jabatan,Mata Pelajaran,Keterangan,Kontak\n")
            list.forEachIndexed { idx, t ->
                append("${idx + 1},\"${t.name}\",\"${t.role}\",\"${t.subject}\",\"${t.description}\",\"${t.phone}\"\n")
            }
        }
    }

    fun parseStudentsFromInputStream(inputStream: InputStream, fileName: String?): List<StudentEntity> {
        val nameLower = fileName?.lowercase() ?: ""
        return if (nameLower.endsWith(".xlsx")) {
            parseStudentsXlsx(inputStream)
        } else {
            val text = inputStream.bufferedReader(Charsets.UTF_8).readText()
            parseStudentsCsv(text)
        }
    }

    fun parseStudentsCsv(csvContent: String): List<StudentEntity> {
        val lines = csvContent.lines()
        val result = mutableListOf<StudentEntity>()
        if (lines.isEmpty()) return result

        val startIndex = if (lines[0].contains("Nama", ignoreCase = true)) 1 else 0

        for (i in startIndex until lines.size) {
            val line = lines[i].trim()
            if (line.isBlank()) continue

            // Determine delimiter: semicolon or comma
            val delimiter = if (line.count { it == ';' } > line.count { it == ',' }) ';' else ','
            val tokens = parseCsvLine(line, delimiter)
            if (tokens.size >= 5) {
                val hasNoCol = tokens[0].toIntOrNull() != null && tokens.size >= 7
                val offset = if (hasNoCol) 1 else 0

                val name = tokens.getOrNull(offset)?.trim() ?: ""
                val nisn = tokens.getOrNull(offset + 1)?.trim() ?: ""
                val bPlace = tokens.getOrNull(offset + 2)?.trim() ?: "Lampung Timur"
                val bDate = tokens.getOrNull(offset + 3)?.trim() ?: "01/01/2017"
                val classLvl = tokens.getOrNull(offset + 4)?.trim()?.toIntOrNull() ?: 1
                val rombel = tokens.getOrNull(offset + 5)?.trim() ?: "Ar Rahman"
                val gender = tokens.getOrNull(offset + 6)?.trim() ?: "Laki-laki"
                val address = tokens.getOrNull(offset + 7)?.trim() ?: "Tanjung Qencono"

                if (name.isNotEmpty() && !name.equals("Nama Lengkap", ignoreCase = true)) {
                    result.add(
                        StudentEntity(
                            fullName = name,
                            nisn = nisn,
                            birthPlace = bPlace,
                            birthDate = bDate,
                            classLevel = classLvl.coerceIn(1, 6),
                            rombel = rombel,
                            gender = gender,
                            address = address
                        )
                    )
                }
            }
        }
        return result
    }

    private fun parseCsvLine(line: String, delimiter: Char = ','): List<String> {
        val tokens = mutableListOf<String>()
        val sb = StringBuilder()
        var inQuotes = false

        for (char in line) {
            when {
                char == '\"' -> inQuotes = !inQuotes
                char == delimiter && !inQuotes -> {
                    tokens.add(sb.toString().trim())
                    sb.clear()
                }
                else -> sb.append(char)
            }
        }
        tokens.add(sb.toString().trim())
        return tokens
    }

    /**
     * Parse Excel .xlsx file by unpacking its OpenXML Zip stream
     */
    private fun parseStudentsXlsx(inputStream: InputStream): List<StudentEntity> {
        val result = mutableListOf<StudentEntity>()
        val sharedStrings = mutableListOf<String>()
        val rows = mutableListOf<List<String>>()

        try {
            val bytes = inputStream.readBytes()
            // First pass: extract shared strings
            ZipInputStream(bytes.inputStream()).use { zip ->
                var entry = zip.nextEntry
                while (entry != null) {
                    if (entry.name.equals("xl/sharedStrings.xml", ignoreCase = true)) {
                        val xml = BufferedReader(InputStreamReader(zip, Charsets.UTF_8)).readText()
                        val regex = Regex("<t(?:\\s+[^>]*)?>(.*?)</t>", RegexOption.DOT_MATCHES_ALL)
                        regex.findAll(xml).forEach { match ->
                            sharedStrings.add(match.groupValues[1])
                        }
                    }
                    entry = zip.nextEntry
                }
            }

            // Second pass: extract sheet1 rows
            ZipInputStream(bytes.inputStream()).use { zip ->
                var entry = zip.nextEntry
                while (entry != null) {
                    if (entry.name.equals("xl/worksheets/sheet1.xml", ignoreCase = true)) {
                        val xml = BufferedReader(InputStreamReader(zip, Charsets.UTF_8)).readText()
                        val rowRegex = Regex("<row[^>]*>(.*?)</row>", RegexOption.DOT_MATCHES_ALL)
                        val cellRegex = Regex("<c\\s+([^>]*)>(.*?)</c>", RegexOption.DOT_MATCHES_ALL)

                        rowRegex.findAll(xml).forEach { rowMatch ->
                            val rowContent = rowMatch.groupValues[1]
                            val cellValues = mutableListOf<String>()

                            cellRegex.findAll(rowContent).forEach { cellMatch ->
                                val attr = cellMatch.groupValues[1]
                                val body = cellMatch.groupValues[2]

                                val isShared = attr.contains("t=\"s\"")
                                val isInline = attr.contains("t=\"inlineStr\"")

                                if (isShared) {
                                    val v = Regex("<v>(.*?)</v>").find(body)?.groupValues?.get(1)?.toIntOrNull()
                                    val text = if (v != null && v < sharedStrings.size) sharedStrings[v] else ""
                                    cellValues.add(text)
                                } else if (isInline) {
                                    val text = Regex("<t[^>]*>(.*?)</t>").find(body)?.groupValues?.get(1) ?: ""
                                    cellValues.add(text)
                                } else {
                                    val v = Regex("<v>(.*?)</v>").find(body)?.groupValues?.get(1) ?: ""
                                    cellValues.add(v)
                                }
                            }
                            if (cellValues.isNotEmpty()) {
                                rows.add(cellValues)
                            }
                        }
                    }
                    entry = zip.nextEntry
                }
            }

            // Map rows to StudentEntity
            val startIdx = if (rows.isNotEmpty() && rows[0].any { it.contains("Nama", ignoreCase = true) }) 1 else 0
            for (i in startIdx until rows.size) {
                val tokens = rows[i]
                if (tokens.size >= 5) {
                    val hasNoCol = tokens[0].toIntOrNull() != null && tokens.size >= 7
                    val offset = if (hasNoCol) 1 else 0

                    val name = tokens.getOrNull(offset)?.trim() ?: ""
                    val nisn = tokens.getOrNull(offset + 1)?.trim() ?: ""
                    val bPlace = tokens.getOrNull(offset + 2)?.trim() ?: "Lampung Timur"
                    val bDate = tokens.getOrNull(offset + 3)?.trim() ?: "01/01/2017"
                    val classLvl = tokens.getOrNull(offset + 4)?.trim()?.toIntOrNull() ?: 1
                    val rombel = tokens.getOrNull(offset + 5)?.trim() ?: "Ar Rahman"
                    val gender = tokens.getOrNull(offset + 6)?.trim() ?: "Laki-laki"
                    val address = tokens.getOrNull(offset + 7)?.trim() ?: "Tanjung Qencono"

                    if (name.isNotEmpty() && !name.equals("Nama Lengkap", ignoreCase = true)) {
                        result.add(
                            StudentEntity(
                                fullName = name,
                                nisn = nisn,
                                birthPlace = bPlace,
                                birthDate = bDate,
                                classLevel = classLvl.coerceIn(1, 6),
                                rombel = rombel,
                                gender = gender,
                                address = address
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }
}
