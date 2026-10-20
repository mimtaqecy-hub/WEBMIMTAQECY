package com.example.data.repository

import com.example.data.local.*
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class OfflineCacheStatus(
    val studentCount: Int = 0,
    val teacherCount: Int = 0,
    val newsCount: Int = 0,
    val lastCachedTime: String = "Tersinkronisasi",
    val isOfflineReady: Boolean = true
)

class MadrasahRepository(private val db: AppDatabase) {

    // Settings
    val settings: Flow<SchoolSettingsEntity?> = db.schoolSettingsDao().getSettings()
    suspend fun getSettingsOnce(): SchoolSettingsEntity? = db.schoolSettingsDao().getSettingsOnce()
    suspend fun updateSettings(settings: SchoolSettingsEntity) {
        db.schoolSettingsDao().insertOrUpdate(settings)
        logAction("Ubah Pengaturan", "Memperbarui identitas dan pengaturan madrasah")
    }

    // Students (Offline Cached via Room)
    val allStudents: Flow<List<StudentEntity>> = db.studentDao().getAllStudents()
    val publishedStudents: Flow<List<StudentEntity>> = db.studentDao().getPublishedStudents()
    val studentCount: Flow<Int> = db.studentDao().getCount()

    fun getStudentById(id: Long): Flow<StudentEntity?> = db.studentDao().getStudentById(id)

    fun filterStudents(classLevel: Int, rombel: String, query: String): Flow<List<StudentEntity>> {
        return db.studentDao().filterStudents(classLevel, rombel, query)
    }

    suspend fun clearAndCacheStudents(students: List<StudentEntity>) {
        db.studentDao().clearAllStudents()
        db.studentDao().insertAll(students.map { it.copy(cachedAt = System.currentTimeMillis()) })
        logAction("Cache Siswa", "Memperbarui cache offline untuk ${students.size} data siswa")
    }

    suspend fun insertStudent(student: StudentEntity): Long {
        val id = db.studentDao().insert(student)
        logAction("Tambah Siswa", "Menambahkan siswa: ${student.fullName} (Kelas ${student.classLevel} ${student.rombel})")
        return id
    }

    suspend fun insertAllStudents(students: List<StudentEntity>) {
        db.studentDao().insertAll(students)
        logAction("Impor Siswa", "Mengimpor ${students.size} data siswa melalui CSV")
    }

    suspend fun updateStudent(student: StudentEntity) {
        db.studentDao().update(student)
        logAction("Ubah Siswa", "Memperbarui data siswa: ${student.fullName}")
    }

    suspend fun deleteStudent(student: StudentEntity) {
        db.studentDao().delete(student)
        logAction("Hapus Siswa", "Menghapus data siswa: ${student.fullName}")
    }

    suspend fun clearAllStudents() {
        db.studentDao().clearAllStudents()
        logAction("Hapus Semua Siswa", "Menghapus seluruh data siswa dari database")
    }

    // Teachers (Offline Cached via Room)
    val teachers: Flow<List<TeacherEntity>> = db.teacherDao().getAllTeachers()
    val teacherCount: Flow<Int> = db.teacherDao().getCount()

    fun getTeacherById(id: Long): Flow<TeacherEntity?> = db.teacherDao().getTeacherById(id)

    suspend fun clearAndCacheTeachers(teachers: List<TeacherEntity>) {
        db.teacherDao().clearAllTeachers()
        db.teacherDao().insertAll(teachers.map { it.copy(cachedAt = System.currentTimeMillis()) })
        logAction("Cache Guru", "Memperbarui cache offline untuk ${teachers.size} profil guru")
    }

    suspend fun insertTeacher(teacher: TeacherEntity): Long {
        val id = db.teacherDao().insert(teacher)
        logAction("Tambah Guru", "Menambahkan guru/staf: ${teacher.name}")
        return id
    }

    suspend fun updateTeacher(teacher: TeacherEntity) {
        db.teacherDao().update(teacher)
        logAction("Ubah Guru", "Memperbarui guru/staf: ${teacher.name}")
    }

    suspend fun deleteTeacher(teacher: TeacherEntity) {
        db.teacherDao().delete(teacher)
        logAction("Hapus Guru", "Menghapus guru/staf: ${teacher.name}")
    }

    // PPDB
    val allPpdb: Flow<List<PpdbEntity>> = db.ppdbDao().getAllPpdb()
    val ppdbCount: Flow<Int> = db.ppdbDao().getCount()

    suspend fun submitPpdb(ppdb: PpdbEntity): Long {
        val id = db.ppdbDao().insert(ppdb)
        logAction("Pendaftaran PPDB Baru", "Pendaftaran online: ${ppdb.studentName} (${ppdb.regNumber})")
        return id
    }

    suspend fun updatePpdbStatus(ppdb: PpdbEntity, newStatus: String, notes: String) {
        val updated = ppdb.copy(status = newStatus, adminNotes = notes)
        db.ppdbDao().update(updated)
        logAction("Verifikasi PPDB", "Status ${ppdb.regNumber} diubah menjadi '$newStatus'")
    }

