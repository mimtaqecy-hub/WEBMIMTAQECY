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
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    private val _cloudSyncStatus = MutableStateFlow<String?>("Cloud Sync Siap (Firebase Firestore)")
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
     * Start real-time listeners for all public madrasah data.
     * When any device (Device 1, Web Admin, or other phone) updates data in Firestore,
     * this device (Device 2) automatically receives the update and caches it in local Room DB!
     */
    fun startRealtimeListeners() {
        stopRealtimeListeners()

        // 1. Listen to School Settings
        try {
            val settingsReg = firestore.collection("school_settings").document("general")
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
                                    principalName = snapshot.getString("principalName") ?: current.principalName,
                                    phone = snapshot.getString("phone") ?: current.phone,
                                    email = snapshot.getString("email") ?: current.email,
                                    address = snapshot.getString("address") ?: current.address,
                                    visionText = snapshot.getString("visionText") ?: current.visionText,
                                    missionText = snapshot.getString("missionText") ?: current.missionText,
                                    goalsText = snapshot.getString("goalsText") ?: current.goalsText,
                                    historyText = snapshot.getString("historyText") ?: current.historyText,
                                    facebookUrl = snapshot.getString("facebookUrl") ?: current.facebookUrl,
                                    instagramUrl = snapshot.getString("instagramUrl") ?: current.instagramUrl,
                                    tiktokUrl = snapshot.getString("tiktokUrl") ?: current.tiktokUrl
                                )
                                db.schoolSettingsDao().insertOrUpdate(updated)
                                _cloudSyncStatus.value = "Pengaturan madrasah tersinkronisasi dari Cloud"
                            } catch (e: Exception) {
                                Log.e(TAG, "Error caching settings from firestore", e)
                            }
                        }
                    }
                }
            listeners.add(settingsReg)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to listen to school_settings", e)
        }

        // 2. Listen to Students
        try {
            val studentsReg = firestore.collection("students")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        handleFirestoreError(error, OperationType.LIST, "students")
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        scope.launch(Dispatchers.IO) {
                            try {
                                val remoteStudents = snapshot.documents.mapNotNull { doc ->
                                    val name = doc.getString("fullName") ?: return@mapNotNull null
                                    val classLvl = doc.getLong("classLevel")?.toInt() ?: 1
                                    val rombel = doc.getString("rombel") ?: "Ar Rahman"
                                    StudentEntity(
                                        fullName = name,
                                        nisn = doc.getString("nisn") ?: doc.id,
                                        birthPlace = doc.getString("birthPlace") ?: "Lampung Timur",
                                        birthDate = doc.getString("birthDate") ?: "",
                                        classLevel = classLvl.coerceIn(1, 6),
                                        rombel = rombel,
                                        gender = doc.getString("gender") ?: "Laki-laki",
                                        address = doc.getString("address") ?: "Tanjung Qencono",
                                        isPublished = doc.getBoolean("isPublished") ?: true,
                                        cachedAt = System.currentTimeMillis()
                                    )
                                }
                                if (remoteStudents.isNotEmpty() || snapshot.isEmpty) {
                                    db.studentDao().clearAllStudents()
                                    if (remoteStudents.isNotEmpty()) {
                                        db.studentDao().insertAll(remoteStudents)
                                    }
                                    _cloudSyncStatus.value = "Data siswa tersinkronisasi: ${remoteStudents.size} siswa"
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "Error caching students from firestore", e)
                            }
                        }
                    }
                }
            listeners.add(studentsReg)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to listen to students", e)
        }

        // 3. Listen to Facilities
        try {
            val facilitiesReg = firestore.collection("facilities")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        handleFirestoreError(error, OperationType.LIST, "facilities")
                        return@addSnapshotListener
                    }
                    if (snapshot != null && !snapshot.isEmpty) {
                        scope.launch(Dispatchers.IO) {
                            try {
                                val remoteList = snapshot.documents.mapNotNull { doc ->
                                    val title = doc.getString("title") ?: doc.getString("name") ?: return@mapNotNull null
                                    FacilityEntity(
                                        name = title,
                                        category = doc.getString("category") ?: "Ruang Belajar",
                                        description = doc.getString("description") ?: "",
                                        condition = doc.getString("condition") ?: "Sangat Baik"
                                    )
                                }
                                if (remoteList.isNotEmpty()) {
                                    db.facilityDao().clearAll()
                                    db.facilityDao().insertAll(remoteList)
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "Error caching facilities", e)
                            }
                        }
                    }
                }
            listeners.add(facilitiesReg)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to listen to facilities", e)
        }

        // 4. Listen to Notifications
        try {
            val notifReg = firestore.collection("notifications")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        handleFirestoreError(error, OperationType.LIST, "notifications")
                        return@addSnapshotListener
                    }
                    if (snapshot != null && !snapshot.isEmpty) {
                        scope.launch(Dispatchers.IO) {
                            try {
                                val remoteNotifs = snapshot.documents.mapNotNull { doc ->
                                    val title = doc.getString("title") ?: return@mapNotNull null
                                    val content = doc.getString("content") ?: ""
                                    val date = doc.getString("date") ?: ""
                                    NotificationEntity(
                                        title = title,
                                        content = content,
                                        date = date,
                                        type = doc.getString("type") ?: "INFO"
                                    )
                                }
                                if (remoteNotifs.isNotEmpty()) {
                                    db.notificationDao().clearAll()
                                    db.notificationDao().insertAll(remoteNotifs)
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "Error caching notifications", e)
                            }
                        }
                    }
                }
            listeners.add(notifReg)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to listen to notifications", e)
        }
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
                _cloudSyncStatus.value = "Terhubung sebagai: ${user.displayName ?: user.email}"
                Result.success(user)
            } else {
                Result.failure(Exception("Kredensial tidak dikenali"))
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

    /**
     * Push School Settings to Firestore (Multi-Device Live Sync).
     */
    suspend fun pushSchoolSettings(settings: SchoolSettingsEntity): Result<Unit> {
        return try {
            _isCloudSyncing.value = true
            val data = hashMapOf(
                "schoolName" to settings.schoolName,
                "npsn" to settings.npsn,
                "nsm" to settings.nsm,
                "accreditation" to settings.accreditation,
                "principalName" to settings.principalName,
                "phone" to settings.phone,
                "email" to settings.email,
                "address" to settings.address,
                "visionText" to settings.visionText,
                "missionText" to settings.missionText,
                "goalsText" to settings.goalsText,
                "historyText" to settings.historyText,
                "facebookUrl" to settings.facebookUrl,
                "instagramUrl" to settings.instagramUrl,
                "tiktokUrl" to settings.tiktokUrl,
                "updatedAt" to java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss", java.util.Locale("id", "ID")).format(java.util.Date())
            )
            firestore.collection("school_settings").document("general")
                .set(data, SetOptions.merge())
                .await()
            _cloudSyncStatus.value = "Pengaturan madrasah berhasil disimpan ke Cloud!"
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "school_settings/general")
            Result.failure(e)
        } finally {
            _isCloudSyncing.value = false
        }
    }

    /**
     * Push single student to Firestore.
     */
    suspend fun pushStudent(student: StudentEntity): Result<Unit> {
        return try {
            _isCloudSyncing.value = true
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
                "isPublished" to student.isPublished,
                "updatedAt" to java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss", java.util.Locale("id", "ID")).format(java.util.Date())
            )
            firestore.collection("students").document(docId).set(data).await()
            _cloudSyncStatus.value = "Siswa ${student.fullName} tersimpan di Cloud"
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "students")
            Result.failure(e)
        } finally {
            _isCloudSyncing.value = false
        }
    }

    /**
     * Push all local students to Firestore (Cloud sync batch).
     */
    suspend fun pushAllStudents(students: List<StudentEntity>): Result<Int> {
        if (students.isEmpty()) return Result.success(0)
        return try {
            _isCloudSyncing.value = true
            var count = 0
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
                count++
            }
            batch.commit().await()
            _cloudSyncStatus.value = "Berhasil mengirim $count siswa ke Cloud Firestore!"
            Result.success(count)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "students")
            Result.failure(e)
        } finally {
            _isCloudSyncing.value = false
        }
    }

    /**
     * Delete student from Firestore.
     */
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

    /**
     * Delete all students from Firestore.
     */
    suspend fun deleteAllStudents(): Result<Unit> {
        return try {
            _isCloudSyncing.value = true
            val snapshot = firestore.collection("students").get().await()
            val batch = firestore.batch()
            snapshot.documents.forEach { doc ->
                batch.delete(doc.reference)
            }
            batch.commit().await()
            _cloudSyncStatus.value = "Seluruh data siswa di Cloud berhasil dihapus!"
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, "students")
            Result.failure(e)
        } finally {
            _isCloudSyncing.value = false
        }
    }

    /**
     * Submit PPDB to Firestore.
     */
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

    /**
     * Push Facility to Firestore.
     */
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

    /**
     * Pull all data manually from Firestore immediately.
     */
    suspend fun forcePullFromCloud(): Result<String> {
        return try {
            _isCloudSyncing.value = true
            // 1. Pull Settings
            val settingsDoc = firestore.collection("school_settings").document("general").get().await()
            if (settingsDoc.exists()) {
                val current = db.schoolSettingsDao().getSettingsOnce() ?: SchoolSettingsEntity()
                val updated = current.copy(
                    schoolName = settingsDoc.getString("schoolName") ?: current.schoolName,
                    npsn = settingsDoc.getString("npsn") ?: current.npsn,
                    nsm = settingsDoc.getString("nsm") ?: current.nsm,
                    accreditation = settingsDoc.getString("accreditation") ?: current.accreditation,
                    principalName = settingsDoc.getString("principalName") ?: current.principalName,
                    phone = settingsDoc.getString("phone") ?: current.phone,
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
            }

            // 2. Pull Students
            val studentsSnapshot = firestore.collection("students").get().await()
            val list = studentsSnapshot.documents.mapNotNull { doc ->
                val name = doc.getString("fullName") ?: return@mapNotNull null
                val classLvl = doc.getLong("classLevel")?.toInt() ?: 1
                val rombel = doc.getString("rombel") ?: "Ar Rahman"
                StudentEntity(
                    fullName = name,
                    nisn = doc.getString("nisn") ?: doc.id,
                    birthPlace = doc.getString("birthPlace") ?: "Lampung Timur",
                    birthDate = doc.getString("birthDate") ?: "",
                    classLevel = classLvl.coerceIn(1, 6),
                    rombel = rombel,
                    gender = doc.getString("gender") ?: "Laki-laki",
                    address = doc.getString("address") ?: "Tanjung Qencono",
                    isPublished = doc.getBoolean("isPublished") ?: true,
                    cachedAt = System.currentTimeMillis()
                )
            }
            if (list.isNotEmpty()) {
                db.studentDao().clearAllStudents()
                db.studentDao().insertAll(list)
            }

            val msg = "Sinkronisasi Cloud Berhasil! Mengambil ${list.size} data siswa & profil madrasah."
            _cloudSyncStatus.value = msg
            Result.success(msg)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, "cloud_pull")
            Result.failure(e)
        } finally {
            _isCloudSyncing.value = false
        }
    }
}
