package com.example.data.remote

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.example.R
import com.example.data.local.*
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import org.json.JSONObject

enum class OperationType(val value: String) {
    CREATE("create"),
    UPDATE("update"),
    DELETE("delete"),
    LIST("list"),
    GET("get"),
    WRITE("write"),
}

fun handleFirestoreError(exception: Exception, operationType: OperationType, path: String?): String {
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    val providerInfoList = currentUser?.providerData?.map { provider ->
        JSONObject().apply {
            put("providerId", provider.providerId)
            put("email", provider.email)
        }
    } ?: emptyList()

    val authInfoJson = JSONObject().apply {
        put("userId", currentUser?.uid)
        put("email", currentUser?.email)
        put("emailVerified", currentUser?.isEmailVerified)
        put("tenantId", currentUser?.tenantId)
        put("providerInfo", JSONArray(providerInfoList))
    }

    val errorInfoJson = JSONObject().apply {
        put("error", exception.message ?: exception.toString())
        put("operationType", operationType.value)
        put("path", path)
        put("authInfo", authInfoJson)
    }

    val jsonString = errorInfoJson.toString()
    Log.e("FirestoreError", "Firestore Error: $jsonString")
    return jsonString
}

class FirestoreSyncManager(
    private val context: Context,
    private val db: AppDatabase,
    private val scope: CoroutineScope
) {
    companion object {
        private const val TAG = "FirestoreSyncManager"
    }

    private val databaseId: String = context.getString(R.string.firestore_database_id)
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(databaseId)
    val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _isCloudSyncing = MutableStateFlow(false)
    val isCloudSyncing: StateFlow<Boolean> = _isCloudSyncing.asStateFlow()

    private val _cloudSyncStatus = MutableStateFlow<String?>("Cloud Sync Aktif (Multi-Device Firebase)")
    val cloudSyncStatus: StateFlow<String?> = _cloudSyncStatus.asStateFlow()

    private val _currentAuthUser = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val currentAuthUser: StateFlow<FirebaseUser?> = _currentAuthUser.asStateFlow()

    private val listeners = mutableListOf<ListenerRegistration>()

    init {
        auth.addAuthStateListener { firebaseAuth ->
            _currentAuthUser.value = firebaseAuth.currentUser
        }
        startRealtimeListeners()
    }

    /**
     * Start real-time snapshot listeners for all 15 school data modules.
     * When ANY data is saved or updated on Device 1, Device 2 immediately receives it in real-time!
     */
    fun startRealtimeListeners() {
        stopRealtimeListeners()

        // 1. School Settings
        try {
            val reg = firestore.collection("school_settings").document("general")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        handleFirestoreError(error, OperationType.GET, "school_settings/general")
                        return@addSnapshotListener
                    }
                    if (snapshot != null && snapshot.exists()) {
                        scope.launch(Dispatchers.IO) {
                            try {
                                val current = db.schoolSettingsDao().getSettingsOnce() ?: SchoolSettingsEntity()
                                val updated = current.copy(
                                    schoolName = snapshot.getString("schoolName") ?: current.schoolName,
                                    npsn = snapshot.getString("npsn") ?: current.npsn,
                                    nsm = snapshot.getString("nsm") ?: current.nsm,
                                    accreditation = snapshot.getString("accreditation") ?: current.accreditation,
                                    headmasterName = snapshot.getString("headmasterName") ?: snapshot.getString("principalName") ?: current.headmasterName,
                                    phoneWa = snapshot.getString("phoneWa") ?: snapshot.getString("phone") ?: current.phoneWa,
                                    email = snapshot.getString("email") ?: current.email,
                                    address = snapshot.getString("address") ?: current.address,
                                    visionText = snapshot.getString("visionText") ?: current.visionText,
                                    missionText = snapshot.getString("missionText") ?: current.missionText,
                                    goalsText = snapshot.getString("goalsText") ?: current.goalsText,
                                    historyText = snapshot.getString("historyText") ?: current.historyText,
                                    facebookUrl = snapshot.getString("facebookUrl") ?: current.facebookUrl,
                                    instagramUrl = snapshot.getString("instagramUrl") ?: current.instagramUrl,
                                    tiktokUrl = snapshot.getString("tiktokUrl") ?: current.tiktokUrl,
                                    morningAssemblyTime = snapshot.getString("morningAssemblyTime") ?: current.morningAssemblyTime,
                                    classStartTime = snapshot.getString("classStartTime") ?: current.classStartTime,
                                    classEndTimeMonThu = snapshot.getString("classEndTimeMonThu") ?: current.classEndTimeMonThu,
                                    classEndTimeFri = snapshot.getString("classEndTimeFri") ?: current.classEndTimeFri,
                                    classEndTimeSat = snapshot.getString("classEndTimeSat") ?: current.classEndTimeSat,
                                    schoolScheduleRules = snapshot.getString("schoolScheduleRules") ?: current.schoolScheduleRules
                                )
                                db.schoolSettingsDao().insertOrUpdate(updated)
                                _cloudSyncStatus.value = "Pengaturan madrasah live sync aktif"
                            } catch (e: Exception) {
                                Log.e(TAG, "Error caching settings", e)
                            }
                        }
                    }
                }
            listeners.add(reg)
        } catch (e: Exception) { Log.e(TAG, "listen school_settings err", e) }

        // 2. Students
        try {
            val reg = firestore.collection("students")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        handleFirestoreError(error, OperationType.LIST, "students")
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        scope.launch(Dispatchers.IO) {
                            try {
                                val list = snapshot.documents.mapNotNull { doc ->
                                    val name = doc.getString("fullName") ?: return@mapNotNull null
                                    val classLvl = doc.getLong("classLevel")?.toInt() ?: 1
                                    StudentEntity(
                                        fullName = name,
                                        nisn = doc.getString("nisn") ?: doc.id,
                                        birthPlace = doc.getString("birthPlace") ?: "Lampung Timur",
                                        birthDate = doc.getString("birthDate") ?: "",
                                        classLevel = classLvl.coerceIn(1, 6),
                                        rombel = doc.getString("rombel") ?: "Ar Rahman",
                                        gender = doc.getString("gender") ?: "Laki-laki",
                                        address = doc.getString("address") ?: "Tanjung Qencono",
                                        isPublished = doc.getBoolean("isPublished") ?: true,
                                        cachedAt = System.currentTimeMillis()
                                    )
                                }
                                if (list.isNotEmpty() || snapshot.isEmpty) {
                                    db.studentDao().clearAllStudents()
                                    if (list.isNotEmpty()) db.studentDao().insertAll(list)
                                    _cloudSyncStatus.value = "Data siswa tersinkron: ${list.size} siswa"
                                }
                            } catch (e: Exception) { Log.e(TAG, "Error caching students", e) }
                        }
                    }
                }
            listeners.add(reg)
        } catch (e: Exception) { Log.e(TAG, "listen students err", e) }

        // 3. Teachers & Staff
        try {
            val reg = firestore.collection("teachers")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        handleFirestoreError(error, OperationType.LIST, "teachers")
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        scope.launch(Dispatchers.IO) {
                            try {
                                val list = snapshot.documents.mapNotNull { doc ->
                                    val name = doc.getString("name") ?: return@mapNotNull null
                                    TeacherEntity(
                                        name = name,
                                        role = doc.getString("role") ?: "Guru",
                                        subject = doc.getString("subject") ?: "",
                                        photoUrl = doc.getString("photoUrl") ?: "",
                                        description = doc.getString("description") ?: "",
                                        phone = doc.getString("phone") ?: ""
                                    )
                                }
                                if (list.isNotEmpty() || snapshot.isEmpty) {
                                    db.teacherDao().clearAllTeachers()
                                    if (list.isNotEmpty()) db.teacherDao().insertAll(list)
                                }
                            } catch (e: Exception) { Log.e(TAG, "Error caching teachers", e) }
                        }
                    }
                }
            listeners.add(reg)
        } catch (e: Exception) { Log.e(TAG, "listen teachers err", e) }

        // 4. Facilities
        try {
            val reg = firestore.collection("facilities")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        handleFirestoreError(error, OperationType.LIST, "facilities")
                        return@addSnapshotListener
                    }
                    if (snapshot != null && !snapshot.isEmpty) {
                        scope.launch(Dispatchers.IO) {
                            try {
                                val list = snapshot.documents.mapNotNull { doc ->
                                    val title = doc.getString("title") ?: doc.getString("name") ?: return@mapNotNull null
                                    FacilityEntity(
                                        name = title,
                                        category = doc.getString("category") ?: "Sarana Utama",
                                        description = doc.getString("description") ?: "",
                                        condition = doc.getString("condition") ?: "Sangat Baik"
                                    )
                                }
                                if (list.isNotEmpty()) db.facilityDao().insertAll(list)
                            } catch (e: Exception) { Log.e(TAG, "Error caching facilities", e) }
                        }
                    }
                }
            listeners.add(reg)
        } catch (e: Exception) { Log.e(TAG, "listen facilities err", e) }

        // 5. Notifications
        try {
            val reg = firestore.collection("notifications")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        handleFirestoreError(error, OperationType.LIST, "notifications")
                        return@addSnapshotListener
                    }
                    if (snapshot != null && !snapshot.isEmpty) {
                        scope.launch(Dispatchers.IO) {
                            try {
                                val list = snapshot.documents.mapNotNull { doc ->
                                    val title = doc.getString("title") ?: return@mapNotNull null
                                    AppNotificationEntity(
                                        title = title,
                                        message = doc.getString("content") ?: doc.getString("message") ?: "",
                                        dateFormatted = doc.getString("date") ?: "",
                                        category = doc.getString("type") ?: "Pengumuman"
                                    )
                                }
                                if (list.isNotEmpty()) db.appNotificationDao().insertAll(list)
                            } catch (e: Exception) { Log.e(TAG, "Error caching notifications", e) }
                        }
                    }
                }
            listeners.add(reg)
        } catch (e: Exception) { Log.e(TAG, "listen notifications err", e) }

        // 6. News
        try {
            val reg = firestore.collection("news")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        handleFirestoreError(error, OperationType.LIST, "news")
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        scope.launch(Dispatchers.IO) {
                            try {
                                val list = snapshot.documents.mapNotNull { doc ->
                                    val title = doc.getString("title") ?: return@mapNotNull null
                                    NewsEntity(
                                        title = title,
                                        category = doc.getString("category") ?: "Kegiatan",
                                        summary = doc.getString("summary") ?: "",
                                        content = doc.getString("content") ?: "",
                                        date = doc.getString("date") ?: "",
                                        imageUrl = doc.getString("imageUrl") ?: ""
                                    )
                                }
                                if (list.isNotEmpty() || snapshot.isEmpty) {
                                    db.newsDao().clearAllNews()
                                    if (list.isNotEmpty()) db.newsDao().insertAll(list)
                                }
                            } catch (e: Exception) { Log.e(TAG, "Error caching news", e) }
                        }
                    }
                }
            listeners.add(reg)
        } catch (e: Exception) { Log.e(TAG, "listen news err", e) }

        // 7. Gallery
        try {
            val reg = firestore.collection("gallery")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        handleFirestoreError(error, OperationType.LIST, "gallery")
                        return@addSnapshotListener
                    }
                    if (snapshot != null && !snapshot.isEmpty) {
                        scope.launch(Dispatchers.IO) {
                            try {
                                val list = snapshot.documents.mapNotNull { doc ->
                                    val title = doc.getString("title") ?: return@mapNotNull null
                                    GalleryEntity(
                                        title = title,
                                        category = doc.getString("category") ?: "Kegiatan",
                                        description = doc.getString("description") ?: "",
                                        date = doc.getString("date") ?: "",
                                        imageUrl = doc.getString("imageUrl") ?: ""
                                    )
                                }
                                if (list.isNotEmpty()) db.galleryDao().insertAll(list)
                            } catch (e: Exception) { Log.e(TAG, "Error caching gallery", e) }
                        }
                    }
                }
            listeners.add(reg)
        } catch (e: Exception) { Log.e(TAG, "listen gallery err", e) }

        // 8. Extracurriculars
        try {
            val reg = firestore.collection("extracurriculars")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) return@addSnapshotListener
                    if (snapshot != null && !snapshot.isEmpty) {
                        scope.launch(Dispatchers.IO) {
                            try {
                                val list = snapshot.documents.mapNotNull { doc ->
                                    val name = doc.getString("name") ?: return@mapNotNull null
                                    ExtracurricularEntity(
                                        name = name,
                                        coach = doc.getString("coach") ?: "",
                                        schedule = doc.getString("schedule") ?: "",
                                        description = doc.getString("description") ?: "",
                                        imageUrl = doc.getString("imageUrl") ?: ""
                                    )
                                }
                                if (list.isNotEmpty()) db.extracurricularDao().insertAll(list)
                            } catch (e: Exception) { Log.e(TAG, "Error caching extracurriculars", e) }
                        }
                    }
                }
            listeners.add(reg)
        } catch (e: Exception) { Log.e(TAG, "listen extracurriculars err", e) }

        // 9. Featured Programs
        try {
            val reg = firestore.collection("featured_programs")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) return@addSnapshotListener
                    if (snapshot != null && !snapshot.isEmpty) {
                        scope.launch(Dispatchers.IO) {
                            try {
                                val list = snapshot.documents.mapNotNull { doc ->
                                    val name = doc.getString("name") ?: return@mapNotNull null
                                    FeaturedProgramEntity(
                                        name = name,
                                        objective = doc.getString("objective") ?: "",
                                        description = doc.getString("description") ?: "",
                                        imageUrl = doc.getString("imageUrl") ?: ""
                                    )
                                }
                                if (list.isNotEmpty()) db.featuredProgramDao().insertAll(list)
                            } catch (e: Exception) { Log.e(TAG, "Error caching programs", e) }
                        }
                    }
                }
            listeners.add(reg)
        } catch (e: Exception) { Log.e(TAG, "listen featured_programs err", e) }

        // 10. Agendas
        try {
            val reg = firestore.collection("agendas")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) return@addSnapshotListener
                    if (snapshot != null && !snapshot.isEmpty) {
                        scope.launch(Dispatchers.IO) {
                            try {
                                val list = snapshot.documents.mapNotNull { doc ->
                                    val title = doc.getString("title") ?: return@mapNotNull null
                                    AgendaEntity(
                                        title = title,
                                        description = doc.getString("description") ?: "",
                                        date = doc.getString("date") ?: "",
                                        time = doc.getString("time") ?: "",
                                        location = doc.getString("location") ?: "",
                                        imageUrl = doc.getString("imageUrl") ?: ""
                                    )
                                }
                                if (list.isNotEmpty()) db.agendaDao().insertAll(list)
                            } catch (e: Exception) { Log.e(TAG, "Error caching agendas", e) }
                        }
                    }
                }
            listeners.add(reg)
        } catch (e: Exception) { Log.e(TAG, "listen agendas err", e) }

        // 11. Testimonials
        try {
            val reg = firestore.collection("testimonials")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) return@addSnapshotListener
                    if (snapshot != null && !snapshot.isEmpty) {
                        scope.launch(Dispatchers.IO) {
                            try {
                                val list = snapshot.documents.mapNotNull { doc ->
                                    val name = doc.getString("name") ?: return@mapNotNull null
                                    TestimonialEntity(
                                        name = name,
                                        role = doc.getString("role") ?: "Wali Murid",
                                        content = doc.getString("content") ?: "",
                                        rating = doc.getLong("rating")?.toInt() ?: 5,
                                        isApproved = doc.getBoolean("isApproved") ?: false,
                                        date = doc.getString("date") ?: ""
                                    )
                                }
                                if (list.isNotEmpty()) db.testimonialDao().insertAll(list)
                            } catch (e: Exception) { Log.e(TAG, "Error caching testimonials", e) }
                        }
                    }
                }
            listeners.add(reg)
        } catch (e: Exception) { Log.e(TAG, "listen testimonials err", e) }

        // 12. RPP Modules
        try {
            val reg = firestore.collection("rpp")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) return@addSnapshotListener
                    if (snapshot != null && !snapshot.isEmpty) {
                        scope.launch(Dispatchers.IO) {
                            try {
                                val list = snapshot.documents.mapNotNull { doc ->
                                    val title = doc.getString("title") ?: return@mapNotNull null
                                    RppEntity(
                                        title = title,
                                        teacherName = doc.getString("teacherName") ?: "",
                                        classLevel = doc.getLong("classLevel")?.toInt() ?: 1,
                                        rombel = doc.getString("rombel") ?: "Semua",
                                        subject = doc.getString("subject") ?: "",
                                        uploadDate = doc.getString("uploadDate") ?: "",
                                        fileName = doc.getString("fileName") ?: "",
                                        fileSize = doc.getString("fileSize") ?: "1.2 MB",
                                        fileType = doc.getString("fileType") ?: "PDF"
                                    )
                                }
                                if (list.isNotEmpty()) db.rppDao().insertAll(list)
                            } catch (e: Exception) { Log.e(TAG, "Error caching rpp", e) }
                        }
                    }
                }
            listeners.add(reg)
        } catch (e: Exception) { Log.e(TAG, "listen rpp err", e) }

        // 13. Official Documents
        try {
            val reg = firestore.collection("documents")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) return@addSnapshotListener
                    if (snapshot != null && !snapshot.isEmpty) {
                        scope.launch(Dispatchers.IO) {
                            try {
                                val list = snapshot.documents.mapNotNull { doc ->
                                    val title = doc.getString("title") ?: return@mapNotNull null
                                    DocumentEntity(
                                        title = title,
                                        folderName = doc.getString("folderName") ?: "Umum",
                                        classLevel = doc.getString("classLevel") ?: "Semua",
                                        uploadDate = doc.getString("uploadDate") ?: "",
                                        fileName = doc.getString("fileName") ?: "",
                                        fileType = doc.getString("fileType") ?: "PDF",
                                        fileSize = doc.getString("fileSize") ?: "500 KB"
                                    )
                                }
                                if (list.isNotEmpty()) db.documentDao().insertAll(list)
                            } catch (e: Exception) { Log.e(TAG, "Error caching documents", e) }
                        }
                    }
                }
            listeners.add(reg)
        } catch (e: Exception) { Log.e(TAG, "listen documents err", e) }

        // 14. Class Schedules
        try {
            val reg = firestore.collection("class_schedules")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) return@addSnapshotListener
                    if (snapshot != null && !snapshot.isEmpty) {
                        scope.launch(Dispatchers.IO) {
                            try {
                                val list = snapshot.documents.mapNotNull { doc ->
                                    val subject = doc.getString("subject") ?: return@mapNotNull null
                                    ClassScheduleEntity(
                                        classLevel = doc.getLong("classLevel")?.toInt() ?: 1,
                                        rombel = doc.getString("rombel") ?: "Ar Rahman",
                                        day = doc.getString("day") ?: "Senin",
                                        timeSlot = doc.getString("timeSlot") ?: "07.30 - 08.45 WIB",
                                        subject = subject,
                                        teacherName = doc.getString("teacherName") ?: "Ustadz / Ustadzah"
                                    )
                                }
                                if (list.isNotEmpty()) db.classScheduleDao().insertAll(list)
                            } catch (e: Exception) { Log.e(TAG, "Error caching class schedules", e) }
                        }
                    }
                }
            listeners.add(reg)
        } catch (e: Exception) { Log.e(TAG, "listen class_schedules err", e) }
    }

    fun stopRealtimeListeners() {
        listeners.forEach { it.remove() }
        listeners.clear()
    }

    /**
     * Sign In with Google via Jetpack Credential Manager.
     */
    suspend fun signInWithGoogle(): Result<FirebaseUser> {
        return try {
            val credentialManager = CredentialManager.create(context)
            val serverClientId = context.getString(R.string.default_web_client_id)

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(serverClientId)
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(context = context, request = request)
            val credential = result.credential
            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val authCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
                val authResult = auth.signInWithCredential(authCredential).await()
                val user = authResult.user ?: error("Login Google gagal: User null")
                _currentAuthUser.value = user
                _cloudSyncStatus.value = "Terhubung: ${user.displayName ?: user.email}"
                Result.success(user)
            } else {
                Result.failure(Exception("Kredensial Google tidak dikenali"))
            }
        } catch (e: GetCredentialException) {
            Log.w(TAG, "Credential Manager error or user cancelled", e)
            Result.failure(e)
        } catch (e: Exception) {
            Log.e(TAG, "Error during Google Sign-In", e)
            Result.failure(e)
        }
    }

    fun signOut() {
        auth.signOut()
        _currentAuthUser.value = null
        _cloudSyncStatus.value = "Sesi keluar"
    }

    // --- PUSH OPERATIONS (DEVICE 1 -> CLOUD) ---

    suspend fun pushSchoolSettings(settings: SchoolSettingsEntity): Result<Unit> {
        return try {
            _isCloudSyncing.value = true
            val data = hashMapOf(
                "schoolName" to settings.schoolName,
                "npsn" to settings.npsn,
                "nsm" to settings.nsm,
                "accreditation" to settings.accreditation,
                "principalName" to settings.headmasterName,
                "headmasterName" to settings.headmasterName,
                "phone" to settings.phoneWa,
                "phoneWa" to settings.phoneWa,
                "email" to settings.email,
                "address" to settings.address,
                "visionText" to settings.visionText,
                "missionText" to settings.missionText,
                "goalsText" to settings.goalsText,
                "historyText" to settings.historyText,
                "facebookUrl" to settings.facebookUrl,
                "instagramUrl" to settings.instagramUrl,
                "tiktokUrl" to settings.tiktokUrl,
                "morningAssemblyTime" to settings.morningAssemblyTime,
                "classStartTime" to settings.classStartTime,
                "classEndTimeMonThu" to settings.classEndTimeMonThu,
                "classEndTimeFri" to settings.classEndTimeFri,
                "classEndTimeSat" to settings.classEndTimeSat,
                "schoolScheduleRules" to settings.schoolScheduleRules,
                "updatedAt" to java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss", java.util.Locale("id", "ID")).format(java.util.Date())
            )
            firestore.collection("school_settings").document("general")
                .set(data, SetOptions.merge())
                .await()
            _cloudSyncStatus.value = "Pengaturan madrasah tersimpan di Cloud"
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "school_settings/general")
            Result.failure(e)
        } finally {
            _isCloudSyncing.value = false
        }
    }

    suspend fun pushStudent(student: StudentEntity): Result<Unit> {
        return try {
            val docId = if (student.nisn.isNotBlank()) "nisn_${student.nisn.trim()}" else "std_${student.id.takeIf { it > 0 } ?: System.currentTimeMillis()}"
            val data = hashMapOf(
                "fullName" to student.fullName,
                "nisn" to student.nisn,
                "birthPlace" to student.birthPlace,
                "birthDate" to student.birthDate,
                "classLevel" to student.classLevel,
                "rombel" to student.rombel,
                "gender" to student.gender,
                "address" to student.address,
                "isPublished" to student.isPublished
            )
            firestore.collection("students").document(docId).set(data).await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "students")
            Result.failure(e)
        }
    }

    suspend fun pushAllStudents(students: List<StudentEntity>): Result<Int> {
        if (students.isEmpty()) return Result.success(0)
        return try {
            _isCloudSyncing.value = true
            val batch = firestore.batch()
            students.forEachIndexed { index, student ->
                val docId = if (student.nisn.isNotBlank()) "nisn_${student.nisn.trim()}" else "std_${index + 1}"
                val docRef = firestore.collection("students").document(docId)
                val data = hashMapOf(
                    "fullName" to student.fullName,
                    "nisn" to student.nisn,
                    "birthPlace" to student.birthPlace,
                    "birthDate" to student.birthDate,
                    "classLevel" to student.classLevel,
                    "rombel" to student.rombel,
                    "gender" to student.gender,
                    "address" to student.address,
                    "isPublished" to student.isPublished
                )
                batch.set(docRef, data)
            }
            batch.commit().await()
            Result.success(students.size)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "students")
            Result.failure(e)
        } finally {
            _isCloudSyncing.value = false
        }
    }

    suspend fun deleteStudent(student: StudentEntity): Result<Unit> {
        return try {
            val docId = if (student.nisn.isNotBlank()) "nisn_${student.nisn.trim()}" else "std_${student.id}"
            firestore.collection("students").document(docId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, "students")
            Result.failure(e)
        }
    }

    suspend fun deleteAllStudents(): Result<Unit> {
        return try {
            val snapshot = firestore.collection("students").get().await()
            val batch = firestore.batch()
            snapshot.documents.forEach { batch.delete(it.reference) }
            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, "students")
            Result.failure(e)
        }
    }

    // Teacher
    suspend fun pushTeacher(teacher: TeacherEntity): Result<Unit> {
        return try {
            val id = "tch_${teacher.id.takeIf { it > 0 } ?: System.currentTimeMillis()}"
            val data = hashMapOf(
                "name" to teacher.name,
                "role" to teacher.role,
                "subject" to teacher.subject,
                "photoUrl" to teacher.photoUrl,
                "description" to teacher.description,
                "phone" to teacher.phone
            )
            firestore.collection("teachers").document(id).set(data).await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "teachers")
            Result.failure(e)
        }
    }

    suspend fun deleteTeacher(teacher: TeacherEntity): Result<Unit> {
        return try {
            val id = "tch_${teacher.id}"
            firestore.collection("teachers").document(id).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, "teachers")
            Result.failure(e)
        }
    }

    // Facility
    suspend fun pushFacility(facility: FacilityEntity): Result<Unit> {
        return try {
            val id = "fac_${facility.id.takeIf { it > 0 } ?: System.currentTimeMillis()}"
            val data = hashMapOf(
                "title" to facility.name,
                "category" to facility.category,
                "description" to facility.description,
                "condition" to facility.condition
            )
            firestore.collection("facilities").document(id).set(data).await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "facilities")
            Result.failure(e)
        }
    }

    suspend fun deleteFacility(facility: FacilityEntity): Result<Unit> {
        return try {
            val id = "fac_${facility.id}"
            firestore.collection("facilities").document(id).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, "facilities")
            Result.failure(e)
        }
    }

    // Notification
    suspend fun pushNotification(notif: AppNotificationEntity): Result<Unit> {
        return try {
            val id = "notif_${notif.id.takeIf { it > 0 } ?: System.currentTimeMillis()}"
            val data = hashMapOf(
                "title" to notif.title,
                "content" to notif.message,
                "date" to notif.dateFormatted,
                "type" to notif.category
            )
            firestore.collection("notifications").document(id).set(data).await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "notifications")
            Result.failure(e)
        }
    }

    // News
    suspend fun pushNews(news: NewsEntity): Result<Unit> {
        return try {
            val id = "news_${news.id.takeIf { it > 0 } ?: System.currentTimeMillis()}"
            val data = hashMapOf(
                "title" to news.title,
                "category" to news.category,
                "summary" to news.summary,
                "content" to news.content,
                "date" to news.date,
                "imageUrl" to news.imageUrl
            )
            firestore.collection("news").document(id).set(data).await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "news")
            Result.failure(e)
        }
    }

    suspend fun deleteNews(news: NewsEntity): Result<Unit> {
        return try {
            firestore.collection("news").document("news_${news.id}").delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, "news")
            Result.failure(e)
        }
    }

    // Gallery
    suspend fun pushGallery(item: GalleryEntity): Result<Unit> {
        return try {
            val id = "gal_${item.id.takeIf { it > 0 } ?: System.currentTimeMillis()}"
            val data = hashMapOf(
                "title" to item.title,
                "category" to item.category,
                "description" to item.description,
                "date" to item.date,
                "imageUrl" to item.imageUrl
            )
            firestore.collection("gallery").document(id).set(data).await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "gallery")
            Result.failure(e)
        }
    }

    suspend fun deleteGallery(item: GalleryEntity): Result<Unit> {
        return try {
            firestore.collection("gallery").document("gal_${item.id}").delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, "gallery")
            Result.failure(e)
        }
    }

    // Extracurricular
    suspend fun pushExtracurricular(extra: ExtracurricularEntity): Result<Unit> {
        return try {
            val id = "ext_${extra.id.takeIf { it > 0 } ?: System.currentTimeMillis()}"
            val data = hashMapOf(
                "name" to extra.name,
                "coach" to extra.coach,
                "schedule" to extra.schedule,
                "description" to extra.description,
                "imageUrl" to extra.imageUrl
            )
            firestore.collection("extracurriculars").document(id).set(data).await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "extracurriculars")
            Result.failure(e)
        }
    }

    suspend fun deleteExtracurricular(extra: ExtracurricularEntity): Result<Unit> {
        return try {
            firestore.collection("extracurriculars").document("ext_${extra.id}").delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, "extracurriculars")
            Result.failure(e)
        }
    }

    // Program
    suspend fun pushProgram(prog: FeaturedProgramEntity): Result<Unit> {
        return try {
            val id = "prog_${prog.id.takeIf { it > 0 } ?: System.currentTimeMillis()}"
            val data = hashMapOf(
                "name" to prog.name,
                "objective" to prog.objective,
                "description" to prog.description,
                "imageUrl" to prog.imageUrl
            )
            firestore.collection("featured_programs").document(id).set(data).await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "featured_programs")
            Result.failure(e)
        }
    }

    suspend fun deleteProgram(prog: FeaturedProgramEntity): Result<Unit> {
        return try {
            firestore.collection("featured_programs").document("prog_${prog.id}").delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, "featured_programs")
            Result.failure(e)
        }
    }

    // Agenda
    suspend fun pushAgenda(agenda: AgendaEntity): Result<Unit> {
        return try {
            val id = "agd_${agenda.id.takeIf { it > 0 } ?: System.currentTimeMillis()}"
            val data = hashMapOf(
                "title" to agenda.title,
                "description" to agenda.description,
                "date" to agenda.date,
                "time" to agenda.time,
                "location" to agenda.location,
                "imageUrl" to agenda.imageUrl
            )
            firestore.collection("agendas").document(id).set(data).await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "agendas")
            Result.failure(e)
        }
    }

    suspend fun deleteAgenda(agenda: AgendaEntity): Result<Unit> {
        return try {
            firestore.collection("agendas").document("agd_${agenda.id}").delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, "agendas")
            Result.failure(e)
        }
    }

    // Testimonial
    suspend fun pushTestimonial(testi: TestimonialEntity): Result<Unit> {
        return try {
            val id = "tst_${testi.id.takeIf { it > 0 } ?: System.currentTimeMillis()}"
            val data = hashMapOf(
                "name" to testi.name,
                "role" to testi.role,
                "content" to testi.content,
                "rating" to testi.rating,
                "isApproved" to testi.isApproved,
                "date" to testi.date
            )
            firestore.collection("testimonials").document(id).set(data).await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "testimonials")
            Result.failure(e)
        }
    }

    suspend fun deleteTestimonial(testi: TestimonialEntity): Result<Unit> {
        return try {
            firestore.collection("testimonials").document("tst_${testi.id}").delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, "testimonials")
            Result.failure(e)
        }
    }

    // RPP
    suspend fun pushRpp(rpp: RppEntity): Result<Unit> {
        return try {
            val id = "rpp_${rpp.id.takeIf { it > 0 } ?: System.currentTimeMillis()}"
            val data = hashMapOf(
                "title" to rpp.title,
                "teacherName" to rpp.teacherName,
                "classLevel" to rpp.classLevel,
                "rombel" to rpp.rombel,
                "subject" to rpp.subject,
                "uploadDate" to rpp.uploadDate,
                "fileName" to rpp.fileName,
                "fileSize" to rpp.fileSize,
                "fileType" to rpp.fileType
            )
            firestore.collection("rpp").document(id).set(data).await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "rpp")
            Result.failure(e)
        }
    }

    suspend fun deleteRpp(rpp: RppEntity): Result<Unit> {
        return try {
            firestore.collection("rpp").document("rpp_${rpp.id}").delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, "rpp")
            Result.failure(e)
        }
    }

    // Document
    suspend fun pushDocument(doc: DocumentEntity): Result<Unit> {
        return try {
            val id = "doc_${doc.id.takeIf { it > 0 } ?: System.currentTimeMillis()}"
            val data = hashMapOf(
                "title" to doc.title,
                "folderName" to doc.folderName,
                "classLevel" to doc.classLevel,
                "uploadDate" to doc.uploadDate,
                "fileName" to doc.fileName,
                "fileType" to doc.fileType,
                "fileSize" to doc.fileSize
            )
            firestore.collection("documents").document(id).set(data).await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "documents")
            Result.failure(e)
        }
    }

    suspend fun deleteDocument(doc: DocumentEntity): Result<Unit> {
        return try {
            firestore.collection("documents").document("doc_${doc.id}").delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, "documents")
            Result.failure(e)
        }
    }

    // Class Schedule
    suspend fun pushClassSchedule(sch: ClassScheduleEntity): Result<Unit> {
        return try {
            val id = "sch_${sch.id.takeIf { it > 0 } ?: System.currentTimeMillis()}"
            val data = hashMapOf(
                "classLevel" to sch.classLevel,
                "rombel" to sch.rombel,
                "day" to sch.day,
                "timeSlot" to sch.timeSlot,
                "subject" to sch.subject,
                "teacherName" to sch.teacherName
            )
            firestore.collection("class_schedules").document(id).set(data).await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "class_schedules")
            Result.failure(e)
        }
    }

    suspend fun deleteClassSchedule(sch: ClassScheduleEntity): Result<Unit> {
        return try {
            firestore.collection("class_schedules").document("sch_${sch.id}").delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, "class_schedules")
            Result.failure(e)
        }
    }

    // PPDB
    suspend fun submitPpdb(ppdb: PpdbEntity): Result<Unit> {
        return try {
            val cleanReg = ppdb.regNumber.replace(Regex("[^a-zA-Z0-9_-]"), "_")
            val data = hashMapOf(
                "regNumber" to ppdb.regNumber,
                "studentName" to ppdb.studentName,
                "nisn" to ppdb.nisn,
                "nik" to ppdb.nik,
                "birthPlace" to ppdb.birthPlace,
                "birthDate" to ppdb.birthDate,
                "gender" to ppdb.gender,
                "address" to ppdb.address,
                "whatsapp" to ppdb.whatsapp,
                "prevSchool" to ppdb.prevSchool,
                "fatherName" to ppdb.fatherName,
                "motherName" to ppdb.motherName,
                "status" to ppdb.status,
                "regDate" to ppdb.regDate,
                "adminNotes" to ppdb.adminNotes
            )
            firestore.collection("ppdb").document(cleanReg).set(data).await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, "ppdb")
            Result.failure(e)
        }
    }

    suspend fun deletePpdb(ppdb: PpdbEntity): Result<Unit> {
        return try {
            val cleanReg = ppdb.regNumber.replace(Regex("[^a-zA-Z0-9_-]"), "_")
            firestore.collection("ppdb").document(cleanReg).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, "ppdb")
            Result.failure(e)
        }
    }

    /**
     * Push ALL local data modules to Cloud Firestore in one comprehensive batch!
     * Covers: Settings, Students, Teachers, Facilities, News, Gallery, Extracurriculars,
     * Programs, Agendas, Testimonials, RPP, Documents, Schedules, Notifications, and PPDB.
     */
    suspend fun pushAllDataToCloud(): Result<String> {
        return try {
            _isCloudSyncing.value = true
            var totalCount = 0

            // 1. Settings
            val currentSettings = db.schoolSettingsDao().getSettingsOnce() ?: SchoolSettingsEntity()
            pushSchoolSettings(currentSettings)
            totalCount++

            // 2. Students
            val studentsList = db.studentDao().getAllStudents().first()
            if (studentsList.isNotEmpty()) {
                val batch = firestore.batch()
                studentsList.forEachIndexed { idx, s ->
                    val id = if (s.nisn.isNotBlank()) "nisn_${s.nisn.trim()}" else "std_${idx + 1}"
                    batch.set(firestore.collection("students").document(id), hashMapOf(
                        "fullName" to s.fullName, "nisn" to s.nisn, "birthPlace" to s.birthPlace,
                        "birthDate" to s.birthDate, "classLevel" to s.classLevel, "rombel" to s.rombel,
                        "gender" to s.gender, "address" to s.address, "isPublished" to s.isPublished
                    ))
                }
                batch.commit().await()
                totalCount += studentsList.size
            }

            // 3. Teachers
            val teachersList = db.teacherDao().getAllTeachers().first()
            if (teachersList.isNotEmpty()) {
                val batch = firestore.batch()
                teachersList.forEachIndexed { idx, t ->
                    val id = "tch_${idx + 1}"
                    batch.set(firestore.collection("teachers").document(id), hashMapOf(
                        "name" to t.name, "role" to t.role, "subject" to t.subject,
                        "photoUrl" to t.photoUrl, "description" to t.description, "phone" to t.phone
                    ))
                }
                batch.commit().await()
                totalCount += teachersList.size
            }

            // 4. Facilities
            val facilitiesList = db.facilityDao().getAllFacilities().first()
            if (facilitiesList.isNotEmpty()) {
                val batch = firestore.batch()
                facilitiesList.forEachIndexed { idx, f ->
                    val id = "fac_${idx + 1}"
                    batch.set(firestore.collection("facilities").document(id), hashMapOf(
                        "title" to f.name, "category" to f.category,
                        "description" to f.description, "condition" to f.condition
                    ))
                }
                batch.commit().await()
                totalCount += facilitiesList.size
            }

            // 5. News
            val newsList = db.newsDao().getAllNews().first()
            if (newsList.isNotEmpty()) {
                val batch = firestore.batch()
                newsList.forEachIndexed { idx, n ->
                    val id = "news_${idx + 1}"
                    batch.set(firestore.collection("news").document(id), hashMapOf(
                        "title" to n.title, "category" to n.category, "summary" to n.summary,
                        "content" to n.content, "date" to n.date, "imageUrl" to n.imageUrl
                    ))
                }
                batch.commit().await()
                totalCount += newsList.size
            }

            // 6. Gallery
            val galList = db.galleryDao().getAllGallery().first()
            if (galList.isNotEmpty()) {
                val batch = firestore.batch()
                galList.forEachIndexed { idx, g ->
                    val id = "gal_${idx + 1}"
                    batch.set(firestore.collection("gallery").document(id), hashMapOf(
                        "title" to g.title, "category" to g.category,
                        "description" to g.description, "date" to g.date, "imageUrl" to g.imageUrl
                    ))
                }
                batch.commit().await()
                totalCount += galList.size
            }

            // 7. Extracurriculars
            val extraList = db.extracurricularDao().getAll().first()
            if (extraList.isNotEmpty()) {
                val batch = firestore.batch()
                extraList.forEachIndexed { idx, e ->
                    val id = "ext_${idx + 1}"
                    batch.set(firestore.collection("extracurriculars").document(id), hashMapOf(
                        "name" to e.name, "coach" to e.coach, "schedule" to e.schedule,
                        "description" to e.description, "imageUrl" to e.imageUrl
                    ))
                }
                batch.commit().await()
                totalCount += extraList.size
            }

            // 8. Programs
            val progList = db.featuredProgramDao().getAll().first()
            if (progList.isNotEmpty()) {
                val batch = firestore.batch()
                progList.forEachIndexed { idx, p ->
                    val id = "prog_${idx + 1}"
                    batch.set(firestore.collection("featured_programs").document(id), hashMapOf(
                        "name" to p.name, "objective" to p.objective,
                        "description" to p.description, "imageUrl" to p.imageUrl
                    ))
                }
                batch.commit().await()
                totalCount += progList.size
            }

            // 9. Agendas
            val agendaList = db.agendaDao().getAll().first()
            if (agendaList.isNotEmpty()) {
                val batch = firestore.batch()
                agendaList.forEachIndexed { idx, a ->
                    val id = "agd_${idx + 1}"
                    batch.set(firestore.collection("agendas").document(id), hashMapOf(
                        "title" to a.title, "description" to a.description,
                        "date" to a.date, "time" to a.time, "location" to a.location, "imageUrl" to a.imageUrl
                    ))
                }
                batch.commit().await()
                totalCount += agendaList.size
            }

            // 10. Notifications
            val notifList = db.appNotificationDao().getAllNotifications().first()
            if (notifList.isNotEmpty()) {
                val batch = firestore.batch()
                notifList.forEachIndexed { idx, n ->
                    val id = "notif_${idx + 1}"
                    batch.set(firestore.collection("notifications").document(id), hashMapOf(
                        "title" to n.title, "content" to n.message, "date" to n.dateFormatted, "type" to n.category
                    ))
                }
                batch.commit().await()
                totalCount += notifList.size
            }

            // 11. Testimonials
            val testiList = db.testimonialDao().getAll().first()
            if (testiList.isNotEmpty()) {
                val batch = firestore.batch()
                testiList.forEachIndexed { idx, t ->
                    val id = "tst_${idx + 1}"
                    batch.set(firestore.collection("testimonials").document(id), hashMapOf(
                        "name" to t.name, "role" to t.role, "content" to t.content,
                        "rating" to t.rating, "isApproved" to t.isApproved, "date" to t.date
                    ))
                }
                batch.commit().await()
                totalCount += testiList.size
            }

            // 12. RPP
            val rppList = db.rppDao().getAllRpp().first()
            if (rppList.isNotEmpty()) {
                val batch = firestore.batch()
                rppList.forEachIndexed { idx, r ->
                    val id = "rpp_${idx + 1}"
                    batch.set(firestore.collection("rpp").document(id), hashMapOf(
                        "title" to r.title, "teacherName" to r.teacherName, "classLevel" to r.classLevel,
                        "rombel" to r.rombel, "subject" to r.subject, "uploadDate" to r.uploadDate,
                        "fileName" to r.fileName, "fileSize" to r.fileSize, "fileType" to r.fileType
                    ))
                }
                batch.commit().await()
                totalCount += rppList.size
            }

            // 13. Documents
            val docList = db.documentDao().getAllDocuments().first()
            if (docList.isNotEmpty()) {
                val batch = firestore.batch()
                docList.forEachIndexed { idx, d ->
                    val id = "doc_${idx + 1}"
                    batch.set(firestore.collection("documents").document(id), hashMapOf(
                        "title" to d.title, "folderName" to d.folderName, "classLevel" to d.classLevel,
                        "uploadDate" to d.uploadDate, "fileName" to d.fileName, "fileType" to d.fileType, "fileSize" to d.fileSize
                    ))
                }
                batch.commit().await()
                totalCount += docList.size
            }

            // 14. Class Schedules
            val schList = db.classScheduleDao().getSchedules(0, "").first()
            if (schList.isNotEmpty()) {
                val batch = firestore.batch()
                schList.forEachIndexed { idx, s ->
                    val id = "sch_${idx + 1}"
                    batch.set(firestore.collection("class_schedules").document(id), hashMapOf(
                        "classLevel" to s.classLevel, "rombel" to s.rombel, "day" to s.day,
                        "timeSlot" to s.timeSlot, "subject" to s.subject, "teacherName" to s.teacherName
                    ))
                }
                batch.commit().await()
                totalCount += schList.size
            }

            // 15. PPDB
            val ppdbList = db.ppdbDao().getAllPpdb().first()
            if (ppdbList.isNotEmpty()) {
                val batch = firestore.batch()
                ppdbList.forEach { p ->
                    val cleanReg = p.regNumber.replace(Regex("[^a-zA-Z0-9_-]"), "_")
                    batch.set(firestore.collection("ppdb").document(cleanReg), hashMapOf(
                        "regNumber" to p.regNumber, "studentName" to p.studentName, "nisn" to p.nisn,
                        "nik" to p.nik, "birthPlace" to p.birthPlace, "birthDate" to p.birthDate,
                        "gender" to p.gender, "address" to p.address, "whatsapp" to p.whatsapp,
                        "prevSchool" to p.prevSchool, "fatherName" to p.fatherName, "motherName" to p.motherName,
                        "status" to p.status, "regDate" to p.regDate, "adminNotes" to p.adminNotes
                    ))
                }
                batch.commit().await()
                totalCount += ppdbList.size
            }

            val successMsg = "Seluruh data madrasah ($totalCount item dari 15 modul) berhasil diunggah ke Cloud Firestore!"
            _cloudSyncStatus.value = successMsg
            Result.success(successMsg)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "all_modules_push")
            Result.failure(e)
        } finally {
            _isCloudSyncing.value = false
        }
    }

    /**
     * Force pull all 15 data modules from Cloud Firestore and populate local Room DB immediately!
     */
    suspend fun forcePullFromCloud(): Result<String> {
        return try {
            _isCloudSyncing.value = true
            var pulledCount = 0

            // 1. Settings
            val settingsDoc = firestore.collection("school_settings").document("general").get().await()
            if (settingsDoc.exists()) {
                val current = db.schoolSettingsDao().getSettingsOnce() ?: SchoolSettingsEntity()
                val updated = current.copy(
                    schoolName = settingsDoc.getString("schoolName") ?: current.schoolName,
                    npsn = settingsDoc.getString("npsn") ?: current.npsn,
                    nsm = settingsDoc.getString("nsm") ?: current.nsm,
                    accreditation = settingsDoc.getString("accreditation") ?: current.accreditation,
                    headmasterName = settingsDoc.getString("headmasterName") ?: settingsDoc.getString("principalName") ?: current.headmasterName,
                    phoneWa = settingsDoc.getString("phoneWa") ?: settingsDoc.getString("phone") ?: current.phoneWa,
                    email = settingsDoc.getString("email") ?: current.email,
                    address = settingsDoc.getString("address") ?: current.address,
                    visionText = settingsDoc.getString("visionText") ?: current.visionText,
                    missionText = settingsDoc.getString("missionText") ?: current.missionText,
                    goalsText = settingsDoc.getString("goalsText") ?: current.goalsText,
                    historyText = settingsDoc.getString("historyText") ?: current.historyText,
                    facebookUrl = settingsDoc.getString("facebookUrl") ?: current.facebookUrl,
                    instagramUrl = settingsDoc.getString("instagramUrl") ?: current.instagramUrl,
                    tiktokUrl = settingsDoc.getString("tiktokUrl") ?: current.tiktokUrl
                )
                db.schoolSettingsDao().insertOrUpdate(updated)
                pulledCount++
            }

            // 2. Students
            val stdSnap = firestore.collection("students").get().await()
            val students = stdSnap.documents.mapNotNull { doc ->
                val name = doc.getString("fullName") ?: return@mapNotNull null
                val classLvl = doc.getLong("classLevel")?.toInt() ?: 1
                StudentEntity(
                    fullName = name,
                    nisn = doc.getString("nisn") ?: doc.id,
                    birthPlace = doc.getString("birthPlace") ?: "Lampung Timur",
                    birthDate = doc.getString("birthDate") ?: "",
                    classLevel = classLvl.coerceIn(1, 6),
                    rombel = doc.getString("rombel") ?: "Ar Rahman",
                    gender = doc.getString("gender") ?: "Laki-laki",
                    address = doc.getString("address") ?: "Tanjung Qencono",
                    isPublished = doc.getBoolean("isPublished") ?: true,
                    cachedAt = System.currentTimeMillis()
                )
            }
            if (students.isNotEmpty()) {
                db.studentDao().clearAllStudents()
                db.studentDao().insertAll(students)
                pulledCount += students.size
            }

            // 3. Teachers
            val tchSnap = firestore.collection("teachers").get().await()
            val teachers = tchSnap.documents.mapNotNull { doc ->
                val name = doc.getString("name") ?: return@mapNotNull null
                TeacherEntity(
                    name = name,
                    role = doc.getString("role") ?: "Guru",
                    subject = doc.getString("subject") ?: "",
                    photoUrl = doc.getString("photoUrl") ?: "",
                    description = doc.getString("description") ?: "",
                    phone = doc.getString("phone") ?: ""
                )
            }
            if (teachers.isNotEmpty()) {
                db.teacherDao().clearAllTeachers()
                db.teacherDao().insertAll(teachers)
                pulledCount += teachers.size
            }

            // 4. Facilities
            val facSnap = firestore.collection("facilities").get().await()
            val facilities = facSnap.documents.mapNotNull { doc ->
                val title = doc.getString("title") ?: doc.getString("name") ?: return@mapNotNull null
                FacilityEntity(
                    name = title,
                    category = doc.getString("category") ?: "Sarana Utama",
                    description = doc.getString("description") ?: "",
                    condition = doc.getString("condition") ?: "Sangat Baik"
                )
            }
            if (facilities.isNotEmpty()) {
                db.facilityDao().insertAll(facilities)
                pulledCount += facilities.size
            }

            // 5. News
            val newsSnap = firestore.collection("news").get().await()
            val newsList = newsSnap.documents.mapNotNull { doc ->
                val title = doc.getString("title") ?: return@mapNotNull null
                NewsEntity(
                    title = title,
                    category = doc.getString("category") ?: "Kegiatan",
                    summary = doc.getString("summary") ?: "",
                    content = doc.getString("content") ?: "",
                    date = doc.getString("date") ?: "",
                    imageUrl = doc.getString("imageUrl") ?: ""
                )
            }
            if (newsList.isNotEmpty()) {
                db.newsDao().clearAllNews()
                db.newsDao().insertAll(newsList)
                pulledCount += newsList.size
            }

            // 6. Gallery
            val galSnap = firestore.collection("gallery").get().await()
            val galList = galSnap.documents.mapNotNull { doc ->
                val title = doc.getString("title") ?: return@mapNotNull null
                GalleryEntity(
                    title = title,
                    category = doc.getString("category") ?: "Kegiatan",
                    description = doc.getString("description") ?: "",
                    date = doc.getString("date") ?: "",
                    imageUrl = doc.getString("imageUrl") ?: ""
                )
            }
            if (galList.isNotEmpty()) {
                db.galleryDao().insertAll(galList)
                pulledCount += galList.size
            }

            // 7. Extracurriculars
            val extraSnap = firestore.collection("extracurriculars").get().await()
            val extraList = extraSnap.documents.mapNotNull { doc ->
                val name = doc.getString("name") ?: return@mapNotNull null
                ExtracurricularEntity(
                    name = name,
                    coach = doc.getString("coach") ?: "",
                    schedule = doc.getString("schedule") ?: "",
                    description = doc.getString("description") ?: "",
                    imageUrl = doc.getString("imageUrl") ?: ""
                )
            }
            if (extraList.isNotEmpty()) {
                db.extracurricularDao().insertAll(extraList)
                pulledCount += extraList.size
            }

            // 8. Programs
            val progSnap = firestore.collection("featured_programs").get().await()
            val progList = progSnap.documents.mapNotNull { doc ->
                val name = doc.getString("name") ?: return@mapNotNull null
                FeaturedProgramEntity(
                    name = name,
                    objective = doc.getString("objective") ?: "",
                    description = doc.getString("description") ?: "",
                    imageUrl = doc.getString("imageUrl") ?: ""
                )
            }
            if (progList.isNotEmpty()) {
                db.featuredProgramDao().insertAll(progList)
                pulledCount += progList.size
            }

            // 9. Agendas
            val agdSnap = firestore.collection("agendas").get().await()
            val agdList = agdSnap.documents.mapNotNull { doc ->
                val title = doc.getString("title") ?: return@mapNotNull null
                AgendaEntity(
                    title = title,
                    description = doc.getString("description") ?: "",
                    date = doc.getString("date") ?: "",
                    time = doc.getString("time") ?: "",
                    location = doc.getString("location") ?: "",
                    imageUrl = doc.getString("imageUrl") ?: ""
                )
            }
            if (agdList.isNotEmpty()) {
                db.agendaDao().insertAll(agdList)
                pulledCount += agdList.size
            }

            // 10. Notifications
            val notifSnap = firestore.collection("notifications").get().await()
            val notifList = notifSnap.documents.mapNotNull { doc ->
                val title = doc.getString("title") ?: return@mapNotNull null
                AppNotificationEntity(
                    title = title,
                    message = doc.getString("content") ?: doc.getString("message") ?: "",
                    dateFormatted = doc.getString("date") ?: "",
                    category = doc.getString("type") ?: "Pengumuman"
                )
            }
            if (notifList.isNotEmpty()) {
                db.appNotificationDao().insertAll(notifList)
                pulledCount += notifList.size
            }

            // 11. Testimonials
            val testiSnap = firestore.collection("testimonials").get().await()
            val testiList = testiSnap.documents.mapNotNull { doc ->
                val name = doc.getString("name") ?: return@mapNotNull null
                TestimonialEntity(
                    name = name,
                    role = doc.getString("role") ?: "Wali Murid",
                    content = doc.getString("content") ?: "",
                    rating = doc.getLong("rating")?.toInt() ?: 5,
                    isApproved = doc.getBoolean("isApproved") ?: false,
                    date = doc.getString("date") ?: ""
                )
            }
            if (testiList.isNotEmpty()) {
                db.testimonialDao().insertAll(testiList)
                pulledCount += testiList.size
            }

            // 12. RPP
            val rppSnap = firestore.collection("rpp").get().await()
            val rppList = rppSnap.documents.mapNotNull { doc ->
                val title = doc.getString("title") ?: return@mapNotNull null
                RppEntity(
                    title = title,
                    teacherName = doc.getString("teacherName") ?: "",
                    classLevel = doc.getLong("classLevel")?.toInt() ?: 1,
                    rombel = doc.getString("rombel") ?: "Semua",
                    subject = doc.getString("subject") ?: "",
                    uploadDate = doc.getString("uploadDate") ?: "",
                    fileName = doc.getString("fileName") ?: "",
                    fileSize = doc.getString("fileSize") ?: "1.2 MB",
                    fileType = doc.getString("fileType") ?: "PDF"
                )
            }
            if (rppList.isNotEmpty()) {
                db.rppDao().insertAll(rppList)
                pulledCount += rppList.size
            }

            // 13. Documents
            val docSnap = firestore.collection("documents").get().await()
            val docList = docSnap.documents.mapNotNull { doc ->
                val title = doc.getString("title") ?: return@mapNotNull null
                DocumentEntity(
                    title = title,
                    folderName = doc.getString("folderName") ?: "Umum",
                    classLevel = doc.getString("classLevel") ?: "Semua",
                    uploadDate = doc.getString("uploadDate") ?: "",
                    fileName = doc.getString("fileName") ?: "",
                    fileType = doc.getString("fileType") ?: "PDF",
                    fileSize = doc.getString("fileSize") ?: "500 KB"
                )
            }
            if (docList.isNotEmpty()) {
                db.documentDao().insertAll(docList)
                pulledCount += docList.size
            }

            // 14. Class Schedules
            val schSnap = firestore.collection("class_schedules").get().await()
            val schList = schSnap.documents.mapNotNull { doc ->
                val subject = doc.getString("subject") ?: return@mapNotNull null
                ClassScheduleEntity(
                    classLevel = doc.getLong("classLevel")?.toInt() ?: 1,
                    rombel = doc.getString("rombel") ?: "Ar Rahman",
                    day = doc.getString("day") ?: "Senin",
                    timeSlot = doc.getString("timeSlot") ?: "07.30 - 08.45 WIB",
                    subject = subject,
                    teacherName = doc.getString("teacherName") ?: "Ustadz / Ustadzah"
                )
            }
            if (schList.isNotEmpty()) {
                db.classScheduleDao().insertAll(schList)
                pulledCount += schList.size
            }

            val resultMsg = "Sinkronisasi Cloud Berhasil! Berhasil memuat $pulledCount data madrasah dari Cloud."
            _cloudSyncStatus.value = resultMsg
            Result.success(resultMsg)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, "force_pull_all")
            Result.failure(e)
        } finally {
            _isCloudSyncing.value = false
        }
    }
}