    suspend fun deletePpdb(ppdb: PpdbEntity) {
        db.ppdbDao().delete(ppdb)
        logAction("Hapus PPDB", "Menghapus berkas PPDB: ${ppdb.regNumber}")
    }

    // RPP
    val allRpp: Flow<List<RppEntity>> = db.rppDao().getAllRpp()
    val rppCount: Flow<Int> = db.rppDao().getCount()

    fun filterRpp(classLevel: Int, rombel: String, query: String): Flow<List<RppEntity>> {
        return db.rppDao().filterRpp(classLevel, rombel, query)
    }

    suspend fun insertRpp(rpp: RppEntity): Long {
        val id = db.rppDao().insert(rpp)
        logAction("Unggah RPP", "Mengunggah perangkat ajar: ${rpp.title}")
        return id
    }

    suspend fun deleteRpp(rpp: RppEntity) {
        db.rppDao().delete(rpp)
        logAction("Hapus RPP", "Menghapus berkas RPP: ${rpp.title}")
    }

    // Documents
    val allDocuments: Flow<List<DocumentEntity>> = db.documentDao().getAllDocuments()

    suspend fun insertDocument(doc: DocumentEntity): Long {
        val id = db.documentDao().insert(doc)
        logAction("Unggah Dokumen", "Dokumen baru: ${doc.title} [${doc.folderName}]")
        return id
    }

    suspend fun deleteDocument(doc: DocumentEntity) {
        db.documentDao().delete(doc)
        logAction("Hapus Dokumen", "Menghapus dokumen: ${doc.title}")
    }

    // News (Offline Cached via Room)
    val allNews: Flow<List<NewsEntity>> = db.newsDao().getAllNews()
    val publishedNews: Flow<List<NewsEntity>> = db.newsDao().getPublishedNews()
    val newsCount: Flow<Int> = db.newsDao().getCount()

    fun getNewsById(id: Long): Flow<NewsEntity?> = db.newsDao().getNewsById(id)

    suspend fun clearAndCacheNews(newsList: List<NewsEntity>) {
        db.newsDao().clearAllNews()
        db.newsDao().insertAll(newsList.map { it.copy(cachedAt = System.currentTimeMillis()) })
        logAction("Cache Berita", "Memperbarui cache offline untuk ${newsList.size} artikel berita")
    }

    suspend fun insertNews(news: NewsEntity): Long {
        val id = db.newsDao().insert(news)
        logAction("Tambah Berita", "Menambahkan berita: ${news.title}")
        return id
    }

    suspend fun updateNews(news: NewsEntity) {
        db.newsDao().update(news)
        logAction("Ubah Berita", "Memperbarui berita: ${news.title}")
    }

    suspend fun deleteNews(news: NewsEntity) {
        db.newsDao().delete(news)
        logAction("Hapus Berita", "Menghapus berita: ${news.title}")
    }

    // Gallery
    val allGallery: Flow<List<GalleryEntity>> = db.galleryDao().getAllGallery()
    val publishedGallery: Flow<List<GalleryEntity>> = db.galleryDao().getPublishedGallery()
    val galleryCount: Flow<Int> = db.galleryDao().getCount()

    suspend fun insertGallery(item: GalleryEntity): Long {
        val id = db.galleryDao().insert(item)
        logAction("Tambah Galeri", "Foto baru: ${item.title} [${item.category}]")
        return id
    }

    suspend fun deleteGallery(item: GalleryEntity) {
        db.galleryDao().delete(item)
        logAction("Hapus Galeri", "Menghapus foto: ${item.title}")
    }

    // Extracurriculars
    val allExtracurriculars: Flow<List<ExtracurricularEntity>> = db.extracurricularDao().getAll()

    suspend fun insertExtracurricular(extra: ExtracurricularEntity): Long {
        val id = db.extracurricularDao().insert(extra)
        logAction("Tambah Ekskul", "Ekskul baru: ${extra.name}")
        return id
    }

    suspend fun deleteExtracurricular(extra: ExtracurricularEntity) {
        db.extracurricularDao().delete(extra)
        logAction("Hapus Ekskul", "Menghapus ekskul: ${extra.name}")
    }

    // Featured Programs
    val allPrograms: Flow<List<FeaturedProgramEntity>> = db.featuredProgramDao().getAll()

    suspend fun insertProgram(prog: FeaturedProgramEntity): Long {
        val id = db.featuredProgramDao().insert(prog)
        logAction("Tambah Program", "Program unggulan: ${prog.name}")
        return id
    }

    suspend fun deleteProgram(prog: FeaturedProgramEntity) {
        db.featuredProgramDao().delete(prog)
        logAction("Hapus Program", "Menghapus program: ${prog.name}")
    }

    // Agendas
    val allAgendas: Flow<List<AgendaEntity>> = db.agendaDao().getAll()
    val publishedAgendas: Flow<List<AgendaEntity>> = db.agendaDao().getPublished()

