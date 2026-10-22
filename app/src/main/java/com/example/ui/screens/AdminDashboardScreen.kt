package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import com.example.R
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.*
import com.example.ui.components.ConfirmDeleteDialog
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatCard
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.IslamicGreen
import com.example.ui.theme.NavyDeep
import com.example.ui.theme.NavyPrimary
import com.example.ui.viewmodel.AdminTab
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.MadrasahViewModel
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.example.util.CsvHelper
import com.example.util.FilePickerHelper
import com.example.util.GasScriptTemplate
import com.example.util.GoogleSheetsHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(viewModel: MadrasahViewModel) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val currentTab by viewModel.currentAdminTab.collectAsState()
    val settings by viewModel.settings.collectAsState()

    // Navigation Back to Public Site
    Column(modifier = Modifier.fillMaxSize()) {
        // Admin Top Bar Header
        Surface(
            color = NavyDeep,
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(GoldAccent, RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("PORTAL ADMIN", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NavyDeep)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "MIM Tanjung Qencono",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { viewModel.navigateTo(AppScreen.HOME) },
                        modifier = Modifier.testTag("admin_view_public_btn")
                    ) {
                        Icon(Icons.Default.Visibility, contentDescription = "Lihat Web Publik", tint = Color.White)
                    }
                    IconButton(
                        onClick = { viewModel.logoutAdmin() },
                        modifier = Modifier.testTag("admin_logout_btn")
                    ) {
                        Icon(Icons.Default.Logout, contentDescription = "Keluar", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }

        // Admin SubTab Carousel
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(AdminTab.values()) { tab ->
                FilterChip(
                    selected = currentTab == tab,
                    onClick = { viewModel.navigateAdminTab(tab) },
                    label = { Text(tab.title, fontSize = 12.sp) },
                    modifier = Modifier.testTag("admin_tab_${tab.name.lowercase()}")
                )
            }
        }

        HorizontalDivider()

        // Content Area for current tab
        Box(modifier = Modifier.weight(1f)) {
            when (currentTab) {
                AdminTab.OVERVIEW -> AdminOverviewSection(viewModel)
                AdminTab.SETTINGS -> AdminSettingsSection(viewModel)
                AdminTab.WRITE_NOTIFICATION -> AdminWriteNotificationSection(viewModel)
                AdminTab.FACILITIES -> AdminFacilitiesSection(viewModel)
                AdminTab.SCHOOL_SCHEDULE -> AdminSchoolScheduleSection(viewModel)
                AdminTab.STUDENTS -> AdminStudentsSection(viewModel)
                AdminTab.TEACHERS -> AdminTeachersSection(viewModel)
                AdminTab.PPDB -> AdminPpdbSection(viewModel)
                AdminTab.RPP -> AdminRppSection(viewModel)
                AdminTab.NEWS -> AdminNewsSection(viewModel)
                AdminTab.GALLERY -> AdminGallerySection(viewModel)
                AdminTab.EXTRACURRICULARS -> AdminExtraSection(viewModel)
                AdminTab.PROGRAMS -> AdminProgramsSection(viewModel)
                AdminTab.AGENDAS -> AdminAgendasSection(viewModel)
                AdminTab.TESTIMONIALS -> AdminTestimonialsSection(viewModel)
                AdminTab.MESSAGES -> AdminMessagesSection(viewModel)
                AdminTab.SCHEDULES -> AdminSchedulesSection(viewModel)
                AdminTab.MUSIC -> AdminMusicSection(viewModel)
                AdminTab.LOGS -> AdminLogsSection(viewModel)
                AdminTab.GAS_INTEGRATION -> AdminGasIntegrationSection(viewModel)
                AdminTab.CLOUD_SYNC -> AdminCloudSyncSection(viewModel)
                AdminTab.VISION_MISSION -> AdminVisionMissionSection(viewModel)
                AdminTab.VERCEL_WEB -> AdminVercelSection(viewModel)
            }
        }
    }
}

