package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SchoolSettingsDao {
    @Query("SELECT * FROM school_settings WHERE id = 1 LIMIT 1")
    fun getSettings(): Flow<SchoolSettingsEntity?>

    @Query("SELECT * FROM school_settings WHERE id = 1 LIMIT 1")
    suspend fun getSettingsOnce(): SchoolSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(settings: SchoolSettingsEntity)
}

@Dao
interface StudentDao {
    @Query("SELECT * FROM students ORDER BY classLevel ASC, rombel ASC, fullName ASC")
    fun getAllStudents(): Flow<List<StudentEntity>>

    @Query("SELECT * FROM students WHERE isPublished = 1 ORDER BY classLevel ASC, rombel ASC, fullName ASC")
    fun getPublishedStudents(): Flow<List<StudentEntity>>

    @Query("SELECT * FROM students WHERE (:classLevel = 0 OR classLevel = :classLevel) AND (:rombel = '' OR rombel = :rombel) AND (fullName LIKE '%' || :query || '%' OR nisn LIKE '%' || :query || '%') ORDER BY classLevel ASC, rombel ASC, fullName ASC")
    fun filterStudents(classLevel: Int, rombel: String, query: String): Flow<List<StudentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(student: StudentEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(students: List<StudentEntity>)

    @Update
    suspend fun update(student: StudentEntity)

    @Delete
    suspend fun delete(student: StudentEntity)

    @Query("DELETE FROM students WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM students WHERE id = :id LIMIT 1")
    fun getStudentById(id: Long): Flow<StudentEntity?>

    @Query("DELETE FROM students")
    suspend fun clearAllStudents()

    @Query("SELECT MAX(cachedAt) FROM students")
    suspend fun getLastCachedTimestamp(): Long?

    @Query("SELECT COUNT(*) FROM students")
    fun getCount(): Flow<Int>
}

@Dao
interface TeacherDao {
    @Query("SELECT * FROM teachers ORDER BY role DESC, name ASC")
    fun getAllTeachers(): Flow<List<TeacherEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(teacher: TeacherEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(teachers: List<TeacherEntity>)

    @Update
    suspend fun update(teacher: TeacherEntity)

    @Delete
    suspend fun delete(teacher: TeacherEntity)

    @Query("SELECT * FROM teachers WHERE id = :id LIMIT 1")
    fun getTeacherById(id: Long): Flow<TeacherEntity?>

    @Query("DELETE FROM teachers")
    suspend fun clearAllTeachers()

    @Query("SELECT MAX(cachedAt) FROM teachers")
    suspend fun getLastCachedTimestamp(): Long?

    @Query("SELECT COUNT(*) FROM teachers")
    fun getCount(): Flow<Int>
}

@Dao
interface PpdbDao {
    @Query("SELECT * FROM ppdb ORDER BY id DESC")
    fun getAllPpdb(): Flow<List<PpdbEntity>>

    @Query("SELECT * FROM ppdb WHERE id = :id LIMIT 1")
    suspend fun getPpdbById(id: Long): PpdbEntity?

    @Query("SELECT * FROM ppdb WHERE regNumber = :regNumber LIMIT 1")
    suspend fun getPpdbByRegNumber(regNumber: String): PpdbEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(ppdb: PpdbEntity): Long

    @Update
    suspend fun update(ppdb: PpdbEntity)

    @Delete
    suspend fun delete(ppdb: PpdbEntity)

    @Query("SELECT COUNT(*) FROM ppdb")
    fun getCount(): Flow<Int>
}

@Dao
interface RppDao {
    @Query("SELECT * FROM rpp ORDER BY id DESC")
    fun getAllRpp(): Flow<List<RppEntity>>

    @Query("SELECT * FROM rpp WHERE (:classLevel = 0 OR classLevel = :classLevel) AND (:rombel = '' OR rombel = :rombel) AND (title LIKE '%' || :query || '%' OR subject LIKE '%' || :query || '%' OR teacherName LIKE '%' || :query || '%') ORDER BY id DESC")
    fun filterRpp(classLevel: Int, rombel: String, query: String): Flow<List<RppEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(rpp: RppEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(rppList: List<RppEntity>)

    @Update
    suspend fun update(rpp: RppEntity)

    @Delete
    suspend fun delete(rpp: RppEntity)

    @Query("SELECT COUNT(*) FROM rpp")
    fun getCount(): Flow<Int>
}

@Dao
interface DocumentDao {
    @Query("SELECT * FROM documents ORDER BY id DESC")
    fun getAllDocuments(): Flow<List<DocumentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(document: DocumentEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(docs: List<DocumentEntity>)

    @Update
    suspend fun update(document: DocumentEntity)

    @Delete
    suspend fun delete(document: DocumentEntity)
}

@Dao
interface NewsDao {
    @Query("SELECT * FROM news ORDER BY id DESC")
    fun getAllNews(): Flow<List<NewsEntity>>