    suspend fun insertAgenda(agenda: AgendaEntity): Long {
        val id = db.agendaDao().insert(agenda)
        logAction("Tambah Agenda", "Agenda baru: ${agenda.title} (${agenda.date})")
        return id
    }

    suspend fun deleteAgenda(agenda: AgendaEntity) {
        db.agendaDao().delete(agenda)
        logAction("Hapus Agenda", "Menghapus agenda: ${agenda.title}")
    }

    // Testimonials
    val allTestimonials: Flow<List<TestimonialEntity>> = db.testimonialDao().getAll()
    val approvedTestimonials: Flow<List<TestimonialEntity>> = db.testimonialDao().getApproved()
    val pendingTestimonialCount: Flow<Int> = db.testimonialDao().getPendingCount()

    suspend fun submitTestimonial(testi: TestimonialEntity): Long {
        return db.testimonialDao().insert(testi)
    }

    suspend fun approveTestimonial(testi: TestimonialEntity, isApproved: Boolean) {
        db.testimonialDao().update(testi.copy(isApproved = isApproved))
        val statusStr = if (isApproved) "disetujui" else "ditunda"
        logAction("Moderasi Testimoni", "Testimoni dari ${testi.name} $statusStr")
    }

    suspend fun deleteTestimonial(testi: TestimonialEntity) {
        db.testimonialDao().delete(testi)
        logAction("Hapus Testimoni", "Menghapus testimoni dari ${testi.name}")
    }

    // Contact Messages
    val allContactMessages: Flow<List<ContactMessageEntity>> = db.contactMessageDao().getAll()
    val unreadMessageCount: Flow<Int> = db.contactMessageDao().getUnreadCount()

    suspend fun sendContactMessage(msg: ContactMessageEntity): Long {
        return db.contactMessageDao().insert(msg)
    }

    suspend fun markMessageRead(msg: ContactMessageEntity) {
        db.contactMessageDao().update(msg.copy(isRead = true))
    }

    suspend fun deleteContactMessage(msg: ContactMessageEntity) {
        db.contactMessageDao().delete(msg)
        logAction("Hapus Pesan", "Menghapus pesan dari ${msg.senderName}")
    }

    // Admin Logs
    val recentLogs: Flow<List<AdminLogEntity>> = db.adminLogDao().getRecentLogs()

    suspend fun logAction(action: String, details: String) {
        val now = SimpleDateFormat("dd MMM yyyy HH:mm", Locale("id", "ID")).format(Date())
        db.adminLogDao().insert(AdminLogEntity(timestamp = now, action = action, details = details))
    }

    // Schedules
    fun getSchedules(classLevel: Int, rombel: String): Flow<List<ClassScheduleEntity>> {
        return db.classScheduleDao().getSchedules(classLevel, rombel)
    }

    suspend fun insertSchedule(schedule: ClassScheduleEntity): Long {
        val id = db.classScheduleDao().insert(schedule)
        logAction("Tambah Jadwal", "Jadwal ${schedule.subject} (${schedule.day})")
        return id
    }

    suspend fun deleteSchedule(schedule: ClassScheduleEntity) {
        db.classScheduleDao().delete(schedule)
        logAction("Hapus Jadwal", "Hapus jadwal ${schedule.subject}")
    }

    // Facilities
    val allFacilities: Flow<List<FacilityEntity>> = db.facilityDao().getAllFacilities()
    val publishedFacilities: Flow<List<FacilityEntity>> = db.facilityDao().getPublishedFacilities()

    suspend fun insertFacility(facility: FacilityEntity): Long {
        val id = db.facilityDao().insert(facility)
        logAction("Tambah Fasilitas", "Menambahkan fasilitas: ${facility.name}")
        return id
    }

    suspend fun updateFacility(facility: FacilityEntity) {
        db.facilityDao().update(facility)
        logAction("Ubah Fasilitas", "Memperbarui fasilitas: ${facility.name}")
    }

    suspend fun deleteFacility(facility: FacilityEntity) {
        db.facilityDao().delete(facility)
        logAction("Hapus Fasilitas", "Menghapus fasilitas: ${facility.name}")
    }

    // Notifications
    val allNotifications: Flow<List<AppNotificationEntity>> = db.appNotificationDao().getAllNotifications()
    val unreadNotificationCount: Flow<Int> = db.appNotificationDao().getUnreadCount()

    suspend fun insertNotification(notification: AppNotificationEntity): Long {
        val id = db.appNotificationDao().insert(notification)
        logAction("Kirim Notifikasi", "Mengirim notifikasi: [${notification.category}] ${notification.title}")
        return id
    }

    suspend fun markNotificationAsRead(id: Long) {
        db.appNotificationDao().markAsRead(id)
    }

    suspend fun markAllNotificationsAsRead() {
        db.appNotificationDao().markAllAsRead()
    }

    suspend fun deleteNotification(id: Long) {
        db.appNotificationDao().deleteById(id)
        logAction("Hapus Notifikasi", "Menghapus notifikasi ID: $id")
    }
}