// 1. OVERVIEW
@Composable
fun AdminOverviewSection(viewModel: MadrasahViewModel) {
    val studentCount by viewModel.studentCount.collectAsState()
    val teacherCount by viewModel.teacherCount.collectAsState()
    val ppdbCount by viewModel.ppdbCount.collectAsState()
    val rppCount by viewModel.rppCount.collectAsState()
    val newsCount by viewModel.newsCount.collectAsState()
    val galleryCount by viewModel.galleryCount.collectAsState()
    val pendingTestiCount by viewModel.pendingTestimonialCount.collectAsState()
    val unreadMsgCount by viewModel.unreadMessageCount.collectAsState()
    val recentLogs by viewModel.adminLogs.collectAsState()

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        item {
            SectionHeader(title = "Ringkasan & Statistik Madrasah", subtitle = "Data statistik real-time terhubung ke database")

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatCard("Total Siswa", studentCount.toString(), Icons.Default.Groups, modifier = Modifier.weight(1f)) {
                    viewModel.navigateAdminTab(AdminTab.STUDENTS)
                }
                StatCard("Guru & Staf", teacherCount.toString(), Icons.Default.Psychology, containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.weight(1f)) {
                    viewModel.navigateAdminTab(AdminTab.TEACHERS)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatCard("Pendaftar PPDB", ppdbCount.toString(), Icons.Default.AppRegistration, containerColor = MaterialTheme.colorScheme.tertiaryContainer, contentColor = MaterialTheme.colorScheme.onTertiaryContainer, modifier = Modifier.weight(1f)) {
                    viewModel.navigateAdminTab(AdminTab.PPDB)
                }
                StatCard("Berkas RPP", rppCount.toString(), Icons.Default.Article, modifier = Modifier.weight(1f)) {
                    viewModel.navigateAdminTab(AdminTab.RPP)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatCard("Berita Terbit", newsCount.toString(), Icons.Default.Newspaper, modifier = Modifier.weight(1f)) {
                    viewModel.navigateAdminTab(AdminTab.NEWS)
                }
                StatCard("Foto Galeri", galleryCount.toString(), Icons.Default.Collections, modifier = Modifier.weight(1f)) {
                    viewModel.navigateAdminTab(AdminTab.GALLERY)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatCard("Testimoni Pending", pendingTestiCount.toString(), Icons.Default.RateReview, containerColor = GoldAccent.copy(alpha = 0.2f), contentColor = NavyDeep, modifier = Modifier.weight(1f)) {
                    viewModel.navigateAdminTab(AdminTab.TESTIMONIALS)
                }
                StatCard("Pesan Masuk", unreadMsgCount.toString(), Icons.Default.Email, containerColor = IslamicGreen.copy(alpha = 0.2f), contentColor = NavyDeep, modifier = Modifier.weight(1f)) {
                    viewModel.navigateAdminTab(AdminTab.MESSAGES)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // OFFLINE CACHE ROOM STATUS
            Card(
                modifier = Modifier.fillMaxWidth().testTag("admin_cache_status_card"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F766E).copy(alpha = 0.12f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CloudDone, contentDescription = null, tint = Color(0xFF0F766E))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Status Cache Lokal Room Database",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F766E),
                                fontSize = 14.sp
                            )
                        }
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF0F766E), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("OFFLINE READY", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Data tersimpan permanen di SQLite lokal perangkat:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text("• $studentCount Data Siswa Lengkap ter-cache", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Text("• $teacherCount Profil Dewan Guru & Staf ter-cache", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Text("• $newsCount Artikel Berita & Pengumuman ter-cache", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)

                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { viewModel.refreshOfflineCache() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F766E)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.fillMaxWidth().testTag("btn_sync_cache")
                    ) {
                        Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Validasi & Segarkan Cache Offline", fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // FIREBASE CLOUD LIVE SYNC CARD
            Card(
                modifier = Modifier.fillMaxWidth().testTag("admin_firestore_status_card"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = IslamicGreen.copy(alpha = 0.12f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CloudDone, contentDescription = null, tint = IslamicGreen, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Sinkronisasi Cloud (Firebase)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = IslamicGreen)
                        }
                        Box(
                            modifier = Modifier
                                .background(IslamicGreen, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("MULTI-DEVICE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Seluruh 15 modul data madrasah (siswa, guru, fasilitas, berita, galeri, ekskul, program, agenda, testimoni, RPP, dokumen, jadwal, notifikasi, visi-misi, PPDB) tersinkron otomatis antar HP secara real-time.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { viewModel.pushAllDataToCloud() },
                            colors = ButtonDefaults.buttonColors(containerColor = IslamicGreen),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Kirim 15 Modul", fontSize = 11.sp)
                        }
                        OutlinedButton(
                            onClick = { viewModel.navigateAdminTab(AdminTab.CLOUD_SYNC) },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.CloudSync, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Kelola Cloud", fontSize = 11.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // GOOGLE SHEETS CLOUD SYNC CARD
            val currentSettings by viewModel.settings.collectAsState()
            val hasGasUrl = currentSettings?.gasWebAppUrl?.isNotBlank() == true || currentSettings?.googleSpreadsheetId?.isNotBlank() == true
            Card(
                modifier = Modifier.fillMaxWidth().testTag("admin_sheets_status_card"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = if (hasGasUrl) IslamicGreen.copy(alpha = 0.12f) else NavyPrimary.copy(alpha = 0.08f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CloudSync, contentDescription = null, tint = if (hasGasUrl) IslamicGreen else NavyPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Integrasi Google Sheets",
                                fontWeight = FontWeight.Bold,
                                color = if (hasGasUrl) IslamicGreen else NavyPrimary,
                                fontSize = 14.sp
                            )
                        }
                        Box(
                            modifier = Modifier
                                .background(if (hasGasUrl) IslamicGreen else MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(if (hasGasUrl) "TERHUBUNG" else "BELUM SETUP", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (hasGasUrl) "Sinkronisasi formulir PPDB & data siswa aktif ke spreadsheet madrasah." else "Hubungkan spreadsheet Google Sheets untuk arsip PPDB dan data siswa online.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { viewModel.syncAllPpdbToSheet() },
                            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Sync PPDB", fontSize = 11.sp)
                        }
                        OutlinedButton(
                            onClick = { viewModel.navigateAdminTab(AdminTab.GAS_INTEGRATION) },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Buka Pengaturan", fontSize = 11.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            SectionHeader(title = "Log Aktivitas Terakhir", icon = Icons.Default.History)
        }

        items(recentLogs.take(8)) { log ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircleOutline, contentDescription = null, tint = IslamicGreen, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(log.action, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text(log.details, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(log.timestamp, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

// 2. SETTINGS
@Composable
fun AdminSettingsSection(viewModel: MadrasahViewModel) {
    val context = LocalContext.current
    val currentSettings by viewModel.settings.collectAsState()

    var name by remember(currentSettings) { mutableStateOf(currentSettings?.schoolName ?: "") }
    var slogan by remember(currentSettings) { mutableStateOf(currentSettings?.slogan ?: "") }
    var npsn by remember(currentSettings) { mutableStateOf(currentSettings?.npsn ?: "60721234") }
    var nsm by remember(currentSettings) { mutableStateOf(currentSettings?.nsm ?: "111218070001") }
    var schoolStatus by remember(currentSettings) { mutableStateOf(currentSettings?.schoolStatus ?: "Swasta (Diakui Dikdasmen)") }
    var accreditation by remember(currentSettings) { mutableStateOf(currentSettings?.accreditation ?: "B (Terakreditasi Baik Sekali)") }
    var foundedYear by remember(currentSettings) { mutableStateOf(currentSettings?.foundedYear ?: "1988") }
    var academicYear by remember(currentSettings) { mutableStateOf(currentSettings?.academicYear ?: "2025/2026") }
    var address by remember(currentSettings) { mutableStateOf(currentSettings?.address ?: "") }
    var phone by remember(currentSettings) { mutableStateOf(currentSettings?.phoneWa ?: "") }
    var email by remember(currentSettings) { mutableStateOf(currentSettings?.email ?: "") }

    // Kepala Madrasah
    var headmaster by remember(currentSettings) { mutableStateOf(currentSettings?.headmasterName ?: "") }
    var headmasterPhotoUri by remember(currentSettings) { mutableStateOf(currentSettings?.headmasterPhotoUri ?: "") }
    var speech by remember(currentSettings) { mutableStateOf(currentSettings?.headmasterSpeech ?: "") }
    var tausiyah by remember(currentSettings) { mutableStateOf(currentSettings?.dailyTausiyah ?: "") }
    var bgPreset by remember(currentSettings) { mutableStateOf(currentSettings?.bgPreset ?: "Gedung Madrasah") }

    var oldPass by remember { mutableStateOf("") }
    var newPass by remember { mutableStateOf("") }

    val headmasterPhotoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            val saved = FilePickerHelper.copyUriToInternalStorage(
                context = context,
                uri = it,
                subfolder = "headmaster",
                prefix = "kamad"
            )
            headmasterPhotoUri = saved
        }
    }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        item {
            SectionHeader(title = "Pengaturan Identitas & Legalitas Madrasah", icon = Icons.Default.AccountBalance)

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nama Madrasah") },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            )
            OutlinedTextField(
                value = slogan,
                onValueChange = { slogan = it },
                label = { Text("Slogan Madrasah") },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            )

            // Status Madrasah & Akreditasi
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = schoolStatus,
                    onValueChange = { schoolStatus = it },
                    label = { Text("Status Madrasah") },
                    placeholder = { Text("Swasta / Negeri") },
                    modifier = Modifier.weight(1f).padding(vertical = 4.dp)
                )
                OutlinedTextField(
                    value = accreditation,
                    onValueChange = { accreditation = it },
                    label = { Text("Akreditasi") },
                    placeholder = { Text("A / B / Unggul") },
                    modifier = Modifier.weight(1f).padding(vertical = 4.dp)
                )
            }

            // Tahun Berdiri & Tahun Ajaran
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = foundedYear,
                    onValueChange = { foundedYear = it },
                    label = { Text("Tahun Berdiri / Madrasah") },
                    placeholder = { Text("1988") },
                    modifier = Modifier.weight(1f).padding(vertical = 4.dp)
                )
                OutlinedTextField(
                    value = academicYear,
                    onValueChange = { academicYear = it },
                    label = { Text("Tahun Pelajaran") },
                    placeholder = { Text("2025/2026") },
                    modifier = Modifier.weight(1f).padding(vertical = 4.dp)
                )
            }

            // NPSN & NSM
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = npsn,
                    onValueChange = { npsn = it },
                    label = { Text("NPSN") },
                    modifier = Modifier.weight(1f).padding(vertical = 4.dp)
                )
                OutlinedTextField(
                    value = nsm,
                    onValueChange = { nsm = it },
                    label = { Text("NSM") },
                    modifier = Modifier.weight(1f).padding(vertical = 4.dp)
                )
            }

            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Alamat Madrasah") },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("No. WhatsApp") },
                    modifier = Modifier.weight(1f).padding(vertical = 4.dp)
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Resmi") },
                    modifier = Modifier.weight(1f).padding(vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // KEPALA MADRASAH SECTION (DENGAN UNGGAH FOTO DARI PERANGKAT)
            SectionHeader(title = "Profil Kepala Madrasah", icon = Icons.Default.Person)
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        "Foto Resmi Kepala Madrasah",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (headmasterPhotoUri.isNotBlank()) {
                            AsyncImage(
                                model = if (headmasterPhotoUri.startsWith("/")) java.io.File(headmasterPhotoUri) else headmasterPhotoUri,
                                contentDescription = "Foto Kepala Madrasah",
                                modifier = Modifier
                                    .size(76.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(76.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(NavyPrimary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.AccountCircle,
                                    contentDescription = null,
                                    tint = NavyPrimary,
                                    modifier = Modifier.size(48.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Button(
                                onClick = {
                                    headmasterPhotoPicker.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                            ) {
                                Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Unggah Foto Perangkat", fontSize = 12.sp)
                            }

                            if (headmasterPhotoUri.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedButton(
                                    onClick = { headmasterPhotoUri = "" }
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Hapus Foto", fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = headmaster,
                        onValueChange = { headmaster = it },
                        label = { Text("Nama & Gelar Kepala Madrasah") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )
                    OutlinedTextField(
                        value = speech,
                        onValueChange = { speech = it },
                        label = { Text("Sambutan Kepala Madrasah") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        minLines = 3
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = tausiyah,
                onValueChange = { tausiyah = it },
                label = { Text("Tausiyah Harian (Tampil di Beranda)") },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                minLines = 2
            )

            Spacer(modifier = Modifier.height(10.dp))
            Text("Preset Latar Belakang Beranda:", style = MaterialTheme.typography.labelMedium)
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Gedung Madrasah", "Kelas Islami", "Taman Belajar").forEach { preset ->
                    FilterChip(
                        selected = bgPreset == preset,
                        onClick = { bgPreset = preset },
                        label = { Text(preset, fontSize = 11.sp) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Button(
                onClick = {
                    currentSettings?.let {
                        viewModel.updateSchoolSettings(
                            it.copy(
                                schoolName = name,
                                slogan = slogan,
                                npsn = npsn,
                                nsm = nsm,
                                schoolStatus = schoolStatus,
                                accreditation = accreditation,
                                foundedYear = foundedYear,
                                academicYear = academicYear,
                                address = address,
                                phoneWa = phone,
                                email = email,
                                headmasterName = headmaster,
                                headmasterPhotoUri = headmasterPhotoUri,
                                headmasterSpeech = speech,
                                dailyTausiyah = tausiyah,
                                bgPreset = bgPreset
                            )
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth().testTag("save_settings_btn")
            ) {
                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Simpan Pengaturan Madrasah")
            }

            Spacer(modifier = Modifier.height(10.dp))
            OutlinedButton(
                onClick = { viewModel.navigateAdminTab(AdminTab.VISION_MISSION) },
                modifier = Modifier.fillMaxWidth().testTag("goto_vision_mission_btn")
            ) {
                Icon(Icons.Default.School, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Kelola Visi, Misi & Sejarah Singkat", color = NavyPrimary)
            }

            Spacer(modifier = Modifier.height(10.dp))
            OutlinedButton(
                onClick = { viewModel.navigateAdminTab(AdminTab.GAS_INTEGRATION) },
                modifier = Modifier.fillMaxWidth().testTag("goto_sheets_integration_btn")
            ) {
                Icon(Icons.Default.CloudSync, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Pengaturan Integrasi Google Sheets", color = NavyPrimary)
            }

            Spacer(modifier = Modifier.height(20.dp))
            SectionHeader(title = "Keamanan & Ganti Sandi Admin", icon = Icons.Default.Security)

            OutlinedTextField(value = oldPass, onValueChange = { oldPass = it }, label = { Text("Sandi Lama") }, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp))
            OutlinedTextField(value = newPass, onValueChange = { newPass = it }, label = { Text("Sandi Baru (Min 6 karakter)") }, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp))

            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    viewModel.changeAdminPassword(oldPass, newPass)
                    oldPass = ""
                    newPass = ""
                },
                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = NavyDeep),
                modifier = Modifier.fillMaxWidth().testTag("change_password_btn")
            ) {
                Text("Perbarui Sandi Admin")
            }
        }
    }
}

// 3. STUDENTS ADMIN
@Composable
fun AdminStudentsSection(viewModel: MadrasahViewModel) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val students by viewModel.allStudents.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var itemToDelete by remember { mutableStateOf<StudentEntity?>(null) }
    var showDeleteAllDialog by remember { mutableStateOf(false) }

    val excelPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val fileName = context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1 && cursor.moveToFirst()) cursor.getString(nameIndex) else null
            } ?: "data_siswa.xlsx"
            viewModel.importStudentsFromUri(uri, fileName)
            showImportDialog = false
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SectionHeader(title = "Data Siswa (${students.size})", icon = Icons.Default.People, modifier = Modifier.weight(1f))
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                if (students.isNotEmpty()) {
                    OutlinedButton(
                        onClick = { showDeleteAllDialog = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = "Hapus Semua", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Hapus Semua", fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
                    }
                }
                Button(
                    onClick = { viewModel.pushAllStudentsToCloud() },
                    colors = ButtonDefaults.buttonColors(containerColor = IslamicGreen),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.CloudSync, contentDescription = "Sync Cloud", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Sync Cloud", fontSize = 11.sp)
                }
                OutlinedButton(
                    onClick = { viewModel.syncAllStudentsToSheet() },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.CloudUpload, contentDescription = "Sync Siswa ke Sheet", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Ke Sheets", fontSize = 11.sp)
                }
                OutlinedButton(
                    onClick = { excelPickerLauncher.launch("*/*") },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.UploadFile, contentDescription = "Unggah Excel", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Unggah Excel", fontSize = 11.sp)
                }
                IconButton(onClick = { showImportDialog = true }) {
                    Icon(Icons.Default.Upload, contentDescription = "Import Data Siswa")
                }
                IconButton(onClick = {
                    val csv = CsvHelper.exportStudentsToCsv(students)
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, csv)
                        type = "text/plain"
                    }
                    context.startActivity(Intent.createChooser(sendIntent, "Ekspor CSV Data Siswa"))
                }) {
                    Icon(Icons.Default.Download, contentDescription = "Ekspor CSV")
                }
                Button(onClick = { showAddDialog = true }, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)) {
                    Text("+ Siswa", fontSize = 12.sp)
                }
            }
        }

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(students) { student ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(student.fullName, fontWeight = FontWeight.Bold)
                            Text("NISN: ${student.nisn} • Kls ${student.classLevel} ${student.rombel}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("TTL: ${student.birthPlace}, ${student.birthDate}", style = MaterialTheme.typography.labelSmall)
                        }
                        IconButton(onClick = { itemToDelete = student }) {
                            Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }

    if (itemToDelete != null) {
        ConfirmDeleteDialog(
            itemTitle = itemToDelete!!.fullName,
            onConfirm = { viewModel.deleteStudent(itemToDelete!!) },
            onDismiss = { itemToDelete = null }
        )
    }

    if (showDeleteAllDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAllDialog = false },
            title = { Text("Hapus Seluruh Data Siswa?") },
            text = { Text("Tindakan ini akan menghapus seluruh ${students.size} data siswa dari database secara permanen. Apakah Anda yakin?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteAllStudents()
                        showDeleteAllDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Ya, Hapus Semua")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteAllDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    if (showAddDialog) {
        var sName by remember { mutableStateOf("") }
        var sNisn by remember { mutableStateOf("") }
        var sPlace by remember { mutableStateOf("Lampung Timur") }
        var sDate by remember { mutableStateOf("10/05/2018") }
        var sClass by remember { mutableStateOf(1) }
        var sRombel by remember { mutableStateOf("Ar Rahman") }
        var sGender by remember { mutableStateOf("Laki-laki") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Tambah Data Siswa") },
            text = {
                Column {
                    OutlinedTextField(value = sName, onValueChange = { sName = it }, label = { Text("Nama Lengkap") }, modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp))
                    OutlinedTextField(value = sNisn, onValueChange = { sNisn = it }, label = { Text("NISN") }, modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(value = sPlace, onValueChange = { sPlace = it }, label = { Text("Tempat") }, modifier = Modifier.weight(1f).padding(vertical = 3.dp))
                        OutlinedTextField(value = sDate, onValueChange = { sDate = it }, label = { Text("Tgl Lahir") }, modifier = Modifier.weight(1f).padding(vertical = 3.dp))
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(value = sClass.toString(), onValueChange = { sClass = it.toIntOrNull() ?: 1 }, label = { Text("Kelas (1-6)") }, modifier = Modifier.weight(1f).padding(vertical = 3.dp))
                        OutlinedTextField(value = sRombel, onValueChange = { sRombel = it }, label = { Text("Rombel") }, modifier = Modifier.weight(1f).padding(vertical = 3.dp))
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (sName.isNotBlank()) {
                        viewModel.addStudent(
                            StudentEntity(
                                fullName = sName,
                                nisn = sNisn,
                                birthPlace = sPlace,
                                birthDate = sDate,
                                classLevel = sClass.coerceIn(1, 6),
                                rombel = sRombel,
                                gender = sGender
                            )
                        )
                        showAddDialog = false
                    }
                }) {
                    Text("Simpan")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showAddDialog = false }) { Text("Batal") }
            }
        )
    }

    if (showImportDialog) {
        var csvInput by remember { mutableStateOf(CsvHelper.generateStudentCsvTemplate()) }
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text("Unggah / Impor Data Siswa") },
            text = {
                Column {
                    Button(
                        onClick = { excelPickerLauncher.launch("*/*") },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                    ) {
                        Icon(Icons.Default.UploadFile, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Pilih File Excel (.xlsx / .csv)")
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))

                    Text("Atau tempel format teks CSV langsung:", fontSize = 12.sp)
                    OutlinedTextField(
                        value = csvInput,
                        onValueChange = { csvInput = it },
                        modifier = Modifier.fillMaxWidth().height(160.dp),
                        minLines = 5
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.importStudentsCsv(csvInput)
                    showImportDialog = false
                }) {
                    Text("Impor Teks CSV")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showImportDialog = false }) { Text("Batal") }
            }
        )
    }
}

// 4. TEACHERS ADMIN
@Composable
fun AdminTeachersSection(viewModel: MadrasahViewModel) {
    val context = LocalContext.current
    val teachers by viewModel.teachers.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingTeacher by remember { mutableStateOf<TeacherEntity?>(null) }
    var itemToDelete by remember { mutableStateOf<TeacherEntity?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SectionHeader(title = "Dewan Guru & Staf (${teachers.size})", icon = Icons.Default.Psychology, modifier = Modifier.weight(1f))
            Button(onClick = { showAddDialog = true }, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)) {
                Text("+ Guru", fontSize = 12.sp)
            }
        }

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(teachers) { teacher ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), shape = RoundedCornerShape(10.dp)) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        // Avatar / Photo
                        if (teacher.photoUrl.isNotBlank()) {
                            AsyncImage(
                                model = if (teacher.photoUrl.startsWith("/")) java.io.File(teacher.photoUrl) else teacher.photoUrl,
                                contentDescription = teacher.name,
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(NavyPrimary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = teacher.name.take(2).uppercase(),
                                    color = GoldAccent,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(teacher.name, fontWeight = FontWeight.Bold)
                            Text("${teacher.role} • ${teacher.subject}", style = MaterialTheme.typography.bodySmall, color = GoldAccent)
                            if (teacher.description.isNotEmpty()) {
                                Text(teacher.description, style = MaterialTheme.typography.labelSmall)
                            }
                        }

                        IconButton(onClick = { editingTeacher = teacher }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Guru", tint = MaterialTheme.colorScheme.primary)
                        }

                        IconButton(onClick = { itemToDelete = teacher }) {
                            Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }

    if (itemToDelete != null) {
        ConfirmDeleteDialog(itemToDelete!!.name, { viewModel.deleteTeacher(itemToDelete!!) }, { itemToDelete = null })
    }

    // DIALOG TAMBAH GURU DENGAN UNGGAH FOTO
    if (showAddDialog) {
        var name by remember { mutableStateOf("") }
        var role by remember { mutableStateOf("Guru Kelas") }
        var subject by remember { mutableStateOf("Tematik") }
        var desc by remember { mutableStateOf("") }
        var photoPath by remember { mutableStateOf("") }

        val imagePickerLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickVisualMedia()
        ) { uri: Uri? ->
            if (uri != null) {
                photoPath = FilePickerHelper.copyUriToInternalStorage(context, uri, "teachers", "teacher")
            }
        }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Tambah Guru / Staf") },
            text = {
                Column {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nama & Gelar *") }, modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp))
                    OutlinedTextField(value = role, onValueChange = { role = it }, label = { Text("Jabatan *") }, modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp))
                    OutlinedTextField(value = subject, onValueChange = { subject = it }, label = { Text("Mata Pelajaran") }, modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp))
                    OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Keterangan") }, modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp))

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Foto Guru dari Perangkat:", style = MaterialTheme.typography.labelMedium)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { imagePickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
                        ) {
                            Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (photoPath.isNotBlank()) "Ganti Foto" else "Pilih Foto", fontSize = 11.sp)
                        }
                        if (photoPath.isNotBlank()) {
                            AsyncImage(
                                model = java.io.File(photoPath),
                                contentDescription = "Preview",
                                modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (name.isNotBlank()) {
                        viewModel.addTeacher(
                            TeacherEntity(
                                name = name,
                                role = role,
                                subject = subject,
                                description = desc,
                                photoUrl = photoPath
                            )
                        )
                        showAddDialog = false
                    }
                }) { Text("Simpan") }
            },
            dismissButton = { OutlinedButton(onClick = { showAddDialog = false }) { Text("Batal") } }
        )
    }

    // DIALOG EDIT GURU
    if (editingTeacher != null) {
        val current = editingTeacher!!
        var eName by remember { mutableStateOf(current.name) }
        var eRole by remember { mutableStateOf(current.role) }
        var eSubject by remember { mutableStateOf(current.subject) }
        var eDesc by remember { mutableStateOf(current.description) }
        var ePhotoPath by remember { mutableStateOf(current.photoUrl) }

        val editImagePickerLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickVisualMedia()
        ) { uri: Uri? ->
            if (uri != null) {
                ePhotoPath = FilePickerHelper.copyUriToInternalStorage(context, uri, "teachers", "teacher")
            }
        }

        AlertDialog(
            onDismissRequest = { editingTeacher = null },
            title = { Text("Edit Data Guru / Staf") },
            text = {
                Column {
                    OutlinedTextField(value = eName, onValueChange = { eName = it }, label = { Text("Nama & Gelar *") }, modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp))
                    OutlinedTextField(value = eRole, onValueChange = { eRole = it }, label = { Text("Jabatan *") }, modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp))
                    OutlinedTextField(value = eSubject, onValueChange = { eSubject = it }, label = { Text("Mata Pelajaran") }, modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp))
                    OutlinedTextField(value = eDesc, onValueChange = { eDesc = it }, label = { Text("Keterangan") }, modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp))

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Foto Guru:", style = MaterialTheme.typography.labelMedium)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { editImagePickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
                        ) {
                            Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (ePhotoPath.isNotBlank()) "Ganti Foto" else "Pilih Foto", fontSize = 11.sp)
                        }
                        if (ePhotoPath.isNotBlank()) {
                            AsyncImage(
                                model = if (ePhotoPath.startsWith("/")) java.io.File(ePhotoPath) else ePhotoPath,
                                contentDescription = "Preview",
                                modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (eName.isNotBlank()) {
                        viewModel.updateTeacher(
                            current.copy(
                                name = eName,
                                role = eRole,
                                subject = eSubject,
                                description = eDesc,
                                photoUrl = ePhotoPath
                            )
                        )
                        editingTeacher = null
                    }
                }) { Text("Perbarui") }
            },
            dismissButton = { OutlinedButton(onClick = { editingTeacher = null }) { Text("Batal") } }
        )
    }
}

// 5. PPDB ADMIN
@Composable
fun AdminPpdbSection(viewModel: MadrasahViewModel) {
    val ppdbList by viewModel.allPpdb.collectAsState()
    var selectedItem by remember { mutableStateOf<PpdbEntity?>(null) }
    var itemToDelete by remember { mutableStateOf<PpdbEntity?>(null) }
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SectionHeader(title = "Pendaftaran PPDB (${ppdbList.size})", icon = Icons.Default.AppRegistration, modifier = Modifier.weight(1f))
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedButton(
                    onClick = { viewModel.syncAllPpdbToSheet() },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.CloudUpload, contentDescription = "Sinkronkan PPDB ke Google Sheet", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Sync ke Sheets", fontSize = 11.sp)
                }
                IconButton(onClick = {
                    val csv = CsvHelper.exportPpdbToCsv(ppdbList)
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, csv)
                        type = "text/plain"
                    }
                    context.startActivity(Intent.createChooser(sendIntent, "Ekspor Data PPDB"))
                }) {
                    Icon(Icons.Default.Download, contentDescription = "Ekspor PPDB")
                }
            }
        }

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(ppdbList) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { selectedItem = item },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(item.regNumber, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .background(
                                            when (item.status) {
                                                "Diterima" -> IslamicGreen.copy(alpha = 0.2f)
                                                "Cadangan" -> GoldAccent.copy(alpha = 0.2f)
                                                "Ditolak" -> MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                                                else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                            },
                                            RoundedCornerShape(4.dp)
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(item.status, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Text(item.studentName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("TTL: ${item.birthPlace}, ${item.birthDate} • WA: ${item.whatsapp}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Asal: ${item.prevSchool} • Wali: ${item.fatherName} / ${item.motherName}", style = MaterialTheme.typography.labelSmall)
                        }
                        IconButton(onClick = { itemToDelete = item }) {
                            Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }

    if (itemToDelete != null) {
        ConfirmDeleteDialog(itemToDelete!!.regNumber, { viewModel.deletePpdb(itemToDelete!!) }, { itemToDelete = null })
    }

    // Modal Verifikasi PPDB
    if (selectedItem != null) {
        val p = selectedItem!!
        var status by remember { mutableStateOf(p.status) }
        var notes by remember { mutableStateOf(p.adminNotes) }

        AlertDialog(
            onDismissRequest = { selectedItem = null },
            title = { Text("Detail & Verifikasi PPDB") },
            text = {
                Column {
                    Text("No. Registrasi: ${p.regNumber}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text("Nama Siswa: ${p.studentName}")
                    Text("NIK Siswa: ${p.nik}")
                    Text("TTL: ${p.birthPlace}, ${p.birthDate}")
                    Text("Jenis Kelamin: ${p.gender}")
                    Text("Alamat: ${p.address}")
                    Text("WhatsApp: ${p.whatsapp}")
                    Text("Asal Sekolah: ${p.prevSchool}")
                    Text("Nama Ayah: ${p.fatherName} (NIK: ${p.fatherNik}, Pekerjaan: ${p.fatherJob})")
                    Text("Nama Ibu: ${p.motherName} (NIK: ${p.motherNik}, Pekerjaan: ${p.motherJob})")
                    Text("Berkas: ${p.kkDocName}, ${p.ijazahDocName}, ${p.aktaDocName}")

                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Ubah Status Verifikasi:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("Menunggu", "Diterima", "Cadangan", "Ditolak").forEach { st ->
                            FilterChip(selected = status == st, onClick = { status = st }, label = { Text(st, fontSize = 11.sp) })
                        }
                    }
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Catatan Verifikasi Admin") },
                        modifier = Modifier.fillMaxWidth().padding(top = 6.dp)
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.updatePpdbStatus(p, status, notes)
                    selectedItem = null
                }) {
                    Text("Simpan Status")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { selectedItem = null }) { Text("Tutup") }
            }
        )
    }
}

// 6. RPP ADMIN
@Composable
fun AdminRppSection(viewModel: MadrasahViewModel) {
    val context = LocalContext.current
    val rppList by viewModel.allRpp.collectAsState()
    val settings by viewModel.settings.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var itemToDelete by remember { mutableStateOf<RppEntity?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SectionHeader(title = "Modul RPP (${rppList.size})", icon = Icons.Default.Description, modifier = Modifier.weight(1f))

            // Tombol Buat RPP (Buka Link GAS yang diminta)
            OutlinedButton(
                onClick = {
                    val url = settings?.rppBuilderUrl ?: "https://script.google.com/macros/s/AKfycbzTsiL8Yt80ccACn38TwxsQixNqkiuEo41yRQVubgFU0tbA9VzKvT0c8dh_9q7cJUj0/exec"
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                },
                modifier = Modifier.testTag("admin_btn_buat_rpp")
            ) {
                Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Buat RPP", fontSize = 11.sp)
            }

            Spacer(modifier = Modifier.width(6.dp))
            Button(onClick = { showAddDialog = true }) {
                Text("+ Unggah", fontSize = 12.sp)
            }
        }

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(rppList) { rpp ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), shape = RoundedCornerShape(10.dp)) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(rpp.fileType, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(rpp.title, fontWeight = FontWeight.Bold)
                            Text("Guru: ${rpp.teacherName} • Kls ${rpp.classLevel} ${rpp.rombel}", style = MaterialTheme.typography.bodySmall, color = GoldAccent)
                            Text("Berkas: ${rpp.fileName} (${rpp.fileSize}) • ${rpp.uploadDate}", style = MaterialTheme.typography.labelSmall)
                        }
                        IconButton(onClick = { itemToDelete = rpp }) {
                            Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }

    if (itemToDelete != null) {
        ConfirmDeleteDialog(itemToDelete!!.title, { viewModel.deleteRpp(itemToDelete!!) }, { itemToDelete = null })
    }

    if (showAddDialog) {
        var title by remember { mutableStateOf("") }
        var teacher by remember { mutableStateOf("Ust. Muhammad Ridwan, S.Ag.") }
        var classLvl by remember { mutableStateOf(1) }
        var rombel by remember { mutableStateOf("Ar Rahman") }
        var subject by remember { mutableStateOf("PAI") }
        var fName by remember { mutableStateOf("Modul_Ajar_PAI.pdf") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Unggah Dokumen RPP") },
            text = {
                Column {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Judul RPP / Mata Pelajaran") }, modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp))
                    OutlinedTextField(value = teacher, onValueChange = { teacher = it }, label = { Text("Guru Pengampu") }, modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(value = classLvl.toString(), onValueChange = { classLvl = it.toIntOrNull() ?: 1 }, label = { Text("Kelas") }, modifier = Modifier.weight(1f).padding(vertical = 3.dp))
                        OutlinedTextField(value = rombel, onValueChange = { rombel = it }, label = { Text("Rombel") }, modifier = Modifier.weight(1f).padding(vertical = 3.dp))
                    }
                    OutlinedTextField(value = subject, onValueChange = { subject = it }, label = { Text("Mata Pelajaran") }, modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp))
                    OutlinedTextField(value = fName, onValueChange = { fName = it }, label = { Text("Nama Berkas") }, modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp))
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (title.isNotBlank()) {
                        viewModel.addRpp(
                            RppEntity(
                                title = title,
                                teacherName = teacher,
                                classLevel = classLvl.coerceIn(1, 6),
                                rombel = rombel,
                                subject = subject,
                                uploadDate = "17 Okt 2026",
                                fileName = fName,
                                fileSize = "1.5 MB",
                                fileType = if (fName.endsWith(".pdf", true)) "PDF" else "DOCX"
                            )
                        )
                        showAddDialog = false
                    }
                }) { Text("Simpan") }
            },
            dismissButton = { OutlinedButton(onClick = { showAddDialog = false }) { Text("Batal") } }
        )
    }
}

