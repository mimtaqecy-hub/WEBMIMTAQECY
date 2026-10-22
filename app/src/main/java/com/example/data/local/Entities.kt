package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "school_settings")
data class SchoolSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val schoolName: String = "MI MUHAMMADIYAH TANJUNG QENCONO",
    val address: String = "Jl. Pendidikan No.01 Desa Tanjung Qencono, Kec. Way Bungur, Kab. Lampung Timur",
    val phoneWa: String = "0853-7802-9852",
    val email: String = "mimtaqecy@gmail.com",
    val slogan: String = "Religi, Cerdas, dan Berprestasi",
    val accreditation: String = "B (Terakreditasi Baik Sekali)",
    val foundedYear: String = "1988",
    val headmasterName: String = "Ahmad Syaifuddin, S.Pd.I",
    val headmasterSpeech: String = "Assalamu'alaikum Warahmatullahi Wabarakatuh. Selamat datang di portal resmi MI Muhammadiyah Tanjung Qencono. Kami berkomitmen menyelenggarakan pendidikan dasar Islam terpadu yang memadukan keimanan, kecerdasan intelektual, serta keterampilan hidup untuk mewujudkan generasi qur'ani yang mandiri dan berakhlak mulia.",
    val historyText: String = "MI Muhammadiyah Tanjung Qencono didirikan pada tahun 1988 oleh para tokoh Muhammadiyah Way Bungur Lampung Timur sebagai ikhtiar dakwah pendidikan Islam di desa Tanjung Qencono. Dari tahun ke tahun madrasah terus berbenah dan dipercaya masyarakat luas mendidik putra-putri berilmu amaliyah dan beramal ilmiah.",
    val visionText: String = "Terwujudnya Generasi Muslim yang Taqwa, Cerdas, Berakhlakul Karimah, Mandiri, dan Berprestasi Unggul.",
    val missionText: String = "1. Menumbuhkan penghayatan dan pengamalan ajaran Islam melalui Al-Qur'an dan Sunnah.\n2. Melaksanakan pembelajaran aktif, inovatif, kreatif, dan menyenangkan.\n3. Membiasakan sholat berjamaah, hafalan juz 30, dan adab islami harian.\n4. Mengembangkan potensi bakat, minat, seni, olahraga, dan teknologi peserta didik.",
    val goalsText: String = "Melahirkan lulusan berakhlak mulia, mampu membaca dan menghafal Al-Qur'an dengan tartil, berprestasi akademik dan non-akademik, serta siap melanjutkan ke jenjang pendidikan lanjutan dengan pondasi karakter yang kokoh.",
    val bgPreset: String = "Gedung Madrasah",
    val examUrl: String = "https://script.google.com/macros/s/AKfycbydGFMEnESdvngL0n7v2T0snZr8WmT4cHV0BmtjQhommDpqjTg-tj8Wit6eDPtBFuK3/exec",
    val rppBuilderUrl: String = "https://script.google.com/macros/s/AKfycbzTsiL8Yt80ccACn38TwxsQixNqkiuEo41yRQVubgFU0tbA9VzKvT0c8dh_9q7cJUj0/exec",
    val gasWebAppUrl: String = "",
    val googleSpreadsheetId: String = "",
    val autoSyncPpdbToSheet: Boolean = true,
    val lastSheetSyncTime: String = "",
    val vercelWebUrl: String = "https://mim-tanjung-qencono.vercel.app",
    val adminPassword: String = "MIMTAQECY",
    val isMusicPlaying: Boolean = false,
    val musicVolume: Float = 0.5f,
    val morningAssemblyTime: String = "07.00 - 07.15 WIB",
    val classStartTime: String = "07.15 WIB",
    val classEndTimeMonThu: String = "12.30 WIB",
    val classEndTimeFri: String = "11.00 WIB",
    val classEndTimeSat: String = "12.00 WIB",
    val npsn: String = "60721234",
    val nsm: String = "111218070001",
    val schoolStatus: String = "Swasta (Diakui Dikdasmen)",
    val academicYear: String = "2025/2026",
    val headmasterPhotoUri: String = "",
    val schoolScheduleRules: String = "Siswa wajib hadir pukul 06.45 - 07.00 WIB. Pukul 07.00 WIB gerbang ditutup untuk apel pagi dan sholat dhuha berjamaah.",
    val customAudioUri: String = "",
    val customAudioName: String = "Mars Madrasah (Default)",
    val dailyTausiyah: String = "Menuntut ilmu adalah kewajiban bagi setiap muslim. Siapa yang menempuh jalan untuk menuntut ilmu, Allah mudahkan baginya jalan menuju surga. (HR. Muslim)",
    val facebookUrl: String = "https://www.facebook.com/share/1DxkoYBNkL/",
    val instagramUrl: String = "https://www.instagram.com/mim_taqecy?rpxt=MXg5NG1saWZ2Z2l4MQ==",
    val tiktokUrl: String = "https://www.tiktok.com/@mim_tanjung.qencono?_r=1&_t=ZS-9AiTueWtGGj"
)

