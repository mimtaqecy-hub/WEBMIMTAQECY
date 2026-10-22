package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        SchoolSettingsEntity::class,
        StudentEntity::class,
        TeacherEntity::class,
        PpdbEntity::class,
        RppEntity::class,
        DocumentEntity::class,
        NewsEntity::class,
        GalleryEntity::class,
        ExtracurricularEntity::class,
        FeaturedProgramEntity::class,
        AgendaEntity::class,
        TestimonialEntity::class,
        ContactMessageEntity::class,
        AdminLogEntity::class,
        ClassScheduleEntity::class,
        FacilityEntity::class,
        AppNotificationEntity::class
    ],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun schoolSettingsDao(): SchoolSettingsDao
    abstract fun studentDao(): StudentDao
    abstract fun teacherDao(): TeacherDao
    abstract fun ppdbDao(): PpdbDao
    abstract fun rppDao(): RppDao
    abstract fun documentDao(): DocumentDao
    abstract fun newsDao(): NewsDao
    abstract fun galleryDao(): GalleryDao
    abstract fun extracurricularDao(): ExtracurricularDao
    abstract fun featuredProgramDao(): FeaturedProgramDao
    abstract fun agendaDao(): AgendaDao
    abstract fun testimonialDao(): TestimonialDao
    abstract fun contactMessageDao(): ContactMessageDao
    abstract fun adminLogDao(): AdminLogDao
    abstract fun classScheduleDao(): ClassScheduleDao
    abstract fun facilityDao(): FacilityDao
    abstract fun appNotificationDao(): AppNotificationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mim_tq_database.db"
                )
                    .addCallback(DatabaseCallback(scope))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateDatabase(database)
                    }
                }
            }

            suspend fun populateDatabase(db: AppDatabase) {
                // Initial Settings
                db.schoolSettingsDao().insertOrUpdate(SchoolSettingsEntity())

                // Initial Students
                val initialStudents = listOf(
                    StudentEntity(fullName = "Muhammad Fatih Al-Ayyubi", nisn = "3154829101", birthPlace = "Lampung Timur", birthDate = "12 Mei 2017", classLevel = 1, rombel = "Ar Rahman", gender = "Laki-laki"),
                    StudentEntity(fullName = "Aisyah Humaira Putri", nisn = "3154829102", birthPlace = "Way Bungur", birthDate = "25 Juli 2017", classLevel = 1, rombel = "Ar Rahiim", gender = "Perempuan"),
                    StudentEntity(fullName = "Ahmad Zaki Mubarak", nisn = "3144829201", birthPlace = "Tanjung Qencono", birthDate = "10 Januari 2016", classLevel = 2, rombel = "Al Malik", gender = "Laki-laki"),
                    StudentEntity(fullName = "Fatimah Az-Zahra", nisn = "3144829202", birthPlace = "Way Bungur", birthDate = "18 Maret 2016", classLevel = 2, rombel = "Al Quddus", gender = "Perempuan"),
                    StudentEntity(fullName = "Bilal Ibnu Rabah", nisn = "3134829301", birthPlace = "Lampung Timur", birthDate = "05 Agustus 2015", classLevel = 3, rombel = "As Salaam", gender = "Laki-laki"),
                    StudentEntity(fullName = "Zahra Nailatul Husna", nisn = "3134829302", birthPlace = "Tanjung Qencono", birthDate = "22 September 2015", classLevel = 3, rombel = "Al Mu'min", gender = "Perempuan"),
                    StudentEntity(fullName = "Umar Faruq Pratama", nisn = "3124829401", birthPlace = "Metro", birthDate = "14 Februari 2014", classLevel = 4, rombel = "Al Muhaimin", gender = "Laki-laki"),
                    StudentEntity(fullName = "Khadijah Maryam", nisn = "3124829402", birthPlace = "Way Bungur", birthDate = "30 Juni 2014", classLevel = 4, rombel = "Al Aziz", gender = "Perempuan"),
                    StudentEntity(fullName = "Ali Zainal Abidin", nisn = "3114829501", birthPlace = "Lampung Timur", birthDate = "03 November 2013", classLevel = 5, rombel = "Al Jabbar", gender = "Laki-laki"),
                    StudentEntity(fullName = "Hafidzah Nur Ramadhani", nisn = "3114829502", birthPlace = "Tanjung Qencono", birthDate = "09 April 2013", classLevel = 5, rombel = "Al Mutakabbir", gender = "Perempuan"),
                    StudentEntity(fullName = "Ibrahim Rayhan Al-Ghifari", nisn = "3104829601", birthPlace = "Way Bungur", birthDate = "17 Oktober 2012", classLevel = 6, rombel = "Al Khaliq", gender = "Laki-laki"),
                    StudentEntity(fullName = "Salma Nabila Syakira", nisn = "3104829602", birthPlace = "Lampung Timur", birthDate = "04 Desember 2012", classLevel = 6, rombel = "Al Baari", gender = "Perempuan")
                )
                db.studentDao().insertAll(initialStudents)

                // Initial Teachers & Staff
                val initialTeachers = listOf(
                    TeacherEntity(name = "Ahmad Syaifuddin, S.Pd.I", role = "Kepala Madrasah", subject = "Manajemen & Supervisi", description = "Pengabdi pendidikan madrasah berdedikasi tinggi dengan visi keislaman berkemajuan."),
                    TeacherEntity(name = "Siti Nurhaliza, S.Pd.", role = "Guru Kelas", subject = "Tematik Kelas 1 & 2", description = "Pengampu kelas rendah dengan pendekatan kasih sayang dan pembelajaran interaktif."),
                    TeacherEntity(name = "Ust. Muhammad Ridwan, S.Ag.", role = "Guru PAI & Bahasa Arab", subject = "Al-Qur'an Hadist & Fiqih", description = "Pembina tahfidz Al-Qur'an dan pengkaji tafsir juz 30."),
                    TeacherEntity(name = "Tri Wahyuni, S.Pd.", role = "Guru Kelas", subject = "Tematik Kelas 3 & 4", description = "Spesialis literasi numerasi dasar dan pembelajaran tematik kontekstual."),
                    TeacherEntity(name = "Bambang Setiawan, S.Pd.", role = "Guru Kelas", subject = "Tematik Kelas 5 & 6", description = "Fasilitator persiapan asesmen madrasah dan olimpiade sains."),
                    TeacherEntity(name = "Rahmat Hidayat, S.Or.", role = "Guru Olahraga", subject = "PJOK & Tapak Suci", description = "Pelatih beladiri Tapak Suci dan pembina kebugaran jasmani santri."),
                    TeacherEntity(name = "Dewi Anggraini, S.Kom.", role = "Tenaga Administrasi & Operator", subject = "Operator EMIS, SIMPATIKA & PPDB", description = "Pengelola pangkalan data digital dan layanan informasi madrasah.")
                )
                db.teacherDao().insertAll(initialTeachers)

                // Initial PPDB
                val initialPpdb = listOf(
                    PpdbEntity(
                        regNumber = "PPDB-2026-001",
                        studentName = "Arkan Al-Ghazi",
                        nisn = "3167891234",
                        nik = "1807021508180001",
                        birthPlace = "Lampung Timur",
                        birthDate = "15 Agustus 2018",
                        gender = "Laki-laki",
                        address = "Dusun II Tanjung Qencono, Way Bungur",
                        whatsapp = "085378029852",
                        prevSchool = "TK 'Aisyiyah Bustanul Athfal Way Bungur",
                        fatherName = "Hendra Wijaya",
                        fatherNik = "1807021005850002",
                        fatherBirthPlace = "Way Bungur",
                        fatherBirthDate = "10 Mei 1985",
                        fatherEducation = "S1",
                        fatherJob = "Wiraswasta",
                        motherName = "Sri Wahyuni",
                        motherNik = "1807025206880003",
                        motherBirthPlace = "Metro",
                        motherBirthDate = "12 Juni 1988",
                        motherEducation = "SMA",
                        motherJob = "Ibu Rumah Tangga",
                        status = "Diterima",
                        adminNotes = "Berkas lengkap dan memenuhi kriteria usia serta kesiapan belajar.",
                        regDate = "15 Jan 2026"
                    ),
                    PpdbEntity(
                        regNumber = "PPDB-2026-002",
                        studentName = "Nayla Putri Azzahra",
                        nisn = "3167891235",
                        nik = "1807025809180002",
                        birthPlace = "Way Bungur",
                        birthDate = "28 September 2018",
                        gender = "Perempuan",
                        address = "Jl. Poros Desa Tanjung Qencono",
                        whatsapp = "085269871234",
                        prevSchool = "RA Al-Ikhlas",
                        fatherName = "Sunardi",
                        fatherNik = "1807020404830004",
                        fatherBirthPlace = "Tanjung Qencono",
                        fatherBirthDate = "04 April 1983",
                        fatherEducation = "SMA",
                        fatherJob = "Petani",
                        motherName = "Mariana",
                        motherNik = "1807024808860005",
                        motherBirthPlace = "Way Bungur",
                        motherBirthDate = "18 Agustus 1986",
                        motherEducation = "SMP",
                        motherJob = "Pedagang",
                        status = "Menunggu Verifikasi",
                        adminNotes = "Menunggu verifikasi fotokopi akta kelahiran asli.",
                        regDate = "16 Jan 2026"
                    )
                )
                db.ppdbDao().insert(initialPpdb[0])
                db.ppdbDao().insert(initialPpdb[1])

                // Initial RPP
                val initialRpp = listOf(
                    RppEntity(
                        title = "Modul Ajar PAI: Meneladani Sifat Amanah",
                        teacherName = "Ust. Muhammad Ridwan, S.Ag.",
                        classLevel = 4,
                        rombel = "Al Aziz",
                        subject = "Pendidikan Agama Islam",
                        uploadDate = "10 Jan 2026",
                        fileName = "RPP_PAI_Kelas4_Semester2.pdf",
                        fileSize = "1.8 MB",
                        fileType = "PDF"
                    ),
                    RppEntity(
                        title = "Modul Ajar Tematik: Menyayangi Tumbuhan dan Hewan",
                        teacherName = "Siti Nurhaliza, S.Pd.",
                        classLevel = 3,
                        rombel = "As Salaam",
                        subject = "Tematik",
                        uploadDate = "12 Jan 2026",
                        fileName = "Modul_Tematik_Kelas3.docx",
                        fileSize = "2.4 MB",
                        fileType = "DOCX"
                    ),
                    RppEntity(
                        title = "Perangkat Ajar Bahasa Arab: Fil Fasli (Di Dalam Kelas)",
                        teacherName = "Ust. Muhammad Ridwan, S.Ag.",
                        classLevel = 2,
                        rombel = "Al Malik",
                        subject = "Bahasa Arab",
                        uploadDate = "14 Jan 2026",
                        fileName = "RPP_BahasaArab_Kelas2.pdf",
                        fileSize = "1.2 MB",
                        fileType = "PDF"
                    )
                )
                db.rppDao().insertAll(initialRpp)

                // Initial Documents
                val initialDocs = listOf(
                    DocumentEntity(title = "Kurikulum Operasional Madrasah (KOM) 2025/2026", folderName = "Kurikulum", classLevel = "Semua", uploadDate = "05 Jan 2026", fileName = "KOM_MIM_TQ_2025_2026.pdf", fileType = "PDF", fileSize = "4.2 MB"),
                    DocumentEntity(title = "Kalender Pendidikan Madrasah TA 2025/2026", folderName = "Akademik", classLevel = "Semua", uploadDate = "02 Jan 2026", fileName = "Kalender_Akademik_MIM_TQ.pdf", fileType = "PDF", fileSize = "1.1 MB"),
                    DocumentEntity(title = "Tata Tertib dan Panduan Seragam Santri", folderName = "Tata Usaha", classLevel = "Semua", uploadDate = "03 Jan 2026", fileName = "Tata_Tertib_Santri.pdf", fileType = "PDF", fileSize = "850 KB"),
                    DocumentEntity(title = "Juknis Bantuan Operasional Sekolah (BOS) Kemenag", folderName = "Kemenag", classLevel = "Semua", uploadDate = "08 Jan 2026", fileName = "Juknis_BOS_Madrasah.pdf", fileType = "PDF", fileSize = "3.5 MB")
                )
                db.documentDao().insertAll(initialDocs)

                // Initial News
                val initialNews = listOf(
                    NewsEntity(
                        title = "Penerimaan Peserta Didik Baru (PPDB) Tahun Pelajaran 2026/2027 Resmi Dibuka",
                        category = "Pengumuman",
                        summary = "MI Muhammadiyah Tanjung Qencono membuka pendaftaran santri baru dengan kurikulum terpadu berkarakter Islami.",
                        content = "MI Muhammadiyah Tanjung Qencono kembali membuka pendaftaran bagi calon peserta didik baru (PPDB) tahun pelajaran 2026/2027. Tersedia program unggulan Tahfidz Al-Qur'an Juz 30, pembiasaan sholat fardhu dan dhuha berjamaah, serta fasilitas laboratorium digital.",
                        date = "15 Jan 2026",
                        isPublished = true
                    ),
                    NewsEntity(
                        title = "Santri MIM Tanjung Qencono Raih Juara 1 Lomba Tahfidz Tingkat Kabupaten Lampung Timur",
                        category = "Prestasi",
                        summary = "Prestasi membanggakan kembali diukir ananda Salma Nabila Syakira kelas 6 dalam ajang POSMAD Lampung Timur.",
                        content = "Alhamdulillah, ananda Salma Nabila Syakira perwakilan MI Muhammadiyah Tanjung Qencono berhasil meraih gelar Juara 1 Musabaqah Hifdzil Qur'an (MHQ) 1 Juz tingkat Madrasah Ibtidaiyah se-Kabupaten Lampung Timur. Semoga memotivasi santri lainnya.",
                        date = "08 Jan 2026",
                        isPublished = true
                    ),
                    NewsEntity(
                        title = "Peringatan Milad Muhammadiyah & Pawai Ta'aruf Santri",
                        category = "Kegiatan",
                        summary = "Kemeriahan pawai ta'aruf, bazar amal, dan unjuk kreasi seni santri berlangsung penuh suka cita.",
                        content = "Ratusan santri didampingi dewan guru dan wali murid mengikuti pawai ta'aruf keliling desa Tanjung Qencono mengenakan busana muslim kreasi serta seragam Hizbul Wathan dan Tapak Suci.",
                        date = "18 Des 2025",
                        isPublished = true
                    )
                )
                db.newsDao().insertAll(initialNews)

                // Initial Gallery
                val initialGallery = listOf(
                    GalleryEntity(title = "Upacara Bendera Hari Senin & Pembinaan Karakter", category = "Upacara", description = "Pelaksanaan upacara bendera rutin melatih kedisiplinan dan cinta tanah air.", date = "12 Jan 2026"),
                    GalleryEntity(title = "Halaqah Tahfidz Al-Qur'an Pagi Hari", category = "Pembelajaran", description = "Santri menyetorkan hafalan surat-surat pendek juz 30 di masjid madrasah.", date = "10 Jan 2026"),
                    GalleryEntity(title = "Latihan Kepanduan Hizbul Wathan (HW)", category = "Ekstrakurikuler", description = "Kegiatan kepanduan melatih kemandirian, tali-temali, dan survival islami.", date = "09 Jan 2026"),
                    GalleryEntity(title = "Latihan Seni Beladiri Tapak Suci Putera Muhammadiyah", category = "Ekstrakurikuler", description = "Santri mempraktekkan jurus dasar dengan semangat dengan iman dan akhlaq saya menjadi kuat.", date = "07 Jan 2026"),
                    GalleryEntity(title = "Gedung Ruang Kelas Bersih dan Representatif", category = "Fasilitas", description = "Lingkungan belajar asri dengan sirkulasi udara optimal dan taman madrasah.", date = "05 Jan 2026")
                )
                db.galleryDao().insertAll(initialGallery)

                // Initial Extracurriculars
                val initialExtras = listOf(
                    ExtracurricularEntity(name = "Kepanduan Hizbul Wathan (HW)", coach = "Bambang Setiawan, S.Pd.", schedule = "Jumat, 14.00 - 16.30 WIB", description = "Membentuk kader persyarikatan yang mandiri, berjiwa ksatria, peduli sesama, dan berakhlak mulia."),
                    ExtracurricularEntity(name = "Tapak Suci Putera Muhammadiyah", coach = "Rahmat Hidayat, S.Or. (Kader TS)", schedule = "Sabtu, 14.30 - 16.30 WIB", description = "Seni beladiri pencak silat berasaskan Islam untuk kesehatan jasmani, mental juara, dan ketahanan diri."),
                    ExtracurricularEntity(name = "Tahfidzul Qur'an & Tartil", coach = "Ust. Muhammad Ridwan, S.Ag.", schedule = "Senin - Kamis, 06.30 - 07.15 WIB", description = "Bimbingan intensif hafalan surat juz 30 dengan makhorijul huruf dan kaidah tajwid yang benar."),
                    ExtracurricularEntity(name = "Drumband Gema Madani", coach = "Pelatih Musik Tamu", schedule = "Ahad, 08.00 - 11.00 WIB", description = "Pengembangan musikalitas, kekompakan baris-berbaris, dan irama perkusi islami."),
                    ExtracurricularEntity(name = "Sains & Matematika Cilik", coach = "Tri Wahyuni, S.Pd.", schedule = "Rabu, 13.00 - 14.30 WIB", description = "Eksplorasi eksperimen ilmiah sederhana dan penguatan logika berhitung gembira.")
                )
                db.extracurricularDao().insertAll(initialExtras)

                // Initial Featured Programs
                val initialPrograms = listOf(
                    FeaturedProgramEntity(name = "Tahfidz Al-Qur'an Target Juz 30", objective = "Santri hafal minimal 1 juz sebelum lulus", description = "Target hafalan terstruktur setiap semester dilengkapi buku mutaba'ah harian."),
                    FeaturedProgramEntity(name = "Pembiasaan Sholat Dhuha & Dzuhur Berjamaah", objective = "Menanamkan kecintaan sholat fardhu & sunnah", description = "Setiap santri bergiliran mengumandangkan adzan, iqomah, serta kultum ringkas."),
                    FeaturedProgramEntity(name = "Literasi Karakter & Adab Islami", objective = "Penerapan adab makan, berbicara, dan menghormati sesama", description = "Integrasi 7 pilar adab dalam setiap mata pelajaran dan interaksi sehari-hari."),
                    FeaturedProgramEntity(name = "Kelas Digital Ramah Anak", objective = "Pemanfaatan media teknologi edukatif secara bijak", description = "Pengenalan komputer dasar, video pembelajaran, dan asesmen berbasis digital.")
                )
                db.featuredProgramDao().insertAll(initialPrograms)

                // Initial Agendas
                val initialAgendas = listOf(
                    AgendaEntity(title = "Penilaian Tengah Semester (PTS) Genap", description = "Pelaksanaan evaluasi pembelajaran semester genap untuk seluruh tingkat kelas 1-6.", date = "23 Maret 2026", time = "07.30 - 11.30 WIB", location = "Ruang Kelas MI Muhammadiyah"),
                    AgendaEntity(title = "Pesantren Kilat Ramadhan 1447 H", description = "Kajian keislaman intensif, buka puasa bersama, dan santunan anak yatim dhuafa.", date = "10 April 2026", time = "15.00 - 19.30 WIB", location = "Masjid Baitul 'Ilmi Madrasah"),
                    AgendaEntity(title = "Wisuda Tahfidz & Pelepasan Santri Kelas 6", description = "Pemberian syahadah tahfidz juz 30 dan perayaan kelulusan santri angkatan 2026.", date = "15 Juni 2026", time = "08.00 - 12.00 WIB", location = "Gedung Serbaguna Way Bungur")
                )
                db.agendaDao().insertAll(initialAgendas)

                // Initial Testimonials
                val initialTestis = listOf(
                    TestimonialEntity(name = "H. Supriyadi (Wali Murid)", role = "Wali Santri Kelas 4", content = "Alhamdulillah anak saya sekarang terbiasa sholat tanpa disuruh dan hafalannya sudah sampai An-Naba'. Guru-gurunya sabar dan amanah.", rating = 5, isApproved = true, date = "12 Jan 2026"),
                    TestimonialEntity(name = "Ibu Siti Rohmah", role = "Wali Santri Kelas 2", content = "Suasana madrasah sangat kondusif, anak-anak diajarkan adab dan sopan santun. Pelayanan madrasah sangat transparan dan ramah.", rating = 5, isApproved = true, date = "14 Jan 2026"),
                    TestimonialEntity(name = "Bapak Wahyudi", role = "Masyarakat Way Bungur", content = "MI Muhammadiyah Tanjung Qencono pilihan tepat bagi para orang tua yang mendambakan pendidikan agama kuat sekaligus ilmu umum berkualitas.", rating = 5, isApproved = true, date = "16 Jan 2026")
                )
                db.testimonialDao().insertAll(initialTestis)

                // Initial Schedules
                val initialSchedules = listOf(
                    ClassScheduleEntity(classLevel = 1, rombel = "Ar Rahman", day = "Senin", timeSlot = "07.30 - 08.45", subject = "Al-Qur'an Hadist", teacherName = "Ust. Muhammad Ridwan, S.Ag."),
                    ClassScheduleEntity(classLevel = 1, rombel = "Ar Rahman", day = "Senin", timeSlot = "09.00 - 10.15", subject = "Tematik (Bahasa Indonesia)", teacherName = "Siti Nurhaliza, S.Pd."),
                    ClassScheduleEntity(classLevel = 1, rombel = "Ar Rahman", day = "Selasa", timeSlot = "07.30 - 08.45", subject = "Akidah Akhlak", teacherName = "Ust. Muhammad Ridwan, S.Ag."),
                    ClassScheduleEntity(classLevel = 2, rombel = "Al Malik", day = "Senin", timeSlot = "07.30 - 08.45", subject = "Bahasa Arab", teacherName = "Ust. Muhammad Ridwan, S.Ag."),
                    ClassScheduleEntity(classLevel = 3, rombel = "As Salaam", day = "Senin", timeSlot = "07.30 - 08.45", subject = "Matematika", teacherName = "Tri Wahyuni, S.Pd."),
                    ClassScheduleEntity(classLevel = 6, rombel = "Al Khaliq", day = "Senin", timeSlot = "07.30 - 09.00", subject = "IPA & Asesmen Madrasah", teacherName = "Bambang Setiawan, S.Pd.")
                )
                db.classScheduleDao().insertAll(initialSchedules)

                // Initial Facilities
                val initialFacilities = listOf(
                    FacilityEntity(
                        name = "Gedung Ruang Kelas Ber-AC & Nyaman",
                        category = "Ruang Belajar",
                        description = "6 ruang kelas representatif dilengkapi pendingin ruangan, pencahayaan alami optimal, dan sarana multimedia interaktif.",
                        condition = "Sangat Baik",
                        capacity = "32 Siswa / Kelas"
                    ),
                    FacilityEntity(
                        name = "Masjid Baitul 'Ilmi Madrasah",
                        category = "Sarana Ibadah",
                        description = "Masjid madrasah untuk pembiasaan sholat Dhuha dan Dzuhur berjamaah, hafalan Al-Qur'an, dan kajian adab islami harian.",
                        condition = "Sangat Baik",
                        capacity = "200 Jamaah"
                    ),
                    FacilityEntity(
                        name = "Laboratorium Komputer & Digital",
                        category = "Laboratorium & Komputer",
                        description = "Unit komputer modern berakses internet cepat untuk pembelajaran TIK santri dan pelaksanaan Asesmen Nasional Berbasis Komputer (ANBK).",
                        condition = "Sangat Baik",
                        capacity = "35 Komputer"
                    ),
                    FacilityEntity(
                        name = "Perpustakaan & Pojok Literasi Qur'ani",
                        category = "Ruang Belajar",
                        description = "Koleksi ribuan buku sains, ensiklopedia Islam, buku cerita bergambar akhlak, dan sudut baca nyaman santri.",
                        condition = "Sangat Baik",
                        capacity = "50 Pembaca"
                    ),
                    FacilityEntity(
                        name = "Lapangan Olahraga & Area Tapak Suci",
                        category = "Olahraga & Seni",
                        description = "Lapangan multifungsi untuk upacara bendera hari Senin, latihan senam santri, futsal, bulutangkis, dan pencak silat Tapak Suci.",
                        condition = "Sangat Baik",
                        capacity = "300 Siswa"
                    ),
                    FacilityEntity(
                        name = "Unit Kesehatan Sekolah (UKS) Ramah Anak",
                        category = "Penunjang",
                        description = "Ruang pertolongan pertama dilengkapi tempat tidur medis, kotak obat P3K lengkap, dan pengawasan berkala dari Puskesmas Way Bungur.",
                        condition = "Sangat Baik",
                        capacity = "4 Tempat Tidur"
                    ),
                    FacilityEntity(
                        name = "Kantin Sehat & Halal",
                        category = "Penunjang",
                        description = "Kantin madrasah yang menyajikan makanan dan minuman higienis, bergizi, bebas pengawet berbahaya, dan bersertifikasi halal.",
                        condition = "Sangat Baik",
                        capacity = "60 Siswa"
                    ),
                    FacilityEntity(
                        name = "Taman Belajar & Gazebo Literasi",
                        category = "Penunjang",
                        description = "Area hijau terbuka asri dengan pepohonan rindang dan gazebo untuk diskusi kelompok santri saat istirahat.",
                        condition = "Sangat Baik",
                        capacity = "4 Gazebo"
                    )
                )
                db.facilityDao().insertAll(initialFacilities)

                // Initial Notifications
                val initialNotifications = listOf(
                    AppNotificationEntity(
                        title = "Selamat Datang di Portal Resmi MIM Tanjung Qencono",
                        message = "Portal digital resmi madrasah kini aktif. Wali murid dan santri dapat memantau jadwal masuk, pengumuman, modul rpp, dan fasilitas madrasah secara real-time.",
                        category = "Pengumuman",
                        timestamp = System.currentTimeMillis() - 86400000L,
                        dateFormatted = "17 Okt 2026, 08:00 WIB",
                        isRead = false,
                        targetAudience = "Semua (Wali Murid & Siswa)",
                        soundAlert = true
                    ),
                    AppNotificationEntity(
                        title = "⏰ Jadwal Masuk & Apel Pagi Madrasah",
                        message = "Diberitahukan kepada seluruh siswa bahwa gerbang madrasah dibuka pukul 06.30 WIB dan apel pagi dimulai tepat pukul 07.00 WIB dilanjutkan Sholat Dhuha berjamaah.",
                        category = "Jadwal Masuk",
                        timestamp = System.currentTimeMillis() - 7200000L,
                        dateFormatted = "18 Okt 2026, 06:30 WIB",
                        isRead = false,
                        targetAudience = "Semua (Wali Murid & Siswa)",
                        soundAlert = true
                    )
                )
                db.appNotificationDao().insertAll(initialNotifications)

                // Initial Admin Log
                db.adminLogDao().insert(
                    AdminLogEntity(timestamp = "17 Okt 2026 09:00", action = "Sistem Diinisialisasi", details = "Database madrasah diinstal dengan pengaturan dan data awal.")
                )
            }
        }
    }
}