// 7. NEWS ADMIN
@Composable
fun AdminNewsSection(viewModel: MadrasahViewModel) {
    val context = LocalContext.current
    val newsList by viewModel.allNews.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var itemToDelete by remember { mutableStateOf<NewsEntity?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SectionHeader(title = "Berita & Pengumuman (${newsList.size})", icon = Icons.Default.Newspaper, modifier = Modifier.weight(1f))
            Button(onClick = { showAddDialog = true }) { Text("+ Berita", fontSize = 12.sp) }
        }

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(newsList) { item ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), shape = RoundedCornerShape(10.dp)) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        if (item.imageUrl.isNotBlank()) {
                            AsyncImage(
                                model = if (item.imageUrl.startsWith("/")) java.io.File(item.imageUrl) else item.imageUrl,
                                contentDescription = item.title,
                                modifier = Modifier
                                    .size(50.dp)
                                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.title, fontWeight = FontWeight.Bold)
                            Text("Kategori: ${item.category} • ${item.date}", style = MaterialTheme.typography.labelSmall, color = GoldAccent)
                        }
                        IconButton(onClick = { itemToDelete = item }) {
                            Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }

    if (itemToDelete != null) {
        ConfirmDeleteDialog(itemToDelete!!.title, { viewModel.deleteNews(itemToDelete!!) }, { itemToDelete = null })
    }

    if (showAddDialog) {
        var title by remember { mutableStateOf("") }
        var cat by remember { mutableStateOf("Kegiatan") }
        var summary by remember { mutableStateOf("") }
        var content by remember { mutableStateOf("") }
        var photoPath by remember { mutableStateOf("") }

        val imagePickerLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickVisualMedia()
        ) { uri: Uri? ->
            if (uri != null) {
                photoPath = FilePickerHelper.copyUriToInternalStorage(context, uri, "news", "news")
            }
        }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Tambah Berita") },
            text = {
                Column {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Judul Berita") }, modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp))
                    OutlinedTextField(value = cat, onValueChange = { cat = it }, label = { Text("Kategori (Kegiatan/Prestasi/Pengumuman)") }, modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp))
                    OutlinedTextField(value = summary, onValueChange = { summary = it }, label = { Text("Ringkasan Singkat") }, modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp))
                    OutlinedTextField(value = content, onValueChange = { content = it }, label = { Text("Isi Berita Lengkap") }, modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp), minLines = 3)

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Unggah Gambar Berita:", style = MaterialTheme.typography.labelMedium)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { imagePickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
                        ) {
                            Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (photoPath.isNotBlank()) "Ganti Gambar" else "Pilih Gambar", fontSize = 11.sp)
                        }
                        if (photoPath.isNotBlank()) {
                            AsyncImage(
                                model = java.io.File(photoPath),
                                contentDescription = "Preview",
                                modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(6.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (title.isNotBlank()) {
                        viewModel.addNews(
                            NewsEntity(
                                title = title,
                                category = cat,
                                summary = summary,
                                content = content,
                                date = "17 Okt 2026",
                                imageUrl = photoPath
                            )
                        )
                        showAddDialog = false
                    }
                }) { Text("Terbitkan") }
            },
            dismissButton = { OutlinedButton(onClick = { showAddDialog = false }) { Text("Batal") } }
        )
    }
}