    @Query("SELECT * FROM news WHERE isPublished = 1 ORDER BY id DESC")
    fun getPublishedNews(): Flow<List<NewsEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(news: NewsEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(newsList: List<NewsEntity>)

    @Update
    suspend fun update(news: NewsEntity)

    @Delete
    suspend fun delete(news: NewsEntity)

    @Query("SELECT * FROM news WHERE id = :id LIMIT 1")
    fun getNewsById(id: Long): Flow<NewsEntity?>

    @Query("DELETE FROM news")
    suspend fun clearAllNews()

    @Query("SELECT MAX(cachedAt) FROM news")
    suspend fun getLastCachedTimestamp(): Long?

    @Query("SELECT COUNT(*) FROM news")
    fun getCount(): Flow<Int>
}

@Dao
interface GalleryDao {
    @Query("SELECT * FROM gallery ORDER BY id DESC")
    fun getAllGallery(): Flow<List<GalleryEntity>>

    @Query("SELECT * FROM gallery WHERE isPublished = 1 ORDER BY id DESC")
    fun getPublishedGallery(): Flow<List<GalleryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(gallery: GalleryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(galleries: List<GalleryEntity>)

    @Update
    suspend fun update(gallery: GalleryEntity)

    @Delete
    suspend fun delete(gallery: GalleryEntity)

    @Query("SELECT COUNT(*) FROM gallery")
    fun getCount(): Flow<Int>
}

@Dao
interface ExtracurricularDao {
    @Query("SELECT * FROM extracurriculars ORDER BY id ASC")
    fun getAll(): Flow<List<ExtracurricularEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(extra: ExtracurricularEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(extras: List<ExtracurricularEntity>)

    @Update
    suspend fun update(extra: ExtracurricularEntity)

    @Delete
    suspend fun delete(extra: ExtracurricularEntity)
}

@Dao
interface FeaturedProgramDao {
    @Query("SELECT * FROM featured_programs ORDER BY id ASC")
    fun getAll(): Flow<List<FeaturedProgramEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(program: FeaturedProgramEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(programs: List<FeaturedProgramEntity>)

    @Update
    suspend fun update(program: FeaturedProgramEntity)

    @Delete
    suspend fun delete(program: FeaturedProgramEntity)
}

@Dao
interface AgendaDao {
    @Query("SELECT * FROM agendas ORDER BY id DESC")
    fun getAll(): Flow<List<AgendaEntity>>

    @Query("SELECT * FROM agendas WHERE isPublished = 1 ORDER BY id DESC")
    fun getPublished(): Flow<List<AgendaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(agenda: AgendaEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(agendas: List<AgendaEntity>)

    @Update
    suspend fun update(agenda: AgendaEntity)

    @Delete
    suspend fun delete(agenda: AgendaEntity)
}

@Dao
interface TestimonialDao {
    @Query("SELECT * FROM testimonials ORDER BY id DESC")
    fun getAll(): Flow<List<TestimonialEntity>>

    @Query("SELECT * FROM testimonials WHERE isApproved = 1 ORDER BY id DESC")
    fun getApproved(): Flow<List<TestimonialEntity>>

    @Query("SELECT COUNT(*) FROM testimonials WHERE isApproved = 0")
    fun getPendingCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(testi: TestimonialEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(testis: List<TestimonialEntity>)

    @Update
    suspend fun update(testi: TestimonialEntity)

    @Delete
    suspend fun delete(testi: TestimonialEntity)
}

@Dao
interface ContactMessageDao {
    @Query("SELECT * FROM contact_messages ORDER BY id DESC")
    fun getAll(): Flow<List<ContactMessageEntity>>

    @Query("SELECT COUNT(*) FROM contact_messages WHERE isRead = 0")
    fun getUnreadCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(msg: ContactMessageEntity): Long

    @Update
    suspend fun update(msg: ContactMessageEntity)

    @Delete
    suspend fun delete(msg: ContactMessageEntity)
}

@Dao
interface AdminLogDao {
    @Query("SELECT * FROM admin_logs ORDER BY id DESC LIMIT 100")
    fun getRecentLogs(): Flow<List<AdminLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: AdminLogEntity): Long
}

@Dao
interface ClassScheduleDao {
    @Query("SELECT * FROM class_schedules WHERE (:classLevel = 0 OR classLevel = :classLevel) AND (:rombel = '' OR rombel = :rombel) ORDER BY classLevel ASC, rombel ASC, id ASC")
    fun getSchedules(classLevel: Int, rombel: String): Flow<List<ClassScheduleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(schedule: ClassScheduleEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(schedules: List<ClassScheduleEntity>)

    @Delete
    suspend fun delete(schedule: ClassScheduleEntity)
}

@Dao
interface FacilityDao {
    @Query("SELECT * FROM facilities ORDER BY id ASC")
    fun getAllFacilities(): Flow<List<FacilityEntity>>

    @Query("SELECT * FROM facilities WHERE isPublished = 1 ORDER BY id ASC")
    fun getPublishedFacilities(): Flow<List<FacilityEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(facility: FacilityEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(facilities: List<FacilityEntity>)

    @Update
    suspend fun update(facility: FacilityEntity)

    @Delete
    suspend fun delete(facility: FacilityEntity)
}

@Dao
interface AppNotificationDao {
    @Query("SELECT * FROM app_notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<AppNotificationEntity>>

    @Query("SELECT COUNT(*) FROM app_notifications WHERE isRead = 0")
    fun getUnreadCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(notification: AppNotificationEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(notifications: List<AppNotificationEntity>)

    @Query("UPDATE app_notifications SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: Long)

    @Query("UPDATE app_notifications SET isRead = 1")
    suspend fun markAllAsRead()

    @Query("DELETE FROM app_notifications WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Delete
    suspend fun delete(notification: AppNotificationEntity)
}