@Entity(tableName = "students")
data class StudentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fullName: String,
    val nisn: String,
    val birthPlace: String,
    val birthDate: String,
    val classLevel: Int, // 1 to 6
    val rombel: String,  // e.g. Ar Rahman, Ar Rahiim, etc.
    val gender: String,  // "Laki-laki" / "Perempuan"
    val address: String = "Tanjung Qencono",
    val isPublished: Boolean = true,
    val cachedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "teachers")
data class TeacherEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val role: String,
    val subject: String,
    val photoUrl: String = "",
    val description: String = "",
    val phone: String = "",
    val cachedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "ppdb")
data class PpdbEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val regNumber: String, // PPDB-2026-001
    val studentName: String,
    val nisn: String,
    val nik: String,
    val birthPlace: String,
    val birthDate: String,
    val gender: String,
    val address: String,
    val whatsapp: String,
    val prevSchool: String,
    val fatherName: String,
    val fatherNik: String,
    val fatherBirthPlace: String,
    val fatherBirthDate: String,
    val fatherEducation: String,
    val fatherJob: String,
    val motherName: String,
    val motherNik: String,
    val motherBirthPlace: String,
    val motherBirthDate: String,
    val motherEducation: String,
    val motherJob: String,
    val kkDocName: String = "KK_terlampir.pdf",
    val ijazahDocName: String = "Ijazah_terlampir.pdf",
    val aktaDocName: String = "Akta_terlampir.pdf",
    val studentPhotoUri: String = "",
    val status: String = "Menunggu Verifikasi", // Menunggu Verifikasi, Diterima, Cadangan, Ditolak
    val adminNotes: String = "",
    val regDate: String
)

@Entity(tableName = "rpp")
data class RppEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val teacherName: String,
    val classLevel: Int,
    val rombel: String,
    val subject: String,
    val uploadDate: String,
    val fileName: String,
    val fileSize: String,
    val fileType: String
)

@Entity(tableName = "documents")
data class DocumentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val folderName: String,
    val classLevel: String = "Semua",
    val uploadDate: String,
    val fileName: String,
    val fileType: String,
    val fileSize: String
)

@Entity(tableName = "news")
data class NewsEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val category: String, // "Kegiatan", "Prestasi", "Pengumuman", "Madrasah"
    val summary: String,
    val content: String,
    val date: String,
    val isPublished: Boolean = true,
    val imageUrl: String = "",
    val cachedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "gallery")
data class GalleryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val category: String, // "Kegiatan", "Pembelajaran", "Upacara", "Prestasi", "Ekstrakurikuler", "Fasilitas"
    val description: String,
    val date: String,
    val imageUrl: String = "",
    val isPublished: Boolean = true
)

@Entity(tableName = "extracurriculars")
data class ExtracurricularEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val coach: String,
    val schedule: String,
    val description: String,
    val imageUrl: String = "",
    val isPublished: Boolean = true
)

@Entity(tableName = "featured_programs")
data class FeaturedProgramEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val objective: String,
    val description: String,
    val imageUrl: String = "",
    val isPublished: Boolean = true
)

@Entity(tableName = "agendas")
data class AgendaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val date: String,
    val time: String,
    val location: String,
    val imageUrl: String = "",
    val isPublished: Boolean = true
)

@Entity(tableName = "testimonials")
data class TestimonialEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val role: String,
    val content: String,
    val rating: Int = 5,
    val isApproved: Boolean = false,
    val date: String
)

@Entity(tableName = "contact_messages")
data class ContactMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val senderName: String,
    val email: String,
    val phone: String,
    val subject: String,
    val message: String,
    val date: String,
    val isRead: Boolean = false
)

@Entity(tableName = "admin_logs")
data class AdminLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: String,
    val action: String,
    val details: String
)

@Entity(tableName = "class_schedules")
data class ClassScheduleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val classLevel: Int,
    val rombel: String,
    val day: String, // Senin, Selasa, Rabu, Kamis, Jumat, Sabtu
    val timeSlot: String,
    val subject: String,
    val teacherName: String
)

@Entity(tableName = "facilities")
data class FacilityEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String = "Sarana Utama", // Sarana Utama, Pembelajaran, Olahraga, Ibadah, Penunjang
    val description: String,
    val condition: String = "Sangat Baik", // Sangat Baik, Baik, Dalam Pemeliharaan
    val imageUrl: String = "",
    val capacity: String = "", // e.g. "30 Siswa", "150 Jamaah", "40 Komputer"
    val isPublished: Boolean = true
)

@Entity(tableName = "app_notifications")
data class AppNotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val message: String,
    val category: String = "Pengumuman", // Pengumuman, Akademik, Jadwal Masuk, Kegiatan, Darurat
    val timestamp: Long = System.currentTimeMillis(),
    val dateFormatted: String = "",
    val isRead: Boolean = false,
    val targetAudience: String = "Semua (Wali Murid & Siswa)",
    val soundAlert: Boolean = true
)