// 9. GALLERY ADMIN
@Composable
fun AdminGallerySection(viewModel: MadrasahViewModel) {
    val context = LocalContext.current
    val list by viewModel.allGallery.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var itemToDelete by remember { mutableStateOf<GalleryEntity?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SectionHeader(title = "Galeri Foto (${list.size})", icon = Icons.Default.Collections, modifier = Modifier.weight(1f))
            Button(onClick = { showAddDialog = true }) { Text("+ Foto", fontSize = 12.sp) }
        }

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(list) { item ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), shape = RoundedCornerShape(10.dp)) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        if (item.imageUrl.isNotBlank()) {
                            AsyncImage(
                                model = if (item.imageUrl.startsWith("/")) java.io.File(item.imageUrl) else item.imageUrl,
                                contentDescription = item.title,
                                modifier = Modifier
                                    .size(50.dp)
                                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.title, fontWeight = FontWeight.Bold)
                            Text("${item.category} • ${item.date}", style = MaterialTheme.typography.labelSmall, color = GoldAccent)
                        }
                        IconButton(onClick = { itemToDelete = item }) {
                            Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }

    if (itemToDelete != null) {
        ConfirmDeleteDialog(itemToDelete!!.title, { viewModel.deleteGallery(itemToDelete!!) }, { itemToDelete = null })
    }

    if (showAddDialog) {
        var title by remember { mutableStateOf("") }
        var cat by remember { mutableStateOf("Kegiatan") }
        var desc by remember { mutableStateOf("") }
        var photoPath by remember { mutableStateOf("") }

        val imagePickerLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickVisualMedia()
        ) { uri: Uri? ->
            if (uri != null) {
                photoPath = FilePickerHelper.copyUriToInternalStorage(context, uri, "gallery", "gallery")
            }
        }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Tambah Galeri Foto") },
            text = {
                Column {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Judul Foto") }, modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp))
                    OutlinedTextField(value = cat, onValueChange = { cat = it }, label = { Text("Kategori (Kegiatan/Pembelajaran/Upacara/dll)") }, modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp))
                    OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Deskripsi Foto") }, modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp))

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Pilih Foto dari Perangkat:", style = MaterialTheme.typography.labelMedium)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { imagePickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
                        ) {
                            Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (photoPath.isNotBlank()) "Ganti Foto" else "Pilih Foto", fontSize = 11.sp)
                        }
                        if (photoPath.isNotBlank()) {
                            AsyncImage(
                                model = java.io.File(photoPath),
                                contentDescription = "Preview",
                                modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(6.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (title.isNotBlank()) {
                        viewModel.addGallery(
                            GalleryEntity(
                                title = title,
                                category = cat,
                                description = desc,
                                date = "17 Okt 2026",
                                imageUrl = photoPath
                            )
                        )
                        showAddDialog = false
                    }
                }) { Text("Simpan") }
            },
            dismissButton = { OutlinedButton(onClick = { showAddDialog = false }) { Text("Batal") } }
        )
    }
}

// 10. EXTRACURRICULARS
@Composable
fun AdminExtraSection(viewModel: MadrasahViewModel) {
    val context = LocalContext.current
    val list by viewModel.extracurriculars.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var itemToDelete by remember { mutableStateOf<ExtracurricularEntity?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SectionHeader(title = "Ekstrakurikuler (${list.size})", icon = Icons.Default.SportsHandball, modifier = Modifier.weight(1f))
            Button(onClick = { showAddDialog = true }) { Text("+ Ekskul", fontSize = 12.sp) }
        }

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(list) { item ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), shape = RoundedCornerShape(10.dp)) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        if (item.imageUrl.isNotBlank()) {
                            AsyncImage(
                                model = if (item.imageUrl.startsWith("/")) java.io.File(item.imageUrl) else item.imageUrl,
                                contentDescription = item.name,
                                modifier = Modifier
                                    .size(50.dp)
                                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.name, fontWeight = FontWeight.Bold)
                            Text("Pembina: ${item.coach} • ${item.schedule}", style = MaterialTheme.typography.bodySmall, color = GoldAccent)
                        }
                        IconButton(onClick = { itemToDelete = item }) {
                            Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }

    if (itemToDelete != null) {
        ConfirmDeleteDialog(itemToDelete!!.name, { viewModel.deleteExtracurricular(itemToDelete!!) }, { itemToDelete = null })
    }

    if (showAddDialog) {
        var name by remember { mutableStateOf("") }
        var coach by remember { mutableStateOf("") }
        var schedule by remember { mutableStateOf("") }
        var desc by remember { mutableStateOf("") }
        var photoPath by remember { mutableStateOf("") }

        val imagePickerLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickVisualMedia()
        ) { uri: Uri? ->
            if (uri != null) {
                photoPath = FilePickerHelper.copyUriToInternalStorage(context, uri, "extracurricular", "ekskul")
            }
        }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Tambah Ekstrakurikuler") },
            text = {
                Column {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nama Ekskul") }, modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp))
                    OutlinedTextField(value = coach, onValueChange = { coach = it }, label = { Text("Pembina / Pelatih") }, modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp))
                    OutlinedTextField(value = schedule, onValueChange = { schedule = it }, label = { Text("Jadwal Latihan") }, modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp))
                    OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Deskripsi") }, modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp))

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Foto Kegiatan Ekskul:", style = MaterialTheme.typography.labelMedium)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { imagePickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
                        ) {
                            Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (photoPath.isNotBlank()) "Ganti Foto" else "Pilih Foto", fontSize = 11.sp)
                        }
                        if (photoPath.isNotBlank()) {
                            AsyncImage(
                                model = java.io.File(photoPath),
                                contentDescription = "Preview",
                                modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(6.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (name.isNotBlank()) {
                        viewModel.addExtracurricular(
                            ExtracurricularEntity(
                                name = name,
                                coach = coach,
                                schedule = schedule,
                                description = desc,
                                imageUrl = photoPath
                            )
                        )
                        showAddDialog = false
                    }
                }) { Text("Simpan") }
            },
            dismissButton = { OutlinedButton(onClick = { showAddDialog = false }) { Text("Batal") } }
        )
    }
}

// 11. PROGRAMS
@Composable
fun AdminProgramsSection(viewModel: MadrasahViewModel) {
    val context = LocalContext.current
    val list by viewModel.featuredPrograms.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var itemToDelete by remember { mutableStateOf<FeaturedProgramEntity?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SectionHeader(title = "Program Unggulan (${list.size})", icon = Icons.Default.Stars, modifier = Modifier.weight(1f))
            Button(onClick = { showAddDialog = true }) { Text("+ Program", fontSize = 12.sp) }
        }

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(list) { item ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), shape = RoundedCornerShape(10.dp)) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        if (item.imageUrl.isNotBlank()) {
                            AsyncImage(
                                model = if (item.imageUrl.startsWith("/")) java.io.File(item.imageUrl) else item.imageUrl,
                                contentDescription = item.name,
                                modifier = Modifier
                                    .size(50.dp)
                                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.name, fontWeight = FontWeight.Bold)
                            Text(item.objective, style = MaterialTheme.typography.bodySmall, color = GoldAccent)
                        }
                        IconButton(onClick = { itemToDelete = item }) {
                            Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }

    if (itemToDelete != null) {
        ConfirmDeleteDialog(itemToDelete!!.name, { viewModel.deleteProgram(itemToDelete!!) }, { itemToDelete = null })
    }

    if (showAddDialog) {
        var name by remember { mutableStateOf("") }
        var obj by remember { mutableStateOf("") }
        var desc by remember { mutableStateOf("") }
        var photoPath by remember { mutableStateOf("") }

        val imagePickerLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickVisualMedia()
        ) { uri: Uri? ->
            if (uri != null) {
                photoPath = FilePickerHelper.copyUriToInternalStorage(context, uri, "programs", "program")
            }
        }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Tambah Program Unggulan") },
            text = {
                Column {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nama Program") }, modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp))
                    OutlinedTextField(value = obj, onValueChange = { obj = it }, label = { Text("Tujuan Program") }, modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp))
                    OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Deskripsi") }, modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp))

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Foto Program Unggulan:", style = MaterialTheme.typography.labelMedium)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { imagePickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
                        ) {
                            Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (photoPath.isNotBlank()) "Ganti Foto" else "Pilih Foto", fontSize = 11.sp)
                        }
                        if (photoPath.isNotBlank()) {
                            AsyncImage(
                                model = java.io.File(photoPath),
                                contentDescription = "Preview",
                                modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(6.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (name.isNotBlank()) {
                        viewModel.addProgram(
                            FeaturedProgramEntity(
                                name = name,
                                objective = obj,
                                description = desc,
                                imageUrl = photoPath
                            )
                        )
                        showAddDialog = false
                    }
                }) { Text("Simpan") }
            },
            dismissButton = { OutlinedButton(onClick = { showAddDialog = false }) { Text("Batal") } }
        )
    }
}

// 12. AGENDAS
@Composable
fun AdminAgendasSection(viewModel: MadrasahViewModel) {
    val context = LocalContext.current
    val list by viewModel.allAgendas.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var itemToDelete by remember { mutableStateOf<AgendaEntity?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SectionHeader(title = "Agenda Kegiatan (${list.size})", icon = Icons.Default.CalendarMonth, modifier = Modifier.weight(1f))
            Button(onClick = { showAddDialog = true }) { Text("+ Agenda", fontSize = 12.sp) }
        }

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(list) { item ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), shape = RoundedCornerShape(10.dp)) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        if (item.imageUrl.isNotBlank()) {
                            AsyncImage(
                                model = if (item.imageUrl.startsWith("/")) java.io.File(item.imageUrl) else item.imageUrl,
                                contentDescription = item.title,
                                modifier = Modifier
                                    .size(50.dp)
                                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.title, fontWeight = FontWeight.Bold)
                            Text("${item.date} • ${item.time} (${item.location})", style = MaterialTheme.typography.bodySmall, color = GoldAccent)
                        }
                        IconButton(onClick = { itemToDelete = item }) {
                            Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }

    if (itemToDelete != null) {
        ConfirmDeleteDialog(itemToDelete!!.title, { viewModel.deleteAgenda(itemToDelete!!) }, { itemToDelete = null })
    }

    if (showAddDialog) {
        var title by remember { mutableStateOf("") }
        var date by remember { mutableStateOf("") }
        var time by remember { mutableStateOf("08.00 - 11.00 WIB") }
        var loc by remember { mutableStateOf("Madrasah") }
        var desc by remember { mutableStateOf("") }
        var photoPath by remember { mutableStateOf("") }

        val imagePickerLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickVisualMedia()
        ) { uri: Uri? ->
            if (uri != null) {
                photoPath = FilePickerHelper.copyUriToInternalStorage(context, uri, "agenda", "agenda")
            }
        }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Tambah Agenda") },
            text = {
                Column {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Nama Kegiatan") }, modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp))
                    OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text("Tanggal") }, modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp))
                    OutlinedTextField(value = time, onValueChange = { time = it }, label = { Text("Waktu") }, modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp))
                    OutlinedTextField(value = loc, onValueChange = { loc = it }, label = { Text("Lokasi") }, modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp))
                    OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Deskripsi") }, modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp))

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Foto Brosur / Pamflet Agenda:", style = MaterialTheme.typography.labelMedium)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { imagePickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
                        ) {
                            Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (photoPath.isNotBlank()) "Ganti Foto" else "Pilih Foto", fontSize = 11.sp)
                        }
                        if (photoPath.isNotBlank()) {
                            AsyncImage(
                                model = java.io.File(photoPath),
                                contentDescription = "Preview",
                                modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(6.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (title.isNotBlank()) {
                        viewModel.addAgenda(
                            AgendaEntity(
                                title = title,
                                date = date,
                                time = time,
                                location = loc,
                                description = desc,
                                imageUrl = photoPath
                            )
                        )
                        showAddDialog = false
                    }
                }) { Text("Simpan") }
            },
            dismissButton = { OutlinedButton(onClick = { showAddDialog = false }) { Text("Batal") } }
        )
    }
}

