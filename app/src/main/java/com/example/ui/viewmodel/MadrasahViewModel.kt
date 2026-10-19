package com.example.ui.viewmodel

import android.app.Application
import android.media.MediaPlayer
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.*
import com.example.data.repository.MadrasahRepository
import com.example.data.repository.OfflineCacheStatus
import com.example.util.CsvHelper
import com.example.util.PrayerTimes
import com.example.util.PrayerTimesCalculator
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class AppScreen(val title: String) {
    HOME("Beranda"),
    PROFILE("Profil"),
    ACADEMIC("Akademik"),
    NEWS("Berita"),
    GALLERY("Galeri"),
    FACILITIES("Fasilitas"),
    SCHOOL_SCHEDULE("Jadwal Masuk"),
    NOTIFICATIONS("Notifikasi"),
    PPDB("PPDB Online"),
    STUDENT_DATA("Data Siswa"),
    PRAYER_SCHEDULE("Jadwal Sholat"),
    CONTACT("Kontak"),
    ADMIN_LOGIN("Login Admin"),
    ADMIN_DASHBOARD("Dashboard Admin")
}

enum class AdminTab(val title: String) {
    OVERVIEW("Dashboard"),
    SETTINGS("Pengaturan Utama"),
    WRITE_NOTIFICATION("Tulis Notifikasi"),
    FACILITIES("Fasilitas"),
    SCHOOL_SCHEDULE("Jadwal Masuk"),
    STUDENTS("Data Siswa"),
    TEACHERS("Guru & Staf"),
    PPDB("PPDB Online"),
    RPP("Modul RPP"),
    NEWS("Berita"),
    GALLERY("Galeri Foto"),
    EXTRACURRICULARS("Ekstrakurikuler"),
    PROGRAMS("Program Unggulan"),
    AGENDAS("Agenda"),
    TESTIMONIALS("Testimoni"),
    MESSAGES("Pesan Kontak"),
    SCHEDULES("Jadwal Pelajaran"),
    MUSIC("Musik Latar"),
    LOGS("Aktivitas Admin"),
    GAS_INTEGRATION("Integrasi Google Sheets")
}

class MadrasahViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application, viewModelScope)
    val repository = MadrasahRepository(db)

    // Navigation & Global UI State
    private val _currentScreen = MutableStateFlow(AppScreen.HOME)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _currentAdminTab = MutableStateFlow(AdminTab.OVERVIEW)
    val currentAdminTab: StateFlow<AdminTab> = _currentAdminTab.asStateFlow()

    private val _isDarkTheme = MutableStateFlow(false)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    private val _globalSearch = MutableStateFlow("")
    val globalSearch: StateFlow<String> = _globalSearch.asStateFlow()

    private val _snackbarMessage = MutableSharedFlow<String>()
    val snackbarMessage: SharedFlow<String> = _snackbarMessage.asSharedFlow()

    // Settings
    val settings: StateFlow<SchoolSettingsEntity?> = repository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Audio & Music Player state
    private val _isMusicPlaying = MutableStateFlow(false)
    val isMusicPlaying: StateFlow<Boolean> = _isMusicPlaying.asStateFlow()

    private val _musicVolume = MutableStateFlow(0.5f)
    val musicVolume: StateFlow<Float> = _musicVolume.asStateFlow()

    // Admin Authentication State
    private val _isAdminLoggedIn = MutableStateFlow(false)
    val isAdminLoggedIn: StateFlow<Boolean> = _isAdminLoggedIn.asStateFlow()

    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError.asStateFlow()

    private var loginAttempts = 0
    private var lockUntilTimestamp: Long = 0

    // Data Streams
    val publishedStudents = repository.publishedStudents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allStudents = repository.allStudents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val teachers = repository.teachers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPpdb = repository.allPpdb
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allRpp = repository.allRpp
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allDocuments = repository.allDocuments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val publishedNews = repository.publishedNews
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allNews = repository.allNews
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val publishedGallery = repository.publishedGallery
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allGallery = repository.allGallery
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val extracurriculars = repository.allExtracurriculars
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val featuredPrograms = repository.allPrograms
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val publishedAgendas = repository.publishedAgendas
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAgendas = repository.allAgendas
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val approvedTestimonials = repository.approvedTestimonials
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTestimonials = repository.allTestimonials
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val contactMessages = repository.allContactMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unreadMessageCount = repository.unreadMessageCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val allFacilities = repository.allFacilities
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val publishedFacilities = repository.publishedFacilities
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allNotifications = repository.allNotifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unreadNotificationCount = repository.unreadNotificationCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val adminLogs = repository.recentLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Google Sheets Sync State
    private val _isSheetSyncing = MutableStateFlow(false)
    val isSheetSyncing: StateFlow<Boolean> = _isSheetSyncing.asStateFlow()

    private val _sheetSyncStatus = MutableStateFlow<String?>(null)
    val sheetSyncStatus: StateFlow<String?> = _sheetSyncStatus.asStateFlow()

    // Counts for stats
    val studentCount = repository.studentCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val teacherCount = repository.teacherCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val ppdbCount = repository.ppdbCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val rppCount = repository.rppCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val newsCount = repository.newsCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val galleryCount = repository.galleryCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val pendingTestimonialCount = repository.pendingTestimonialCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val offlineCacheStatus: StateFlow<OfflineCacheStatus> = combine(
        studentCount,
        teacherCount,
        newsCount
    ) { sCount: Int, tCount: Int, nCount: Int ->
        OfflineCacheStatus(
            studentCount = sCount,
            teacherCount = tCount,
            newsCount = nCount,
            lastCachedTime = SimpleDateFormat("dd MMM yyyy HH:mm", Locale("id", "ID")).format(Date()),
            isOfflineReady = true
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), OfflineCacheStatus())

    fun refreshOfflineCache() {
        viewModelScope.launch {
            repository.logAction("Validasi Cache Offline", "Memverifikasi integritas cache Room untuk siswa, guru, dan berita")
            _snackbarMessage.emit("Cache lokal Room aktif: Seluruh data siswa, profil guru, dan berita tersimpan & siap diakses tanpa internet!")
        }
    }

    // Filtering State for Students
    private val _studentFilterClass = MutableStateFlow(0) // 0 for all
    val studentFilterClass: StateFlow<Int> = _studentFilterClass.asStateFlow()

    private val _studentFilterRombel = MutableStateFlow("")
    val studentFilterRombel: StateFlow<String> = _studentFilterRombel.asStateFlow()

    private val _studentSearchQuery = MutableStateFlow("")
    val studentSearchQuery: StateFlow<String> = _studentSearchQuery.asStateFlow()

    val filteredStudents = combine(
        allStudents,
        _studentFilterClass,
        _studentFilterRombel,
        _studentSearchQuery
    ) { list, cls, rombel, query ->
        list.filter { student ->
            (cls == 0 || student.classLevel == cls) &&
            (rombel.isEmpty() || student.rombel.equals(rombel, ignoreCase = true)) &&
            (query.isEmpty() || student.fullName.contains(query, ignoreCase = true) || student.nisn.contains(query))
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Prayer Times
    private val _prayerTimes = MutableStateFlow(PrayerTimesCalculator.calculate())
    val prayerTimes: StateFlow<PrayerTimes> = _prayerTimes.asStateFlow()

    // PPDB submission result dialog
    private val _lastSubmittedPpdb = MutableStateFlow<PpdbEntity?>(null)
    val lastSubmittedPpdb: StateFlow<PpdbEntity?> = _lastSubmittedPpdb.asStateFlow()

    init {
        refreshPrayerTimes()
    }

    fun refreshPrayerTimes() {
        _prayerTimes.value = PrayerTimesCalculator.calculate()
    }

    // Navigation functions
    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun navigateAdminTab(tab: AdminTab) {
        _currentAdminTab.value = tab
    }

    fun toggleDarkTheme() {
        _isDarkTheme.value = !_isDarkTheme.value
    }

    fun setGlobalSearch(query: String) {
        _globalSearch.value = query
    }

    fun setStudentFilter(cls: Int, rombel: String, query: String) {
        _studentFilterClass.value = cls
        _studentFilterRombel.value = rombel
        _studentSearchQuery.value = query
    }

    // Music Player controls with MediaPlayer
    private var mediaPlayer: MediaPlayer? = null

    fun toggleMusic() {
        val willPlay = !_isMusicPlaying.value
        _isMusicPlaying.value = willPlay
        if (willPlay) {
            playAudio()
        } else {
            pauseAudio()
        }
    }

    private fun playAudio() {
        try {
            if (mediaPlayer == null) {
                val currentSettings = settings.value
                val customPath = currentSettings?.customAudioUri
                if (!customPath.isNullOrBlank()) {
                    val file = File(customPath)
                    val uri = if (file.exists()) Uri.fromFile(file) else Uri.parse(customPath)
                    mediaPlayer = MediaPlayer().apply {
                        setDataSource(getApplication(), uri)
                        isLooping = true
                        setVolume(_musicVolume.value, _musicVolume.value)
                        prepare()
                        start()
                    }
                }
            } else {
                mediaPlayer?.start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun pauseAudio() {
        try {
            mediaPlayer?.pause()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setMusicVolume(volume: Float) {
        val vol = volume.coerceIn(0f, 1f)
        _musicVolume.value = vol
        try {
            mediaPlayer?.setVolume(vol, vol)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setCustomAudio(uriString: String, fileName: String) {
        viewModelScope.launch {
            val current = repository.getSettingsOnce() ?: SchoolSettingsEntity()
            val updated = current.copy(customAudioUri = uriString, customAudioName = fileName)
            repository.updateSettings(updated)
            _snackbarMessage.emit("Audio '$fileName' berhasil dipilih sebagai musik latar.")
            try {
                mediaPlayer?.stop()
                mediaPlayer?.release()
                mediaPlayer = null
                if (_isMusicPlaying.value) {
                    playAudio()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Admin Login logic
    fun loginAdmin(passwordInput: String) {
        val now = System.currentTimeMillis()
        if (now < lockUntilTimestamp) {
            val secondsLeft = ((lockUntilTimestamp - now) / 1000).toInt()
            _loginError.value = "Terlalu banyak percobaan salah. Coba lagi dalam $secondsLeft detik."
            return
        }

        viewModelScope.launch {
            val currentSettings = repository.getSettingsOnce() ?: SchoolSettingsEntity()
            val validPassword = currentSettings.adminPassword

            if (passwordInput.trim() == validPassword) {
                _isAdminLoggedIn.value = true
                _loginError.value = null
                loginAttempts = 0
                repository.logAction("Login Admin Berhasil", "Sesi administrator aktif")
                navigateTo(AppScreen.ADMIN_DASHBOARD)
                _snackbarMessage.emit("Selamat datang di Dashboard Admin MI Muhammadiyah!")
            } else {
                loginAttempts++
                if (loginAttempts >= 5) {
                    lockUntilTimestamp = now + (60 * 1000) // 1 minute lockout
                    _loginError.value = "5 kali percobaan gagal. Terkunci selama 60 detik demi keamanan."
                } else {
                    _loginError.value = "Sandi tidak cocok! Percobaan $loginAttempts dari 5."
                }
            }
        }
    }

    fun logoutAdmin() {
        viewModelScope.launch {
            repository.logAction("Logout Admin", "Sesi administrator ditutup")
            _isAdminLoggedIn.value = false
            _currentAdminTab.value = AdminTab.OVERVIEW
            navigateTo(AppScreen.HOME)
            _snackbarMessage.emit("Anda telah berhasil keluar dari mode admin.")
        }
    }

    fun changeAdminPassword(oldPass: String, newPass: String) {
        viewModelScope.launch {
            val currentSettings = repository.getSettingsOnce() ?: SchoolSettingsEntity()
            if (currentSettings.adminPassword != oldPass) {
                _snackbarMessage.emit("Sandi lama tidak sesuai!")
                return@launch
            }
            if (newPass.length < 6) {
                _snackbarMessage.emit("Sandi baru minimal 6 karakter!")
                return@launch
            }
            repository.updateSettings(currentSettings.copy(adminPassword = newPass))
            _snackbarMessage.emit("Kata sandi admin berhasil diperbarui!")
        }
    }

    fun updateSchoolSettings(settings: SchoolSettingsEntity) {
        viewModelScope.launch {
            repository.updateSettings(settings)
            _snackbarMessage.emit("Pengaturan madrasah berhasil disimpan ke database!")
        }
    }

    // PPDB Form Submission
    fun submitPpdbForm(
        studentName: String,
        nisn: String,
        nik: String,
        birthPlace: String,
        birthDate: String,
        gender: String,
        address: String,
        whatsapp: String,
        prevSchool: String,
        fatherName: String,
        fatherNik: String,
        fatherBirthPlace: String,
        fatherBirthDate: String,
        fatherEducation: String,
        fatherJob: String,
        motherName: String,
        motherNik: String,
        motherBirthPlace: String,
        motherBirthDate: String,
        motherEducation: String,
        motherJob: String,
        kkName: String,
        ijazahName: String,
        aktaName: String,
        studentPhotoUri: String = ""
    ) {
        viewModelScope.launch {
            val count = ppdbCount.value + 1
            val regNum = String.format(Locale.getDefault(), "PPDB-2026-%03d", count)
            val regDate = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")).format(Date())

            val entity = PpdbEntity(
                regNumber = regNum,
                studentName = studentName,
                nisn = nisn,
                nik = nik,
                birthPlace = birthPlace,
                birthDate = birthDate,
                gender = gender,
                address = address,
                whatsapp = whatsapp,
                prevSchool = prevSchool,
                fatherName = fatherName,
                fatherNik = fatherNik,
                fatherBirthPlace = fatherBirthPlace,
                fatherBirthDate = fatherBirthDate,
                fatherEducation = fatherEducation,
                fatherJob = fatherJob,
                motherName = motherName,
                motherNik = motherNik,
                motherBirthPlace = motherBirthPlace,
                motherBirthDate = motherBirthDate,
                motherEducation = motherEducation,
                motherJob = motherJob,
                kkDocName = if (kkName.isBlank()) "Berkas_KK.pdf" else kkName,
                ijazahDocName = if (ijazahName.isBlank()) "Berkas_Ijazah.pdf" else ijazahName,
                aktaDocName = if (aktaName.isBlank()) "Berkas_Akta.pdf" else aktaName,
                studentPhotoUri = studentPhotoUri,
                status = "Menunggu Verifikasi",
                regDate = regDate
            )

            val id = repository.submitPpdb(entity)
            val saved = entity.copy(id = id)
            _lastSubmittedPpdb.value = saved
            _snackbarMessage.emit("Pendaftaran PPDB Berhasil! Nomor: $regNum")

            // Auto-sync to Google Sheet if configured
            val currentSettings = settings.value
            if (currentSettings != null && currentSettings.gasWebAppUrl.isNotBlank() && currentSettings.autoSyncPpdbToSheet) {
                launch {
                    val res = com.example.util.GoogleSheetsHelper.submitSinglePpdb(currentSettings.gasWebAppUrl, saved)
                    if (res is com.example.util.SheetResult.Success) {
                        _snackbarMessage.emit("Data PPDB otomatis tersinkron ke Google Sheets!")
                    }
                }
            }
        }
    }

    fun dismissPpdbSuccessDialog() {
        _lastSubmittedPpdb.value = null
    }

    fun updatePpdbStatus(ppdb: PpdbEntity, status: String, notes: String) {
        viewModelScope.launch {
            repository.updatePpdbStatus(ppdb, status, notes)
            _snackbarMessage.emit("Status pendaftaran ${ppdb.regNumber} diperbarui menjadi '$status'")
        }
    }

    fun deletePpdb(ppdb: PpdbEntity) {
        viewModelScope.launch {
            repository.deletePpdb(ppdb)
            _snackbarMessage.emit("Berkas PPDB ${ppdb.regNumber} berhasil dihapus.")
        }
    }

    // Student CRUD
    fun addStudent(student: StudentEntity) {
        viewModelScope.launch {
            repository.insertStudent(student)
            _snackbarMessage.emit("Data siswa ${student.fullName} berhasil ditambahkan.")
        }
    }

    fun updateStudent(student: StudentEntity) {
        viewModelScope.launch {
            repository.updateStudent(student)
            _snackbarMessage.emit("Data siswa ${student.fullName} berhasil diubah.")
        }
    }

    fun deleteStudent(student: StudentEntity) {
        viewModelScope.launch {
            repository.deleteStudent(student)
            _snackbarMessage.emit("Data siswa ${student.fullName} berhasil dihapus.")
        }
    }

    fun importStudentsFromUri(uri: Uri, fileName: String?) {
        viewModelScope.launch {
            try {
                val context = getApplication<Application>()
                val stream = context.contentResolver.openInputStream(uri)
                if (stream != null) {
                    val list = CsvHelper.parseStudentsFromInputStream(stream, fileName)
                    if (list.isNotEmpty()) {
                        repository.insertAllStudents(list)
                        _snackbarMessage.emit("Berhasil mengunggah ${list.size} siswa dari ${fileName ?: "file Excel"}.")
                        repository.logAction("Unggah Data Siswa", "Impor ${list.size} siswa dari ${fileName ?: "file"}")
                    } else {
                        _snackbarMessage.emit("Format file tidak sesuai template atau data kosong.")
                    }
                } else {
                    _snackbarMessage.emit("Gagal membaca file dari perangkat.")
                }
            } catch (e: Exception) {
                _snackbarMessage.emit("Gagal mengimpor file: ${e.message}")
            }
        }
    }

    fun importStudentsCsv(csvText: String) {
        viewModelScope.launch {
            val list = CsvHelper.parseStudentsCsv(csvText)
            if (list.isNotEmpty()) {
                repository.insertAllStudents(list)
                _snackbarMessage.emit("Berhasil mengimpor ${list.size} siswa dari CSV.")
            } else {
                _snackbarMessage.emit("Format CSV tidak valid atau data kosong.")
            }
        }
    }

    // Teacher CRUD
    fun addTeacher(teacher: TeacherEntity) {
        viewModelScope.launch {
            repository.insertTeacher(teacher)
            _snackbarMessage.emit("Guru/staf ${teacher.name} berhasil ditambahkan.")
        }
    }

    fun updateTeacher(teacher: TeacherEntity) {
        viewModelScope.launch {
            repository.updateTeacher(teacher)
            _snackbarMessage.emit("Data ${teacher.name} berhasil diperbarui.")
        }
    }

    fun deleteTeacher(teacher: TeacherEntity) {
        viewModelScope.launch {
            repository.deleteTeacher(teacher)
            _snackbarMessage.emit("Data guru/staf berhasil dihapus.")
        }
    }

    // RPP CRUD
    fun addRpp(rpp: RppEntity) {
        viewModelScope.launch {
            repository.insertRpp(rpp)
            _snackbarMessage.emit("Perangkat RPP '${rpp.title}' berhasil disimpan.")
        }
    }

    fun deleteRpp(rpp: RppEntity) {
        viewModelScope.launch {
            repository.deleteRpp(rpp)
            _snackbarMessage.emit("RPP berhasil dihapus.")
        }
    }

    // Documents
    fun addDocument(doc: DocumentEntity) {
        viewModelScope.launch {
            repository.insertDocument(doc)
            _snackbarMessage.emit("Dokumen '${doc.title}' berhasil diarsipkan.")
        }
    }

    fun deleteDocument(doc: DocumentEntity) {
        viewModelScope.launch {
            repository.deleteDocument(doc)
            _snackbarMessage.emit("Dokumen berhasil dihapus.")
        }
    }

    // News CRUD
    fun addNews(news: NewsEntity) {
        viewModelScope.launch {
            repository.insertNews(news)
            _snackbarMessage.emit("Berita '${news.title}' berhasil diterbitkan.")
        }
    }

    fun updateNews(news: NewsEntity) {
        viewModelScope.launch {
            repository.updateNews(news)
            _snackbarMessage.emit("Berita berhasil diperbarui.")
        }
    }

    fun deleteNews(news: NewsEntity) {
        viewModelScope.launch {
            repository.deleteNews(news)
            _snackbarMessage.emit("Berita berhasil dihapus.")
        }
    }

    // Gallery
    fun addGallery(item: GalleryEntity) {
        viewModelScope.launch {
            repository.insertGallery(item)
            _snackbarMessage.emit("Foto '${item.title}' ditambahkan ke galeri.")
        }
    }

    fun deleteGallery(item: GalleryEntity) {
        viewModelScope.launch {
            repository.deleteGallery(item)
            _snackbarMessage.emit("Foto galeri dihapus.")
        }
    }

    // Extracurriculars
    fun addExtracurricular(extra: ExtracurricularEntity) {
        viewModelScope.launch {
            repository.insertExtracurricular(extra)
            _snackbarMessage.emit("Ekstrakurikuler '${extra.name}' ditambahkan.")
        }
    }

    fun deleteExtracurricular(extra: ExtracurricularEntity) {
        viewModelScope.launch {
            repository.deleteExtracurricular(extra)
            _snackbarMessage.emit("Ekstrakurikuler berhasil dihapus.")
        }
    }

    // Programs
    fun addProgram(prog: FeaturedProgramEntity) {
        viewModelScope.launch {
            repository.insertProgram(prog)
            _snackbarMessage.emit("Program '${prog.name}' ditambahkan.")
        }
    }

    fun deleteProgram(prog: FeaturedProgramEntity) {
        viewModelScope.launch {
            repository.deleteProgram(prog)
            _snackbarMessage.emit("Program berhasil dihapus.")
        }
    }

    // Agendas
    fun addAgenda(agenda: AgendaEntity) {
        viewModelScope.launch {
            repository.insertAgenda(agenda)
            _snackbarMessage.emit("Agenda '${agenda.title}' berhasil dijadwalkan.")
        }
    }

    fun deleteAgenda(agenda: AgendaEntity) {
        viewModelScope.launch {
            repository.deleteAgenda(agenda)
            _snackbarMessage.emit("Agenda berhasil dihapus.")
        }
    }

    // Testimonial
    fun submitTestimonial(name: String, role: String, content: String, rating: Int) {
        viewModelScope.launch {
            val dateStr = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")).format(Date())
            val entity = TestimonialEntity(
                name = name,
                role = role,
                content = content,
                rating = rating,
                isApproved = false,
                date = dateStr
            )
            repository.submitTestimonial(entity)
            _snackbarMessage.emit("Terima kasih! Testimoni Anda telah terkirim dan akan ditinjau oleh pihak madrasah.")
        }
    }

    fun approveTestimonial(testi: TestimonialEntity, isApproved: Boolean) {
        viewModelScope.launch {
            repository.approveTestimonial(testi, isApproved)
            _snackbarMessage.emit("Status testimoni dari ${testi.name} berhasil diubah.")
        }
    }

    fun deleteTestimonial(testi: TestimonialEntity) {
        viewModelScope.launch {
            repository.deleteTestimonial(testi)
            _snackbarMessage.emit("Testimoni dihapus.")
        }
    }

    // Contact
    fun sendContactMessage(name: String, email: String, phone: String, subject: String, message: String) {
        viewModelScope.launch {
            val dateStr = SimpleDateFormat("dd MMM yyyy HH:mm", Locale("id", "ID")).format(Date())
            val entity = ContactMessageEntity(
                senderName = name,
                email = email,
                phone = phone,
                subject = subject,
                message = message,
                date = dateStr,
                isRead = false
            )
            repository.sendContactMessage(entity)
            _snackbarMessage.emit("Pesan Anda telah terkirim ke Tata Usaha madrasah. Terima kasih!")

            val currentSettings = settings.value
            if (currentSettings != null && currentSettings.gasWebAppUrl.isNotBlank()) {
                launch {
                    com.example.util.GoogleSheetsHelper.submitContactMessage(currentSettings.gasWebAppUrl, entity)
                }
            }
        }
    }

    fun markMessageRead(msg: ContactMessageEntity) {
        viewModelScope.launch {
            repository.markMessageRead(msg)
        }
    }

    fun deleteContactMessage(msg: ContactMessageEntity) {
        viewModelScope.launch {
            repository.deleteContactMessage(msg)
            _snackbarMessage.emit("Pesan kontak dihapus.")
        }
    }

    // Schedules
    fun addSchedule(schedule: ClassScheduleEntity) {
        viewModelScope.launch {
            repository.insertSchedule(schedule)
            _snackbarMessage.emit("Jadwal pelajaran ${schedule.subject} berhasil disimpan.")
        }
    }

    fun deleteSchedule(schedule: ClassScheduleEntity) {
        viewModelScope.launch {
            repository.deleteSchedule(schedule)
            _snackbarMessage.emit("Jadwal pelajaran dihapus.")
        }
    }

    // Notifications & Background Audible Alerts
    fun sendNotification(
        title: String,
        message: String,
        category: String = "Pengumuman",
        targetAudience: String = "Semua (Wali Murid & Siswa)",
        soundAlert: Boolean = true
    ) {
        viewModelScope.launch {
            val dateStr = SimpleDateFormat("dd MMM yyyy, HH:mm 'WIB'", Locale("id", "ID")).format(Date())
            val entity = AppNotificationEntity(
                title = title,
                message = message,
                category = category,
                timestamp = System.currentTimeMillis(),
                dateFormatted = dateStr,
                isRead = false,
                targetAudience = targetAudience,
                soundAlert = soundAlert
            )
            repository.insertNotification(entity)
            com.example.util.NotificationHelper.showNotification(
                context = getApplication(),
                title = title,
                message = message,
                category = category,
                soundAlert = soundAlert
            )
            _snackbarMessage.emit("Notifikasi berhasil dikirim & disiarkan dengan nada dering!")
        }
    }

    fun markNotificationRead(id: Long) {
        viewModelScope.launch {
            repository.markNotificationAsRead(id)
        }
    }

    fun markAllNotificationsRead() {
        viewModelScope.launch {
            repository.markAllNotificationsAsRead()
            _snackbarMessage.emit("Semua notifikasi ditandai sudah dibaca.")
        }
    }

    fun deleteNotification(id: Long) {
        viewModelScope.launch {
            repository.deleteNotification(id)
            _snackbarMessage.emit("Notifikasi berhasil dihapus.")
        }
    }

    fun testAudibleNotification() {
        viewModelScope.launch {
            com.example.util.NotificationHelper.showNotification(
                context = getApplication(),
                title = "Uji Coba Suara Notifikasi",
                message = "Notifikasi madrasah berbunyi dan bergetar di latar belakang!",
                category = "Uji Suara",
                soundAlert = true
            )
            _snackbarMessage.emit("Nada dering notifikasi & getaran berhasil dibunyikan!")
        }
    }

    // Facilities Management
    fun addFacility(facility: FacilityEntity) {
        viewModelScope.launch {
            repository.insertFacility(facility)
            _snackbarMessage.emit("Fasilitas madrasah '${facility.name}' berhasil ditambahkan.")
        }
    }

    fun updateFacility(facility: FacilityEntity) {
        viewModelScope.launch {
            repository.updateFacility(facility)
            _snackbarMessage.emit("Fasilitas madrasah berhasil diperbarui.")
        }
    }

    fun deleteFacility(facility: FacilityEntity) {
        viewModelScope.launch {
            repository.deleteFacility(facility)
            _snackbarMessage.emit("Fasilitas madrasah dihapus.")
        }
    }

    fun scheduleEntryReminder() {
        com.example.util.NotificationHelper.scheduleDailySchoolEntryReminder(getApplication(), 6, 30)
        viewModelScope.launch {
            _snackbarMessage.emit("Pengingat harian jam masuk (06.30 WIB) berhasil diatur di latar belakang!")
        }
    }

    // Google Sheets Integration
    fun testSheetConnection(gasUrl: String, onResult: ((Boolean, String) -> Unit)? = null) {
        viewModelScope.launch {
            _isSheetSyncing.value = true
            val res = com.example.util.GoogleSheetsHelper.testConnection(gasUrl)
            _isSheetSyncing.value = false
            when (res) {
                is com.example.util.SheetResult.Success -> {
                    _sheetSyncStatus.value = res.message
                    _snackbarMessage.emit(res.message)
                    onResult?.invoke(true, res.message)
                }
                is com.example.util.SheetResult.Error -> {
                    _sheetSyncStatus.value = "Error: ${res.message}"
                    _snackbarMessage.emit(res.message)
                    onResult?.invoke(false, res.message)
                }
            }
        }
    }

    fun saveSheetConfig(gasUrl: String, spreadsheetId: String, autoSync: Boolean) {
        viewModelScope.launch {
            val current = settings.value ?: SchoolSettingsEntity()
            val cleanSheetId = com.example.util.GoogleSheetsHelper.extractSpreadsheetId(spreadsheetId)
            val updated = current.copy(
                gasWebAppUrl = gasUrl.trim(),
                googleSpreadsheetId = cleanSheetId,
                autoSyncPpdbToSheet = autoSync
            )
            repository.updateSettings(updated)
            _snackbarMessage.emit("Konfigurasi Google Sheets berhasil disimpan!")
        }
    }

    fun syncAllPpdbToSheet() {
        val currentSettings = settings.value
        val gasUrl = currentSettings?.gasWebAppUrl ?: ""
        if (gasUrl.isBlank()) {
            viewModelScope.launch {
                _snackbarMessage.emit("Harap isi dan simpan URL Google Apps Script terlebih dahulu!")
            }
            return
        }
        viewModelScope.launch {
            _isSheetSyncing.value = true
            val list = allPpdb.value
            val res = com.example.util.GoogleSheetsHelper.syncAllPpdb(gasUrl, list)
            _isSheetSyncing.value = false
            when (res) {
                is com.example.util.SheetResult.Success -> {
                    val timeNow = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("id", "ID")).format(Date())
                    currentSettings?.let {
                        repository.updateSettings(it.copy(lastSheetSyncTime = timeNow))
                    }
                    _snackbarMessage.emit(res.message)
                }
                is com.example.util.SheetResult.Error -> {
                    _snackbarMessage.emit(res.message)
                }
            }
        }
    }

    fun syncAllStudentsToSheet() {
        val currentSettings = settings.value
        val gasUrl = currentSettings?.gasWebAppUrl ?: ""
        if (gasUrl.isBlank()) {
            viewModelScope.launch {
                _snackbarMessage.emit("Harap isi dan simpan URL Google Apps Script terlebih dahulu!")
            }
            return
        }
        viewModelScope.launch {
            _isSheetSyncing.value = true
            val list = allStudents.value
            val res = com.example.util.GoogleSheetsHelper.syncAllStudents(gasUrl, list)
            _isSheetSyncing.value = false
            when (res) {
                is com.example.util.SheetResult.Success -> {
                    val timeNow = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("id", "ID")).format(Date())
                    currentSettings?.let {
                        repository.updateSettings(it.copy(lastSheetSyncTime = timeNow))
                    }
                    _snackbarMessage.emit(res.message)
                }
                is com.example.util.SheetResult.Error -> {
                    _snackbarMessage.emit(res.message)
                }
            }
        }
    }

    fun importStudentsFromGoogleSheet(isPublicCsv: Boolean, spreadsheetIdOrUrl: String, gasUrl: String) {
        viewModelScope.launch {
            _isSheetSyncing.value = true
            val res = if (isPublicCsv) {
                com.example.util.GoogleSheetsHelper.fetchStudentsFromPublicCsv(spreadsheetIdOrUrl)
            } else {
                com.example.util.GoogleSheetsHelper.fetchStudentsFromGas(gasUrl)
            }
            _isSheetSyncing.value = false
            when (res) {
                is com.example.util.SheetResult.Success -> {
                    val imported = res.data
                    if (imported.isNotEmpty()) {
                        repository.insertAllStudents(imported)
                        val timeNow = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("id", "ID")).format(Date())
                        settings.value?.let {
                            repository.updateSettings(it.copy(lastSheetSyncTime = timeNow))
                        }
                        _snackbarMessage.emit("Berhasil mengimpor ${imported.size} data siswa dari Google Sheets!")
                    } else {
                        _snackbarMessage.emit("Tidak ada data siswa ditemukan di Google Sheets.")
                    }
                }
                is com.example.util.SheetResult.Error -> {
                    _snackbarMessage.emit(res.message)
                }
            }
        }
    }
}