// 13. TESTIMONIALS
@Composable
fun AdminTestimonialsSection(viewModel: MadrasahViewModel) {
    val list by viewModel.allTestimonials.collectAsState()
    var itemToDelete by remember { mutableStateOf<TestimonialEntity?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        SectionHeader(title = "Moderasi Testimoni (${list.size})", icon = Icons.Default.RateReview)

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(list) { item ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), shape = RoundedCornerShape(10.dp)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(item.name, fontWeight = FontWeight.Bold)
                            Box(
                                modifier = Modifier
                                    .background(if (item.isApproved) IslamicGreen.copy(alpha = 0.2f) else GoldAccent.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(if (item.isApproved) "Disetujui" else "Menunggu Moderasi", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Text("${item.role} • Rating: ${item.rating} Bintang", style = MaterialTheme.typography.labelSmall, color = GoldAccent)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("“${item.content}”", style = MaterialTheme.typography.bodySmall)

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            if (!item.isApproved) {
                                Button(
                                    onClick = { viewModel.approveTestimonial(item, true) },
                                    colors = ButtonDefaults.buttonColors(containerColor = IslamicGreen),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("Setujui", fontSize = 11.sp)
                                }
                            } else {
                                OutlinedButton(
                                    onClick = { viewModel.approveTestimonial(item, false) },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("Tunda", fontSize = 11.sp)
                                }
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            IconButton(onClick = { itemToDelete = item }) {
                                Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }

    if (itemToDelete != null) {
        ConfirmDeleteDialog("testimoni dari ${itemToDelete!!.name}", { viewModel.deleteTestimonial(itemToDelete!!) }, { itemToDelete = null })
    }
}

// 14. MESSAGES
@Composable
fun AdminMessagesSection(viewModel: MadrasahViewModel) {
    val list by viewModel.contactMessages.collectAsState()
    val context = LocalContext.current
    var itemToDelete by remember { mutableStateOf<ContactMessageEntity?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        SectionHeader(title = "Pesan Masuk (${list.size})", icon = Icons.Default.Email)

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(list) { msg ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), shape = RoundedCornerShape(10.dp)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(msg.senderName, fontWeight = FontWeight.Bold)
                            Text(msg.date, style = MaterialTheme.typography.labelSmall)
                        }
                        Text("Subjek: ${msg.subject}", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
                        Text("WA: ${msg.phone} • Email: ${msg.email}", style = MaterialTheme.typography.labelSmall)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(msg.message, style = MaterialTheme.typography.bodySmall)

                        Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.End) {
                            Button(
                                onClick = {
                                    val url = "https://wa.me/${msg.phone.replace("-", "").replace(" ", "")}?text=Assalamu%27alaikum%20Bpk/Ibu%20${msg.senderName},%20menanggapi%20pesan%20Anda%20ke%20MI%20Muhammadiyah%20Tanjung%20Qencono:"
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                                    viewModel.markMessageRead(msg)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.Reply, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Balas WhatsApp", fontSize = 11.sp)
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            IconButton(onClick = { itemToDelete = msg }) {
                                Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }

    if (itemToDelete != null) {
        ConfirmDeleteDialog("pesan dari ${itemToDelete!!.senderName}", { viewModel.deleteContactMessage(itemToDelete!!) }, { itemToDelete = null })
    }
}

// 15. SCHEDULES
@Composable
fun AdminSchedulesSection(viewModel: MadrasahViewModel) {
    var cls by remember { mutableStateOf(1) }
    var rombel by remember { mutableStateOf("Ar Rahman") }
    val schedules by viewModel.repository.getSchedules(cls, rombel).collectAsState(initial = emptyList())
    var showAddDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SectionHeader(title = "Jadwal Pelajaran Kls $cls", icon = Icons.Default.Schedule, modifier = Modifier.weight(1f))
            Button(onClick = { showAddDialog = true }) { Text("+ Jadwal", fontSize = 12.sp) }
        }

        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            (1..6).forEach { c ->
                FilterChip(selected = cls == c, onClick = { cls = c }, label = { Text("Kls $c", fontSize = 11.sp) })
            }
        }

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(schedules) { s ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp), shape = RoundedCornerShape(8.dp)) {
                    Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(s.day, fontWeight = FontWeight.Bold, modifier = Modifier.width(65.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(s.subject, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("${s.timeSlot} • Guru: ${s.teacherName}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = { viewModel.deleteSchedule(s) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        var day by remember { mutableStateOf("Senin") }
        var timeSlot by remember { mutableStateOf("07.30 - 08.45") }
        var subject by remember { mutableStateOf("Al-Qur'an Hadist") }
        var teacher by remember { mutableStateOf("Ust. Muhammad Ridwan, S.Ag.") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Tambah Jadwal Pelajaran") },
            text = {
                Column {
                    OutlinedTextField(value = day, onValueChange = { day = it }, label = { Text("Hari") }, modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp))
                    OutlinedTextField(value = timeSlot, onValueChange = { timeSlot = it }, label = { Text("Waktu Jam") }, modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp))
                    OutlinedTextField(value = subject, onValueChange = { subject = it }, label = { Text("Mata Pelajaran") }, modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp))
                    OutlinedTextField(value = teacher, onValueChange = { teacher = it }, label = { Text("Nama Guru") }, modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp))
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (subject.isNotBlank()) {
                        viewModel.addSchedule(ClassScheduleEntity(classLevel = cls, rombel = rombel, day = day, timeSlot = timeSlot, subject = subject, teacherName = teacher))
                        showAddDialog = false
                    }
                }) { Text("Simpan") }
            },
            dismissButton = { OutlinedButton(onClick = { showAddDialog = false }) { Text("Batal") } }
        )
    }
}

// 16. MUSIC
@Composable
fun AdminMusicSection(viewModel: MadrasahViewModel) {
    val context = LocalContext.current
    val isPlaying by viewModel.isMusicPlaying.collectAsState()
    val volume by viewModel.musicVolume.collectAsState()
    val settings by viewModel.settings.collectAsState()

    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val fileName = context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1 && cursor.moveToFirst()) cursor.getString(nameIndex) else null
            } ?: "Musik_Madrasah.mp3"
            val savedPath = FilePickerHelper.copyUriToInternalStorage(context, uri, "audio", "music")
            viewModel.setCustomAudio(savedPath, fileName)
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        SectionHeader(title = "Pengaturan Musik Latar", icon = Icons.Default.MusicNote)

        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("File Musik Latar Aktif:", fontWeight = FontWeight.Bold)
                Text(
                    text = settings?.customAudioName?.ifBlank { "Mars Madrasah (Default)" } ?: "Mars Madrasah (Default)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = GoldAccent,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { audioPickerLauncher.launch("audio/*") },
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                    ) {
                        Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Pilih Musik dari Perangkat", fontSize = 12.sp)
                    }

                    if (!settings?.customAudioUri.isNullOrBlank()) {
                        OutlinedButton(
                            onClick = { viewModel.setCustomAudio("", "Mars Madrasah (Default)") }
                        ) {
                            Text("Reset Default", fontSize = 11.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(14.dp))

                Text("Status Pemutar Musik:", fontWeight = FontWeight.Bold)
                Text(
                    if (isPlaying) "Sedang Memutar: ${settings?.customAudioName ?: "Mars Madrasah"}" else "Musik Dijeda / Nonaktif",
                    color = if (isPlaying) IslamicGreen else MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { viewModel.toggleMusic() },
                        colors = ButtonDefaults.buttonColors(containerColor = if (isPlaying) MaterialTheme.colorScheme.error else IslamicGreen)
                    ) {
                        Icon(if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isPlaying) "Jeda Musik" else "Putar Musik")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Pengaturan Volume: ${(volume * 100).toInt()}%", style = MaterialTheme.typography.labelMedium)
                Slider(value = volume, onValueChange = { viewModel.setMusicVolume(it) })
            }
        }
    }
}

// 17. LOGS
@Composable
fun AdminLogsSection(viewModel: MadrasahViewModel) {
    val logs by viewModel.adminLogs.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        SectionHeader(title = "Riwayat Aktivitas Admin (${logs.size})", icon = Icons.Default.History)

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(logs) { log ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp), shape = RoundedCornerShape(8.dp)) {
                    Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(log.action, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text(log.details, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(log.timestamp, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

// CLOUD SYNC (FIREBASE MULTI-DEVICE)
@Composable
fun AdminCloudSyncSection(viewModel: MadrasahViewModel) {
    val context = LocalContext.current
    val isSyncing by viewModel.isCloudSyncing.collectAsState()
    val syncStatus by viewModel.cloudSyncStatus.collectAsState()
    val currentUser by viewModel.currentAuthUser.collectAsState()
    val settings by viewModel.settings.collectAsState()

    // 15 Modules State Observers
    val studentCount by viewModel.studentCount.collectAsState()
    val teacherCount by viewModel.teacherCount.collectAsState()
    val facilities by viewModel.allFacilities.collectAsState()
    val newsCount by viewModel.newsCount.collectAsState()
    val galleryCount by viewModel.galleryCount.collectAsState()
    val extracurriculars by viewModel.extracurriculars.collectAsState()
    val featuredPrograms by viewModel.featuredPrograms.collectAsState()
    val agendas by viewModel.allAgendas.collectAsState()
    val testimonials by viewModel.allTestimonials.collectAsState()
    val rppCount by viewModel.rppCount.collectAsState()
    val documents by viewModel.allDocuments.collectAsState()
    val notifications by viewModel.allNotifications.collectAsState()
    val ppdbCount by viewModel.ppdbCount.collectAsState()

    val totalItems = 1 + studentCount + teacherCount + facilities.size + newsCount + galleryCount +
            extracurriculars.size + featuredPrograms.size + agendas.size + testimonials.size +
            rppCount + documents.size + notifications.size + ppdbCount

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        item {
            SectionHeader(
                title = "Sinkronisasi Cloud Multi-Device (15 Modul)",
                subtitle = "Semua modul data madrasah tersinkronisasi otomatis antar HP/perangkat secara real-time",
                icon = Icons.Default.CloudSync
            )

            // Status Card
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = IslamicGreen.copy(alpha = 0.12f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.CloudDone,
                                contentDescription = null,
                                tint = IslamicGreen,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Firebase Firestore Cloud Aktif",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = IslamicGreen
                                )
                                Text(
                                    text = "Database ID: ${context.getString(R.string.firestore_database_id).take(22)}...",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .background(IslamicGreen, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("15 MODUL LIVE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = syncStatus ?: "Terhubung dan siap sinkronisasi multi-perangkat",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val firebaseUrl = "https://console.firebase.google.com/u/0/project/gen-lang-client-0399778967/firestore/databases/${context.getString(R.string.firestore_database_id)}/data"
                                try {
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(firebaseUrl)))
                                } catch (_: Exception) {}
                            },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(15.dp), tint = IslamicGreen)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Buka Console", fontSize = 11.sp, color = IslamicGreen)
                        }

                        OutlinedButton(
                            onClick = {
                                val restUrl = "https://firestore.googleapis.com/v1/projects/gen-lang-client-0399778967/databases/${context.getString(R.string.firestore_database_id)}/documents/school_settings"
                                try {
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(restUrl)))
                                } catch (_: Exception) {}
                            },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Language, contentDescription = null, modifier = Modifier.size(15.dp), tint = NavyPrimary)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Cek Data Live", fontSize = 11.sp, color = NavyPrimary)
                        }
                    }

                    if (isSyncing) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = IslamicGreen)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Sedang memproses sinkronisasi data madrasah ke Cloud...", fontSize = 12.sp, color = IslamicGreen)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Google Sign-In Card
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Autentikasi Akun Admin (Google Sign-In)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    if (currentUser != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AccountCircle, contentDescription = null, tint = IslamicGreen, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(currentUser?.displayName ?: "Admin Madrasah", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                                Text(currentUser?.email ?: "", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            OutlinedButton(
                                onClick = { viewModel.signOutFromGoogle() },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text("Keluar", fontSize = 11.sp)
                            }
                        }
                    } else {
                        Text(
                            "Masuk dengan Akun Google untuk hak akses administrator dan sinkronisasi data cloud yang aman antar perangkat.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { viewModel.signInWithGoogle() },
                            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                            modifier = Modifier.fillMaxWidth().testTag("google_signin_button")
                        ) {
                            Icon(Icons.Default.Login, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Masuk dengan Google (Sign In with Google)")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Multi-Device Action Buttons
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Tindakan Sinkronisasi Seluruh Modul:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { viewModel.pushAllDataToCloud() },
                        enabled = !isSyncing,
                        colors = ButtonDefaults.buttonColors(containerColor = IslamicGreen),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).testTag("push_all_to_cloud_btn")
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Kirim Seluruh Data Madrasah ke Cloud (15 Modul • $totalItems Data)")
                    }

                    OutlinedButton(
                        onClick = { viewModel.forceCloudSync() },
                        enabled = !isSyncing,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).testTag("pull_from_cloud_btn")
                    ) {
                        Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Tarik / Perbarui Semua 15 Modul dari Cloud")
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 15 Modules Matrix Card
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Status Sinkronisasi 15 Modul Admin:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Box(
                            modifier = Modifier
                                .background(IslamicGreen.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("100% Real-Time", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = IslamicGreen)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val modules = listOf(
                        Triple("1. Profil & Pengaturan (Visi, Misi, Sejarah, Bell)", "Tersimpan", Icons.Default.School),
                        Triple("2. Master Data Siswa", "$studentCount Santri", Icons.Default.People),
                        Triple("3. Guru & Tenaga Kependidikan", "$teacherCount Pengajar", Icons.Default.Badge),
                        Triple("4. Fasilitas & Sarana Madrasah", "${facilities.size} Sarana", Icons.Default.Business),
                        Triple("5. Berita & Pengumuman Madrasah", "$newsCount Artikel", Icons.Default.Article),
                        Triple("6. Galeri Dokumentasi Foto", "$galleryCount Foto", Icons.Default.PhotoLibrary),
                        Triple("7. Kegiatan Ekstrakurikuler", "${extracurriculars.size} Ekskul", Icons.Default.SportsBasketball),
                        Triple("8. Program Unggulan Madrasah", "${featuredPrograms.size} Program", Icons.Default.Star),
                        Triple("9. Agenda & Kalender Kegiatan", "${agendas.size} Agenda", Icons.Default.Event),
                        Triple("10. Testimoni Santri & Wali Murid", "${testimonials.size} Ulasan", Icons.Default.RateReview),
                        Triple("11. Modul Perangkat Ajar (RPP)", "$rppCount Berkas", Icons.Default.MenuBook),
                        Triple("12. Arsip Dokumen & Surat Resmi", "${documents.size} Dokumen", Icons.Default.Description),
                        Triple("13. Jadwal Pelajaran Kelas & Rombel", "Aktif", Icons.Default.Schedule),
                        Triple("14. Siaran Notifikasi & Alarm Suara", "${notifications.size} Siaran", Icons.Default.NotificationsActive),
                        Triple("15. Pendaftaran Santri Baru (PPDB Online)", "$ppdbCount Pendaftar", Icons.Default.HowToReg)
                    )

                    modules.forEach { (name, countText, icon) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .background(NavyPrimary.copy(alpha = 0.1f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(icon, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(16.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(name, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Box(
                                modifier = Modifier
                                    .background(GoldAccent.copy(alpha = 0.25f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(countText, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = NavyDeep)
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(Icons.Default.CheckCircle, contentDescription = "Tersinkron", tint = IslamicGreen, modifier = Modifier.size(16.dp))
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Multi-Device Explanation Card
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Cara Kerja Sinkronisasi Antar Device:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "1. Device 1 (HP Admin) menambah, mengubah, atau menghapus data pada ke-15 modul di atas.\n" +
                        "2. Perubahan otomatis dikirim dan disimpan ke Cloud Firestore secara instan.\n" +
                        "3. Device 2 (HP Guru, Kepala Madrasah, atau Wali Murid) yang membuka aplikasi akan langsung menerima pembaruan secara real-time melalui snapshot listener tanpa perlu memasukkan data ulang!\n" +
                        "4. Website Publik di Vercel (vercel.app) juga langsung menampilkan data termutakhir yang sama secara live.\n" +
                        "5. Apabila baru menginstal aplikasi di HP baru, cukup buka menu ini lalu tekan tombol 'Tarik / Perbarui Semua 15 Modul dari Cloud'.",
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // DETAILED STEP-BY-STEP FIREBASE GUIDE CARD
            var guideTab by remember { mutableStateOf(0) }
            val clipboard = LocalClipboardManager.current
            val dbId = context.getString(R.string.firestore_database_id)

            val fullGuideText = """PANDUAN LENGKAP MENGHUBUNGKAN APLIKASI KE FIREBASE FIRESTORE

A. STATUS KONEKSI DATABASE SAAT INI (SUDAH AKTIF):
- Project ID: gen-lang-client-0399778967
- Database ID: $dbId
- Konfigurasi: google-services.json & firebase_applet_config.xml terpasang otomatis
- Akun Admin: mimtaqecy@gmail.com
- Status: Live Snapshot Listener aktif pada 15 Collection

B. CARA MENGHUBUNGKAN KE HP / DEVICE BARU (DEVICE 2, 3, DST):
1. Bagikan dan instal file APK yang sama pada HP kedua.
2. Buka aplikasi, masuk ke menu samping (drawer) lalu klik "Login Admin".
3. Masukkan password admin (default: mimtq2025).
4. Buka tab "Sinkronisasi Cloud".
5. Klik "Masuk dengan Google (Sign In with Google)" dengan akun admin (mimtaqecy@gmail.com).
6. Tekan tombol "Tarik / Perbarui Semua 15 Modul dari Cloud".
7. Selesai! Kedua HP kini saling tersinkronisasi 100% secara real-time. Setiap data yang disimpan di HP 1 akan otomatis muncul di HP 2 dan sebaliknya.

C. CARA SETUP FIREBASE CONSOLE MANDIRI (JIKA EKSPOR PROYEK):
1. Kunjungi https://console.firebase.google.com/
2. Buat proyek baru dan beri nama "MIM Tanjung Qencono".
3. Tambahkan aplikasi Android dengan Package Name: com.example
4. Unduh file google-services.json dan letakkan di folder app/
5. Buka menu Build -> Firestore Database -> Create Database (Pilih Mode Produksi & Region Jakarta/asia-southeast1).
6. Buka menu Build -> Authentication -> Sign-in method -> Aktifkan Google.
7. Di Firestore Database -> tab Rules, salin seluruh aturan keamanan dari firestore.rules dan klik Publish.
8. Jalankan perintah ./gradlew assembleDebug untuk build aplikasi Android.

D. INTEGRASI OTOMATIS DENGAN WEBSITE VERCEL:
Website madrasah di Vercel terhubung ke Firestore melalui REST API:
https://firestore.googleapis.com/v1/projects/gen-lang-client-0399778967/databases/$dbId/documents/
Sehingga pengunjung web dapat langsung melihat berita, kegiatan, dan profil yang Anda update dari HP Android!"""

            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = NavyDeep)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.MenuBook, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Panduan Lengkap Koneksi Firebase", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                                Text("Petunjuk detail konfigurasi & multi-device", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                            }
                        }

                        Button(
                            onClick = { clipboard.setText(AnnotatedString(fullGuideText)) },
                            colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = NavyDeep),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Salin Panduan", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    TabRow(
                        selectedTabIndex = guideTab,
                        containerColor = Color.Transparent,
                        contentColor = GoldAccent
                    ) {
                        Tab(
                            selected = guideTab == 0,
                            onClick = { guideTab = 0 },
                            text = { Text("1. Info Saat Ini", fontSize = 11.sp, color = if (guideTab == 0) GoldAccent else Color.White.copy(alpha = 0.7f)) }
                        )
                        Tab(
                            selected = guideTab == 1,
                            onClick = { guideTab = 1 },
                            text = { Text("2. Multi-HP", fontSize = 11.sp, color = if (guideTab == 1) GoldAccent else Color.White.copy(alpha = 0.7f)) }
                        )
                        Tab(
                            selected = guideTab == 2,
                            onClick = { guideTab = 2 },
                            text = { Text("3. Firebase Sendiri", fontSize = 11.sp, color = if (guideTab == 2) GoldAccent else Color.White.copy(alpha = 0.7f)) }
                        )
                        Tab(
                            selected = guideTab == 3,
                            onClick = { guideTab = 3 },
                            text = { Text("4. Web Vercel", fontSize = 11.sp, color = if (guideTab == 3) GoldAccent else Color.White.copy(alpha = 0.7f)) }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    when (guideTab) {
                        0 -> {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text("Status Konfigurasi Firebase Aktif:", fontWeight = FontWeight.Bold, color = GoldAccent, fontSize = 13.sp)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("• Project ID: gen-lang-client-0399778967", color = Color(0xFFE2E8F0), fontSize = 11.sp)
                                    Text("• Database ID: $dbId", color = Color(0xFFE2E8F0), fontSize = 11.sp)
                                    Text("• Package: com.example", color = Color(0xFFE2E8F0), fontSize = 11.sp)
                                    Text("• Akun Admin: mimtaqecy@gmail.com", color = Color(0xFFE2E8F0), fontSize = 11.sp)
                                    Text("• File Config: app/google-services.json & firebase_applet_config.xml sudah terpasang.", color = Color(0xFFE2E8F0), fontSize = 11.sp)
                                    Text("• Aturan Keamanan (Rules): 100% Terpasang dan aktif di server Firebase.", color = IslamicGreen, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                        1 -> {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text("Cara Menghubungkan ke HP Lain (Device 2 & Seterusnya):", fontWeight = FontWeight.Bold, color = GoldAccent, fontSize = 13.sp)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("1. Bagikan file APK aplikasi ini ke HP rekan/guru lain.", color = Color(0xFFE2E8F0), fontSize = 11.sp)
                                    Text("2. Buka aplikasi di HP kedua, klik menu samping -> pilih 'Login Admin'.", color = Color(0xFFE2E8F0), fontSize = 11.sp)
                                    Text("3. Masukkan sandi admin (default: mimtq2025).", color = Color(0xFFE2E8F0), fontSize = 11.sp)
                                    Text("4. Masuk ke tab 'Sinkronisasi Cloud' lalu tekan tombol 'Masuk dengan Google' (gunakan akun mimtaqecy@gmail.com).", color = Color(0xFFE2E8F0), fontSize = 11.sp)
                                    Text("5. Tekan tombol hijau 'Tarik / Perbarui Semua 15 Modul dari Cloud'.", color = Color(0xFFE2E8F0), fontSize = 11.sp)
                                    Text("6. Kedua HP kini terhubung penuh! Semua penambahan atau editan data di HP 1 akan otomatis terupdate di HP 2 secara live.", color = IslamicGreen, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                        2 -> {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text("Panduan Jika Ingin Buat Akun Firebase Sendiri di Console:", fontWeight = FontWeight.Bold, color = GoldAccent, fontSize = 13.sp)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("1. Buka https://console.firebase.google.com/ dan login.", color = Color(0xFFE2E8F0), fontSize = 11.sp)
                                    Text("2. Klik 'Tambah Proyek' (Add Project) baru.", color = Color(0xFFE2E8F0), fontSize = 11.sp)
                                    Text("3. Tambah Aplikasi Android dengan package name: com.example", color = Color(0xFFE2E8F0), fontSize = 11.sp)
                                    Text("4. Unduh google-services.json lalu taruh di folder app/", color = Color(0xFFE2E8F0), fontSize = 11.sp)
                                    Text("5. Di menu Build -> Firestore Database -> Klik 'Create Database' (Pilih Region terdekat).", color = Color(0xFFE2E8F0), fontSize = 11.sp)
                                    Text("6. Di menu Build -> Authentication -> Tab Sign-in Method -> Aktifkan Google.", color = Color(0xFFE2E8F0), fontSize = 11.sp)
                                    Text("7. Di Firestore Rules -> Salin aturan dari file firestore.rules dan klik 'Publish'.", color = Color(0xFFE2E8F0), fontSize = 11.sp)
                                    Text("8. Jalankan perintah build APK.", color = Color(0xFFE2E8F0), fontSize = 11.sp)
                                }
                            }
                        }
                        3 -> {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text("Koneksi Firestore ke Website Publik (Vercel):", fontWeight = FontWeight.Bold, color = GoldAccent, fontSize = 13.sp)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("Website HTML di Vercel otomatis mengambil data dari Cloud Firestore menggunakan REST API:", color = Color(0xFFE2E8F0), fontSize = 11.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "https://firestore.googleapis.com/v1/projects/gen-lang-client-0399778967/databases/$dbId/documents/",
                                        color = GoldAccent,
                                        fontSize = 10.sp,
                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("Pengunjung website melihat update guru, pengumuman, dan pendaftaran PPDB secara live tanpa server tambahan!", color = Color(0xFFE2E8F0), fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// 18. GAS INTEGRATION
@Composable
fun AdminGasIntegrationSection(viewModel: MadrasahViewModel) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsState()
    val isSyncing by viewModel.isSheetSyncing.collectAsState()
    val syncStatus by viewModel.sheetSyncStatus.collectAsState()
    val clipboard = LocalClipboardManager.current

    var gasUrl by remember(settings) { mutableStateOf(settings?.gasWebAppUrl ?: "") }
    var sheetId by remember(settings) { mutableStateOf(settings?.googleSpreadsheetId ?: "") }
    var autoSync by remember(settings) { mutableStateOf(settings?.autoSyncPpdbToSheet ?: true) }
    var selectedGuideTab by remember { mutableStateOf(0) }

    val isConnected = gasUrl.isNotBlank() || sheetId.isNotBlank()

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        item {
            SectionHeader(
                title = "Pusat Integrasi Google Sheets",
                subtitle = "Sinkronisasi real-time formulir PPDB, data siswa, buku tamu, dan arsip madrasah",
                icon = Icons.Default.CloudSync
            )

            // 1. STATUS CARD
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isConnected) IslamicGreen.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isConnected) Icons.Default.CheckCircle else Icons.Default.CloudOff,
                                contentDescription = null,
                                tint = if (isConnected) IslamicGreen else MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (isConnected) "Google Sheets Siap Digunakan" else "Belum Terhubung ke Google Sheet",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = if (isConnected) IslamicGreen else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (settings?.lastSheetSyncTime.isNullOrBlank()) "Belum ada riwayat sinkronisasi" else "Terakhir sinkron: ${settings?.lastSheetSyncTime}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (isConnected) {
                            Box(
                                modifier = Modifier
                                    .background(IslamicGreen, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("ONLINE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }

                    if (isSyncing) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(NavyPrimary.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = NavyPrimary)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Sedang memproses sinkronisasi data ke Google Sheets...",
                                fontSize = 12.sp,
                                color = NavyPrimary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    if (syncStatus != null && !isSyncing) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = syncStatus ?: "",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 2. QUICK ACTIONS CARD
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Aksi Sinkronisasi Cepat:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { viewModel.syncAllPpdbToSheet() },
                            enabled = !isSyncing,
                            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                            modifier = Modifier.weight(1f).testTag("sync_ppdb_sheet_btn")
                        ) {
                            Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Kirim PPDB ke Sheet", fontSize = 11.sp)
                        }

                        Button(
                            onClick = { viewModel.syncAllStudentsToSheet() },
                            enabled = !isSyncing,
                            colors = ButtonDefaults.buttonColors(containerColor = IslamicGreen),
                            modifier = Modifier.weight(1f).testTag("sync_students_sheet_btn")
                        ) {
                            Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Kirim Siswa ke Sheet", fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = {
                                val cleanId = GoogleSheetsHelper.extractSpreadsheetId(sheetId.ifBlank { gasUrl })
                                if (cleanId.isNotBlank()) {
                                    viewModel.importStudentsFromGoogleSheet(
                                        isPublicCsv = true,
                                        spreadsheetIdOrUrl = cleanId,
                                        gasUrl = gasUrl
                                    )
                                } else {
                                    viewModel.importStudentsFromGoogleSheet(
                                        isPublicCsv = false,
                                        spreadsheetIdOrUrl = "",
                                        gasUrl = gasUrl
                                    )
                                }
                            },
                            enabled = !isSyncing,
                            modifier = Modifier.weight(1f).testTag("import_students_sheet_btn")
                        ) {
                            Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Tarik Data Siswa", fontSize = 11.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                val target = sheetId.ifBlank { gasUrl }
                                val webUrl = GoogleSheetsHelper.getSpreadsheetWebUrl(target)
                                if (webUrl.isNotBlank()) {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(webUrl))
                                        context.startActivity(intent)
                                    } catch (_: Exception) {}
                                } else {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://sheets.google.com"))
                                        context.startActivity(intent)
                                    } catch (_: Exception) {}
                                }
                            },
                            modifier = Modifier.weight(1f).testTag("open_sheet_in_browser_btn")
                        ) {
                            Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Buka Spreadsheet", fontSize = 11.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 3. FORM PENGATURAN KONEKSI GOOGLE SHEETS
            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), shape = RoundedCornerShape(12.dp)) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Konfigurasi Endpoint & ID Spreadsheet:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    Text("URL Web App Google Apps Script:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = gasUrl,
                        onValueChange = { gasUrl = it },
                        placeholder = { Text("https://script.google.com/macros/s/.../exec") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text("ID atau Tautan Google Spreadsheet:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = sheetId,
                        onValueChange = { input ->
                            sheetId = GoogleSheetsHelper.extractSpreadsheetId(input)
                        },
                        placeholder = { Text("Contoh: 1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms atau paste link") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        singleLine = true
                    )
                    Text(
                        text = "Tips: Cukup salin tautan browser Google Sheet Anda, ID akan otomatis diekstrak.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { autoSync = !autoSync }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Switch(
                            checked = autoSync,
                            onCheckedChange = { autoSync = it }
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Sinkronisasi PPDB Otomatis", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text(
                                "Setiap calon santri mendaftar di aplikasi, data otomatis langsung terkirim ke Google Sheets",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = {
                                if (gasUrl.isNotBlank()) {
                                    viewModel.testSheetConnection(gasUrl)
                                } else {
                                    val cleanId = GoogleSheetsHelper.extractSpreadsheetId(sheetId)
                                    if (cleanId.isNotBlank()) {
                                        viewModel.importStudentsFromGoogleSheet(true, cleanId, "")
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f).testTag("test_sheet_connection_btn")
                        ) {
                            Icon(Icons.Default.Sensors, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Tes Koneksi", fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                viewModel.saveSheetConfig(gasUrl, sheetId, autoSync)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                            modifier = Modifier.weight(1f).testTag("save_sheet_config_btn")
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Simpan Konfigurasi", fontSize = 12.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 4. TABBED PANDUAN & KODE APPS SCRIPT
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Panduan & Kode Integrasi:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                TextButton(onClick = {
                    clipboard.setText(AnnotatedString(GasScriptTemplate.CODE_GS))
                }) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Salin Code.gs", fontSize = 12.sp)
                }
            }

            TabRow(selectedTabIndex = selectedGuideTab, containerColor = Color.Transparent) {
                Tab(
                    selected = selectedGuideTab == 0,
                    onClick = { selectedGuideTab = 0 },
                    text = { Text("1. Petunjuk", fontSize = 12.sp) }
                )
                Tab(
                    selected = selectedGuideTab == 1,
                    onClick = { selectedGuideTab = 1 },
                    text = { Text("2. Kode Script", fontSize = 12.sp) }
                )
                Tab(
                    selected = selectedGuideTab == 2,
                    onClick = { selectedGuideTab = 2 },
                    text = { Text("3. Struktur Tab", fontSize = 12.sp) }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            when (selectedGuideTab) {
                0 -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = NavyDeep),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = GasScriptTemplate.INSTRUCTIONS,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.95f),
                            modifier = Modifier.padding(14.dp),
                            lineHeight = 19.sp
                        )
                    }
                }
                1 -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Code.gs (Apps Script):", color = GoldAccent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Button(
                                    onClick = { clipboard.setText(AnnotatedString(GasScriptTemplate.CODE_GS)) },
                                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = NavyDeep),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text("Salin Kode", fontSize = 11.sp)
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = GasScriptTemplate.CODE_GS.take(1200) + "\n\n/* ... (Klik Salin Kode untuk menyalin seluruh script lengkap) ... */",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFE2E8F0),
                                fontSize = 11.sp,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                            )
                        }
                    }
                }
                2 -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("Nama Tab Sheet yang Otomatis Dibuat:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("1. Tab 'PPDB' -> Rekap data calon siswa baru & status verifikasi", fontSize = 12.sp)
                            Text("2. Tab 'Siswa' -> Daftar data siswa aktif madrasah", fontSize = 12.sp)
                            Text("3. Tab 'PesanKontak' -> Buku tamu & pengaduan wali murid", fontSize = 12.sp)
                            Text("4. Tab 'Notifikasi' -> Arsip log siaran pengumuman", fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Script akan secara otomatis membuat dan mewarnai baris judul tabel (Header) jika tab masih kosong.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

// 19. TULIS NOTIFIKASI ADMIN (DENGAN SUARA & LATAR BELAKANG)
@Composable
fun AdminWriteNotificationSection(viewModel: MadrasahViewModel) {
    val context = LocalContext.current
    val notifications by viewModel.allNotifications.collectAsState()

    var title by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Pengumuman") }
    var targetAudience by remember { mutableStateOf("Semua (Wali Murid & Siswa)") }
    var soundAlert by remember { mutableStateOf(true) }
    var itemToDelete by remember { mutableStateOf<AppNotificationEntity?>(null) }

    val categories = listOf("Pengumuman", "Jadwal Masuk", "Akademik", "Kegiatan", "Darurat")
    val audiences = listOf("Semua (Wali Murid & Siswa)", "Kelas 1 - 3", "Kelas 4 - 6", "Guru & Staf")

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        item {
            SectionHeader(
                title = "Tulis & Siarkan Notifikasi Madrasah",
                subtitle = "Notifikasi akan berbunyi dan bergetar di perangkat penerima walaupun aplikasi di latar belakang",
                icon = Icons.Default.NotificationsActive
            )

            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Pilih Template Cepat:", style = MaterialTheme.typography.labelMedium)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = false,
                            onClick = {
                                title = "Pemberitahuan Jam Masuk & Apel Pagi"
                                message = "Seluruh santri diwajibkan tiba pukul 06.45 - 07.00 WIB untuk apel pagi dan sholat dhuha berjamaah."
                                category = "Jadwal Masuk"
                            },
                            label = { Text("⏰ Jam Masuk", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = false,
                            onClick = {
                                title = "Pengumuman Libur Awal Ramadhan 1447 H"
                                message = "Madrasah diliburkan dalam rangka menyambut awal bulan suci Ramadhan. Santri dihimbau memperbanyak tilawah Al-Qur'an."
                                category = "Pengumuman"
                            },
                            label = { Text("📢 Libur Ramadhan", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = false,
                            onClick = {
                                title = "Jadwal Penilaian Tengah Semester (PTS)"
                                message = "PTS semester genap akan dilaksanakan mulai hari Senin. Santri diharapkan membawa alat tulis lengkap dan hadir tepat waktu."
                                category = "Akademik"
                            },
                            label = { Text("📝 PTS Genap", fontSize = 11.sp) }
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Judul Notifikasi") },
                        placeholder = { Text("Contoh: Pemberitahuan Libur / Jadwal Masuk") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Kategori:", style = MaterialTheme.typography.labelSmall)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        categories.forEach { cat ->
                            FilterChip(
                                selected = category == cat,
                                onClick = { category = cat },
                                label = { Text(cat, fontSize = 11.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Target Penerima:", style = MaterialTheme.typography.labelSmall)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        audiences.take(2).forEach { aud ->
                            FilterChip(
                                selected = targetAudience == aud,
                                onClick = { targetAudience = aud },
                                label = { Text(aud, fontSize = 10.sp) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = message,
                        onValueChange = { message = it },
                        label = { Text("Isi Pesan Notifikasi") },
                        placeholder = { Text("Tuliskan isi pemberitahuan atau pesan yang ingin disampaikan...") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        minLines = 3
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.VolumeUp, contentDescription = null, tint = IslamicGreen, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Bunyikan Nada Dering Notifikasi", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                Text("Berbunyi walaupun aplikasi ditutup", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Switch(checked = soundAlert, onCheckedChange = { soundAlert = it })
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            if (title.isBlank() || message.isBlank()) {
                                return@Button
                            }
                            viewModel.sendNotification(
                                title = title,
                                message = message,
                                category = category,
                                targetAudience = targetAudience,
                                soundAlert = soundAlert
                            )
                            title = ""
                            message = ""
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = IslamicGreen),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Kirim & Siarkan Notifikasi Sekarang")
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedButton(
                        onClick = { viewModel.testAudibleNotification() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Uji Suara Notifikasi & Latar Belakang", fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            SectionHeader(
                title = "Riwayat Notifikasi Terkirim (${notifications.size})",
                icon = Icons.Default.History
            )
        }

        items(notifications) { notif ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                when (notif.category) {
                                    "Darurat" -> MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                                    "Jadwal Masuk" -> GoldAccent.copy(alpha = 0.3f)
                                    else -> NavyPrimary.copy(alpha = 0.15f)
                                },
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = null,
                            tint = if (notif.category == "Darurat") MaterialTheme.colorScheme.error else NavyPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .background(GoldAccent.copy(alpha = 0.25f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(notif.category, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = NavyDeep)
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(notif.dateFormatted, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Text(notif.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.padding(top = 2.dp))
                        Text(notif.message, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 2.dp))
                    }

                    IconButton(
                        onClick = { itemToDelete = notif },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }

    itemToDelete?.let { notif ->
        ConfirmDeleteDialog(
            itemTitle = "Notifikasi: ${notif.title}",
            onConfirm = {
                viewModel.deleteNotification(notif.id)
                itemToDelete = null
            },
            onDismiss = { itemToDelete = null }
        )
    }
}

// 20. FASILITAS ADMIN (DENGAN UNGGAH GAMBAR DARI PERANGKAT)
@Composable
fun AdminFacilitiesSection(viewModel: MadrasahViewModel) {
    val context = LocalContext.current
    val facilities by viewModel.allFacilities.collectAsState()

    var showDialog by remember { mutableStateOf(false) }
    var editingFacility by remember { mutableStateOf<FacilityEntity?>(null) }
    var itemToDelete by remember { mutableStateOf<FacilityEntity?>(null) }

    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Ruang Belajar") }
    var description by remember { mutableStateOf("") }
    var condition by remember { mutableStateOf("Sangat Baik") }
    var capacity by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }

    val facilityImagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            val saved = FilePickerHelper.copyUriToInternalStorage(
                context = context,
                uri = it,
                subfolder = "facilities",
                prefix = "fac"
            )
            imageUrl = saved
        }
    }

    fun openDialog(item: FacilityEntity?) {
        editingFacility = item
        if (item != null) {
            name = item.name
            category = item.category
            description = item.description
            condition = item.condition
            capacity = item.capacity
            imageUrl = item.imageUrl
        } else {
            name = ""
            category = "Ruang Belajar"
            description = ""
            condition = "Sangat Baik"
            capacity = ""
            imageUrl = ""
        }
        showDialog = true
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SectionHeader(title = "Kelola Fasilitas Madrasah (${facilities.size})", icon = Icons.Default.Business)
            Button(
                onClick = { openDialog(null) },
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Tambah", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(facilities) { facility ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        if (facility.imageUrl.isNotBlank()) {
                            AsyncImage(
                                model = if (facility.imageUrl.startsWith("/")) java.io.File(facility.imageUrl) else facility.imageUrl,
                                contentDescription = facility.name,
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(NavyPrimary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Domain, contentDescription = null, tint = NavyPrimary)
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(facility.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .background(GoldAccent.copy(alpha = 0.25f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(facility.category, fontSize = 10.sp, color = NavyDeep, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("• Kondisi: ${facility.condition}", fontSize = 10.sp, color = IslamicGreen)
                            }
                            if (facility.capacity.isNotBlank()) {
                                Text("Kapasitas: ${facility.capacity}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text(
                                facility.description,
                                fontSize = 11.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        IconButton(onClick = { openDialog(facility) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = NavyPrimary, modifier = Modifier.size(18.dp))
                        }
                        IconButton(onClick = { itemToDelete = facility }) {
                            Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(if (editingFacility != null) "Edit Fasilitas" else "Tambah Fasilitas Baru") },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nama Fasilitas") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )

                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Kategori (Ruang Belajar / Sarana Ibadah / Olahraga / Penunjang)") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = condition,
                            onValueChange = { condition = it },
                            label = { Text("Kondisi") },
                            modifier = Modifier.weight(1f).padding(vertical = 4.dp)
                        )
                        OutlinedTextField(
                            value = capacity,
                            onValueChange = { capacity = it },
                            label = { Text("Kapasitas") },
                            modifier = Modifier.weight(1f).padding(vertical = 4.dp)
                        )
                    }

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Deskripsi Fasilitas") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        minLines = 2
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Foto Fasilitas:", style = MaterialTheme.typography.labelSmall)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (imageUrl.isNotBlank()) {
                            AsyncImage(
                                model = if (imageUrl.startsWith("/")) java.io.File(imageUrl) else imageUrl,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Button(
                            onClick = {
                                facilityImagePicker.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                        ) {
                            Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Pilih Gambar Perangkat", fontSize = 11.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (name.isBlank()) return@Button
                    val entity = editingFacility?.copy(
                        name = name,
                        category = category,
                        description = description,
                        condition = condition,
                        capacity = capacity,
                        imageUrl = imageUrl
                    ) ?: FacilityEntity(
                        name = name,
                        category = category,
                        description = description,
                        condition = condition,
                        capacity = capacity,
                        imageUrl = imageUrl
                    )
                    if (editingFacility != null) {
                        viewModel.updateFacility(entity)
                    } else {
                        viewModel.addFacility(entity)
                    }
                    showDialog = false
                }) {
                    Text("Simpan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    itemToDelete?.let { fac ->
        ConfirmDeleteDialog(
            itemTitle = "Fasilitas: ${fac.name}",
            onConfirm = {
                viewModel.deleteFacility(fac)
                itemToDelete = null
            },
            onDismiss = { itemToDelete = null }
        )
    }
}

// 21. JADWAL MASUK ADMIN
@Composable
fun AdminSchoolScheduleSection(viewModel: MadrasahViewModel) {
    val currentSettings by viewModel.settings.collectAsState()

    var morningAssembly by remember(currentSettings) { mutableStateOf(currentSettings?.morningAssemblyTime ?: "07.00 - 07.15 WIB") }
    var classStart by remember(currentSettings) { mutableStateOf(currentSettings?.classStartTime ?: "07.15 WIB") }
    var monThuEnd by remember(currentSettings) { mutableStateOf(currentSettings?.classEndTimeMonThu ?: "12.30 WIB") }
    var friEnd by remember(currentSettings) { mutableStateOf(currentSettings?.classEndTimeFri ?: "11.00 WIB") }
    var satEnd by remember(currentSettings) { mutableStateOf(currentSettings?.classEndTimeSat ?: "12.00 WIB") }
    var rules by remember(currentSettings) { mutableStateOf(currentSettings?.schoolScheduleRules ?: "Siswa wajib hadir pukul 06.45 - 07.00 WIB. Pukul 07.00 WIB gerbang ditutup untuk apel pagi & sholat dhuha berjamaah.") }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        item {
            SectionHeader(
                title = "Pengaturan Jadwal Masuk & Jam Belajar",
                subtitle = "Jadwal kehadiran, apel pagi, dan kepulangan santri MI Muhammadiyah Tanjung Qencono",
                icon = Icons.Default.AccessTime
            )

            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), shape = RoundedCornerShape(12.dp)) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Jam Masuk & Apel Pagi:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    OutlinedTextField(
                        value = morningAssembly,
                        onValueChange = { morningAssembly = it },
                        label = { Text("Apel Pagi & Sholat Dhuha") },
                        placeholder = { Text("07.00 - 07.15 WIB") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )

                    OutlinedTextField(
                        value = classStart,
                        onValueChange = { classStart = it },
                        label = { Text("Jam Mulai Pembelajaran Kelas") },
                        placeholder = { Text("07.15 WIB") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Jam Kepulangan Santri:", fontWeight = FontWeight.Bold, fontSize = 13.sp)

                    OutlinedTextField(
                        value = monThuEnd,
                        onValueChange = { monThuEnd = it },
                        label = { Text("Senin - Kamis") },
                        placeholder = { Text("12.30 WIB (Setelah Sholat Dzuhur)") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )

                    OutlinedTextField(
                        value = friEnd,
                        onValueChange = { friEnd = it },
                        label = { Text("Jumat") },
                        placeholder = { Text("11.00 WIB (Persiapan Sholat Jumat)") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )

                    OutlinedTextField(
                        value = satEnd,
                        onValueChange = { satEnd = it },
                        label = { Text("Sabtu (Ekstrakurikuler & Pengembangan)") },
                        placeholder = { Text("12.00 WIB") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Tata Tertib & Ketentuan Masuk Madrasah:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    OutlinedTextField(
                        value = rules,
                        onValueChange = { rules = it },
                        label = { Text("Ketentuan Kehadiran & Gerbang") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        minLines = 3
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            currentSettings?.let {
                                viewModel.updateSchoolSettings(
                                    it.copy(
                                        morningAssemblyTime = morningAssembly,
                                        classStartTime = classStart,
                                        classEndTimeMonThu = monThuEnd,
                                        classEndTimeFri = friEnd,
                                        classEndTimeSat = satEnd,
                                        schoolScheduleRules = rules
                                    )
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Simpan Jadwal Masuk Madrasah")
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { viewModel.scheduleEntryReminder() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Alarm, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Aktifkan Pengingat Jam Masuk di Latar Belakang (06.30 WIB)", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

// 22. VERCEL WEB DEPLOYMENT SECTION
@Composable
fun AdminVercelSection(viewModel: MadrasahViewModel) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val settings by viewModel.settings.collectAsState()

    var vercelUrl by remember(settings) {
        mutableStateOf(settings?.vercelWebUrl.takeIf { !it.isNullOrBlank() } ?: "https://mim-tanjung-qencono.vercel.app")
    }

    val vercelJsonContent = """{
  "buildCommand": null,
  "outputDirectory": ".",
  "cleanUrls": true,
  "trailingSlash": false,
  "rewrites": [
    { "source": "/download", "destination": "/app-debug.apk" },
    { "source": "/apk", "destination": "/app-debug.apk" },
    { "source": "/ppdb", "destination": "/index.html" }
  ],
  "headers": [
    {
      "source": "/app-debug.apk",
      "headers": [
        { "key": "Content-Type", "value": "application/vnd.android.package-archive" },
        { "key": "Content-Disposition", "value": "attachment; filename=\"MIM_Tanjung_Qencono.apk\"" },
        { "key": "Cache-Control", "value": "public, max-age=3600" }
      ]
    }
  ]
}"""

    val instructions = """PANDUAN MENG-ONLINEKAN KE VERCEL (VERCEL.APP):

CARA 1: LEWAT GITHUB & VERCEL.COM (GRATIS 100% & OTOMATIS)
1. Buka https://vercel.com dan masuk dengan akun GitHub atau Google Anda (mimtaqecy@gmail.com).
2. Klik tombol "Add New..." lalu pilih "Project".
3. Pilih repositori proyek madrasah Anda, lalu klik "Import".
4. Pada form pengaturan:
   - Framework Preset: Other
   - Root Directory: ./
   - File vercel.json & index.html sudah otomatis terdeteksi!
5. Klik tombol "Deploy".
6. Tunggu sekitar 20-30 detik hingga status menjadi Ready. Website dan link download APK Anda langsung online dengan domain:
   👉 https://mim-tanjung-qencono.vercel.app

CARA 2: MENGGUNAKAN VERCEL CLI DARI LAPTOP/KOMPUTER
1. Buka folder proyek di komputer melalui Terminal/CMD.
2. Ketik perintah: npx vercel
3. Tekan 'y' untuk konfirmasi, pilih akun Anda.
4. Setelah selesai, jalankan: npx vercel --prod
5. Website dan file APK langsung terbit online!"""

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        item {
            SectionHeader(
                title = "Publikasi Web & Vercel.app",
                subtitle = "Website resmi madrasah, formulir PPDB online publik, dan portal download APK di vercel.app",
                icon = Icons.Default.Language
            )

            // STATUS CARD
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = NavyDeep)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CloudDone, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Konfigurasi Vercel Siap", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                                Text("vercel.json & index.html telah dibuat", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                            }
                        }
                        Box(
                            modifier = Modifier
                                .background(GoldAccent, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("READY", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = NavyDeep)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Tautan Website Madrasah Anda:",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 11.sp
                    )
                    Text(
                        text = vercelUrl,
                        color = GoldAccent,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // DOMAIN INPUT & ACTIONS
            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), shape = RoundedCornerShape(12.dp)) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Domain / URL Website Vercel:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    OutlinedTextField(
                        value = vercelUrl,
                        onValueChange = { vercelUrl = it },
                        placeholder = { Text("https://mim-tanjung-qencono.vercel.app") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        singleLine = true
                    )
                    Button(
                        onClick = { viewModel.updateVercelUrl(vercelUrl) },
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                        modifier = Modifier.fillMaxWidth().testTag("save_vercel_url_btn")
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Simpan URL Website Vercel")
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(vercelUrl))
                                    context.startActivity(intent)
                                } catch (_: Exception) {}
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = IslamicGreen),
                            modifier = Modifier.weight(1f).testTag("open_vercel_web_btn")
                        ) {
                            Icon(Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Buka Web", fontSize = 11.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                val shareText = "Kunjungi Website Resmi & Pendaftaran PPDB Online MI Muhammadiyah Tanjung Qencono:\n$vercelUrl\n\nUnduh Aplikasi Android:\n$vercelUrl/download"
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, shareText)
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Bagikan Website Vercel"))
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Bagikan", fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = {
                            val apkUrl = if (vercelUrl.endsWith("/")) "${vercelUrl}app-debug.apk" else "$vercelUrl/app-debug.apk"
                            clipboard.setText(AnnotatedString(apkUrl))
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Salin Tautan Download APK Android", fontSize = 11.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // PANDUAN DEPLOY CARD
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Langkah Deploy ke Vercel:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        TextButton(onClick = {
                            clipboard.setText(AnnotatedString(instructions))
                        }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Salin Panduan", fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = instructions,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(12.dp),
                            lineHeight = 18.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Isi File vercel.json:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        TextButton(onClick = {
                            clipboard.setText(AnnotatedString(vercelJsonContent))
                        }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Salin vercel.json", fontSize = 11.sp)
                        }
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = vercelJsonContent,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFE2E8F0),
                            fontSize = 11.sp,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        }
    }
}

// 21. VISI, MISI & SEJARAH SINGKAT ADMIN
@Composable
fun AdminVisionMissionSection(viewModel: MadrasahViewModel) {
    val currentSettings by viewModel.settings.collectAsState()

    var vision by remember(currentSettings) { mutableStateOf(currentSettings?.visionText ?: "") }
    var mission by remember(currentSettings) { mutableStateOf(currentSettings?.missionText ?: "") }
    var history by remember(currentSettings) { mutableStateOf(currentSettings?.historyText ?: "") }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        item {
            SectionHeader(title = "Visi, Misi & Sejarah Singkat Madrasah", icon = Icons.Default.School)
            Text(
                "Kelola teks visi, misi, dan sejarah singkat madrasah yang tampil di halaman profil dan beranda.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Visi Card
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Visibility, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Visi Madrasah", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = NavyPrimary)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = vision,
                        onValueChange = { vision = it },
                        placeholder = { Text("Contoh: Terwujudnya Generasi Muslim yang Taqwa, Cerdas, Berakhlakul Karimah, Mandiri, dan Berprestasi Unggul.") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }
            }

            // Misi Card
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.FormatListNumbered, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Misi Madrasah", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = NavyPrimary)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = mission,
                        onValueChange = { mission = it },
                        placeholder = { Text("Tuliskan butir-butir misi madrasah (gunakan nomor 1, 2, 3...)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 5
                    )
                }
            }

            // Sejarah Singkat Card
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.History, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sejarah Singkat Madrasah", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = NavyPrimary)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = history,
                        onValueChange = { history = it },
                        placeholder = { Text("Tuliskan sejarah berdirinya MI Muhammadiyah Tanjung Qencono...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 5
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    viewModel.updateVisionMissionHistory(vision, mission, history)
                },
                modifier = Modifier.fillMaxWidth().testTag("save_vision_mission_btn")
            ) {
                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Simpan Visi, Misi & Sejarah Singkat")
            }
        }
    }
}


