package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.NewsEntity
import com.example.ui.components.AppFooter
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatCard
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.IslamicGreen
import com.example.ui.theme.NavyDeep
import com.example.ui.theme.NavyPrimary
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.MadrasahViewModel
import coil.compose.AsyncImage
import java.io.File

@Composable
fun HomeScreen(viewModel: MadrasahViewModel) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsState()
    val studentCount by viewModel.studentCount.collectAsState()
    val teacherCount by viewModel.teacherCount.collectAsState()
    val newsList by viewModel.publishedNews.collectAsState()
    val agendaList by viewModel.publishedAgendas.collectAsState()
    val programs by viewModel.featuredPrograms.collectAsState()
    val extras by viewModel.extracurriculars.collectAsState()
    val testimonials by viewModel.approvedTestimonials.collectAsState()
    val galleryList by viewModel.publishedGallery.collectAsState()
    val unreadNotifCount by viewModel.unreadNotificationCount.collectAsState()
    val facilities by viewModel.publishedFacilities.collectAsState()

    var showTestimonialDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // HERO SECTION
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(380.dp)
        ) {
            // Background Image
            Image(
                painter = painterResource(id = R.drawable.hero_madrasah_1792255583606),
                contentDescription = "Gedung Madrasah",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            // Gradient Overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                NavyDeep.copy(alpha = 0.75f),
                                NavyDeep.copy(alpha = 0.95f)
                            )
                        )
                    )
            )

            // Hero Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_madrasah_official_logo),
                    contentDescription = "Logo Resmi MI Muhammadiyah",
                    modifier = Modifier
                        .size(76.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = settings?.schoolName ?: "MI MUHAMMADIYAH TANJUNG QENCONO",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 19.sp,
                        letterSpacing = 0.5.sp
                    ),
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "“${settings?.slogan ?: "Religi, Cerdas, dan Berprestasi"}”",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp
                    ),
                    color = GoldAccent,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp)
                )

                // Badge Identitas Resmi Madrasah (Status, Akreditasi, Tahun, NPSN & NSM)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        modifier = Modifier
                            .padding(top = 6.dp)
                            .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(settings?.schoolStatus ?: "Swasta (Diakui)", color = GoldAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("•", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                        Text(settings?.accreditation ?: "Akreditasi B", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("•", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                        Text("T.A: ${settings?.academicYear ?: "2025/2026"}", color = GoldAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("•", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                        Text("Est. ${settings?.foundedYear ?: "1988"}", color = Color.White, fontSize = 11.sp)
                    }

                    Row(
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 10.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("NPSN: ${settings?.npsn?.ifBlank { "60721234" } ?: "60721234"}", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                        Text("•", color = GoldAccent, fontSize = 10.sp)
                        Text("NSM: ${settings?.nsm?.ifBlank { "111218070001" } ?: "111218070001"}", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                Text(
                    text = "Mencetak Generasi Muslim Berakhlakul Karimah, Mandiri, Berprestasi, dan Berkemajuan di Lampung Timur",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    modifier = Modifier.padding(top = 6.dp, start = 8.dp, end = 8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Hero Buttons Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(
                        onClick = { viewModel.navigateTo(AppScreen.PROFILE) },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = NavyDeep),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.padding(horizontal = 4.dp).testTag("btn_lihat_profil")
                    ) {
                        Text("Lihat Profil", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    Button(
                        onClick = { viewModel.navigateTo(AppScreen.PPDB) },
                        colors = ButtonDefaults.buttonColors(containerColor = IslamicGreen, contentColor = Color.White),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.padding(horizontal = 4.dp).testTag("btn_daftar_ppdb")
                    ) {
                        Text("Daftar PPDB", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    // Tombol Ujian (External GAS URL as requested in prompt)
                    OutlinedButton(
                        onClick = {
                            val examUrl = settings?.examUrl ?: "https://script.google.com/macros/s/AKfycbydGFMEnESdvngL0n7v2T0snZr8WmT4cHV0BmtjQhommDpqjTg-tj8Wit6eDPtBFuK3/exec"
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(examUrl))
                            context.startActivity(intent)
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.padding(horizontal = 4.dp).testTag("btn_ujian_online")
                    ) {
                        Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Tombol Ujian", fontSize = 11.sp)
                    }

                    OutlinedButton(
                        onClick = { showTestimonialDialog = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldAccent),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.padding(horizontal = 4.dp).testTag("btn_tambah_testimoni")
                    ) {
                        Icon(Icons.Default.RateReview, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Tambah Testimoni", fontSize = 11.sp)
                    }
                }
            }
        }

        // PEMBERITAHUAN NOTIFIKASI BARU (LONCENG ALERT BANNER)
        if (unreadNotifCount > 0) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clickable { viewModel.navigateTo(AppScreen.NOTIFICATIONS) }
                    .testTag("home_notif_banner"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = GoldAccent.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(NavyPrimary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Pemberitahuan Madrasah ($unreadNotifCount Baru)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NavyDeep)
                        Text("Ketuk untuk membaca pengumuman & jadwal masuk terbaru", fontSize = 11.sp, color = NavyDeep.copy(alpha = 0.8f))
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = NavyDeep)
                }
            }
        }

        // MENU AKSES CEPAT HALAMAN DEPAN (Fasilitas, Jadwal Masuk, Notifikasi)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Notifikasi Bell
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { viewModel.navigateTo(AppScreen.NOTIFICATIONS) }
                        .testTag("quick_menu_notif")
                ) {
                    BadgedBox(
                        badge = {
                            if (unreadNotifCount > 0) {
                                Badge(
                                    containerColor = MaterialTheme.colorScheme.error,
                                    contentColor = Color.White
                                ) {
                                    Text("$unreadNotifCount")
                                }
                            }
                        }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(NavyPrimary.copy(alpha = 0.12f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Notifications, contentDescription = "Notifikasi", tint = NavyPrimary)
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Notifikasi", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }

                // Fasilitas
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { viewModel.navigateTo(AppScreen.FACILITIES) }
                        .testTag("quick_menu_facilities")
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(IslamicGreen.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Business, contentDescription = "Fasilitas", tint = IslamicGreen)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Fasilitas", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }

                // Jadwal Masuk
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { viewModel.navigateTo(AppScreen.SCHOOL_SCHEDULE) }
                        .testTag("quick_menu_schedule")
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(GoldAccent.copy(alpha = 0.25f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.AccessTime, contentDescription = "Jadwal Masuk", tint = NavyDeep)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Jadwal Masuk", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }

                // PPDB Online
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { viewModel.navigateTo(AppScreen.PPDB) }
                        .testTag("quick_menu_ppdb")
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.AppRegistration, contentDescription = "PPDB", tint = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("PPDB 2026", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // STATISTIK DINAMIS & OFFLINE CACHE
        Column(modifier = Modifier.padding(16.dp)) {
            com.example.ui.components.OfflineCacheBadge(
                title = "Database Lokal Room: Mode Offline Aktif",
                subtitle = "Data siswa, profil dewan guru, dan berita madrasah tersimpan permanen di perangkat",
                modifier = Modifier.padding(bottom = 8.dp)
            )

            SectionHeader(title = "Statistik Madrasah", subtitle = "Data riil terintegrasi database madrasah")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "Jumlah Siswa",
                    value = studentCount.toString(),
                    icon = Icons.Default.Groups,
                    modifier = Modifier.weight(1f)
                ) {
                    viewModel.navigateTo(AppScreen.STUDENT_DATA)
                }
                StatCard(
                    title = "Dewan Guru",
                    value = teacherCount.toString(),
                    icon = Icons.Default.Psychology,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.weight(1f)
                ) {
                    viewModel.navigateTo(AppScreen.PROFILE)
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "Tingkat Kelas",
                    value = "6 Kelas",
                    icon = Icons.Default.Class,
                    modifier = Modifier.weight(1f)
                ) {
                    viewModel.navigateTo(AppScreen.ACADEMIC)
                }
                StatCard(
                    title = "Tahun Berdiri",
                    value = settings?.foundedYear ?: "1988",
                    icon = Icons.Default.History,
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.weight(1f)
                ) {
                    viewModel.navigateTo(AppScreen.PROFILE)
                }
            }
        }

        // TAUSIYAH SINGKAT
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(14.dp)
        ) {
            Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(GoldAccent.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.MenuBook, contentDescription = null, tint = GoldAccent)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Tausiyah Singkat Harian",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = settings?.dailyTausiyah ?: "Menuntut ilmu adalah kewajiban bagi setiap muslim. (HR. Muslim)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // SAMBUTAN KEPALA MADRASAH
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val kamadPhoto = settings?.headmasterPhotoUri ?: ""
                    if (kamadPhoto.isNotBlank()) {
                        AsyncImage(
                            model = if (kamadPhoto.startsWith("/")) File(kamadPhoto) else kamadPhoto,
                            contentDescription = "Foto Kepala Madrasah",
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(NavyPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(36.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "Sambutan Kepala Madrasah",
                            style = MaterialTheme.typography.labelMedium,
                            color = GoldAccent,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = settings?.headmasterName ?: "Ahmad Syaifuddin, S.Pd.I",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = settings?.headmasterSpeech ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // KEUNGGULAN & PROGRAM UNGGULAN
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            SectionHeader(title = "Program Unggulan", subtitle = "Fasilitas & program keilmuan berkemajuan")
            programs.take(4).forEach { prog ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        if (prog.imageUrl.isNotBlank()) {
                            AsyncImage(
                                model = if (prog.imageUrl.startsWith("/")) File(prog.imageUrl) else prog.imageUrl,
                                contentDescription = prog.name,
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Stars, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = prog.name,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = prog.objective,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // EKSTRAKURIKULER
        Column(modifier = Modifier.padding(16.dp)) {
            SectionHeader(title = "Ekstrakurikuler", subtitle = "Wadah minat, bakat, kepanduan, dan beladiri santri")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(extras) { item ->
                    Card(
                        modifier = Modifier
                            .width(220.dp)
                            .height(130.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(12.dp)
                                .fillMaxSize(),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = item.name,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Column {
                                Text(
                                    text = "Pembina: ${item.coach}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = item.schedule,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // BERITA TERBARU
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            SectionHeader(title = "Berita & Informasi", subtitle = "Kabar terkini seputar kegiatan madrasah")
            newsList.take(3).forEach { news ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clickable { viewModel.navigateTo(AppScreen.NEWS) },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(GoldAccent.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(news.category, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NavyDeep)
                            }
                            Text(news.date, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth()) {
                            if (news.imageUrl.isNotBlank()) {
                                AsyncImage(
                                    model = if (news.imageUrl.startsWith("/")) File(news.imageUrl) else news.imageUrl,
                                    contentDescription = news.title,
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = news.title,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = news.summary,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }

        // AGENDA KEGIATAN
        Column(modifier = Modifier.padding(16.dp)) {
            SectionHeader(title = "Agenda Madrasah", subtitle = "Jadwal kegiatan akademik & keislaman mendatang")
            agendaList.take(3).forEach { agenda ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (agenda.imageUrl.isNotBlank()) {
                            AsyncImage(
                                model = if (agenda.imageUrl.startsWith("/")) File(agenda.imageUrl) else agenda.imageUrl,
                                contentDescription = agenda.title,
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = Color.White)
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(agenda.title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                            Text("${agenda.date} • ${agenda.time}", style = MaterialTheme.typography.labelSmall, color = GoldAccent)
                            Text(agenda.location, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        // TESTIMONI WALI MURID & MASYARAKAT
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionHeader(title = "Testimoni", subtitle = "Kata mereka tentang MI Muhammadiyah", modifier = Modifier.weight(1f))
                TextButton(onClick = { showTestimonialDialog = true }) {
                    Text("+ Tulis Ulasan", fontWeight = FontWeight.Bold)
                }
            }

            if (testimonials.isEmpty()) {
                Text(
                    text = "Belum ada testimoni yang disetujui. Jadilah yang pertama memberikan ulasan!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(testimonials) { testi ->
                        Card(
                            modifier = Modifier
                                .width(280.dp)
                                .height(150.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(14.dp)
                                    .fillMaxSize(),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    repeat(testi.rating) {
                                        Icon(Icons.Default.Star, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(16.dp))
                                    }
                                }
                                Text(
                                    text = "“${testi.content}”",
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Column {
                                    Text(testi.name, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                                    Text(testi.role, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        }

        // CTA PPDB BANNER
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = NavyPrimary)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Ayo Bergabung Bersama Kami!",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Text(
                    text = "Penerimaan Peserta Didik Baru (PPDB) TP 2026/2027 telah dibuka. Kuota terbatas untuk kelas 1.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Button(
                    onClick = { viewModel.navigateTo(AppScreen.PPDB) },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = NavyDeep),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Formulir PPDB Online Sekarang", fontWeight = FontWeight.Bold)
                }
            }
        }

        // MEDIA SOSIAL RESMI MADRASAH (BAGIAN BAWAH HALAMAN DEPAN)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            SectionHeader(
                title = "Media Sosial Resmi",
                subtitle = "Ikuti publikasi kegiatan, siaran langsung, dan dokumentasi santri"
            )

            // Facebook Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable {
                        val fbIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/share/1DxkoYBNkL/"))
                        context.startActivity(fbIntent)
                    }
                    .testTag("btn_social_facebook"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1877F2).copy(alpha = 0.1f))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color(0xFF1877F2), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("f", color = Color.White, fontWeight = FontWeight.Black, fontSize = 26.sp)
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Facebook Resmi",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF1877F2)
                        )
                        Text(
                            text = "MI Muhammadiyah Tanjung Qencono",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.OpenInNew,
                        contentDescription = "Buka Facebook",
                        tint = Color(0xFF1877F2),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Instagram Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable {
                        val igIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.instagram.com/mim_taqecy?rpxt=MXg5NG1saWZ2Z2l4MQ=="))
                        context.startActivity(igIntent)
                    }
                    .testTag("btn_social_instagram"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE1306C).copy(alpha = 0.1f))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF833AB4), Color(0xFFFD1D1D), Color(0xFFF77737))
                                ),
                                RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Instagram",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Instagram Resmi",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFFE1306C)
                        )
                        Text(
                            text = "@mim_taqecy",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.OpenInNew,
                        contentDescription = "Buka Instagram",
                        tint = Color(0xFFE1306C),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // TikTok Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable {
                        val ttIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.tiktok.com/@mim_tanjung.qencono?_r=1&_t=ZS-9AiTueWtGGj"))
                        context.startActivity(ttIntent)
                    }
                    .testTag("btn_social_tiktok"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A).copy(alpha = 0.08f))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color(0xFF010101), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("♪", color = Color(0xFF25F4EE), fontWeight = FontWeight.Black, fontSize = 24.sp)
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "TikTok Resmi",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = NavyDeep
                        )
                        Text(
                            text = "@mim_tanjung.qencono",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.OpenInNew,
                        contentDescription = "Buka TikTok",
                        tint = NavyDeep,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // FOOTER
        AppFooter(viewModel = viewModel)
    }

    // Modal dialog tambah testimoni
    if (showTestimonialDialog) {
        var tName by remember { mutableStateOf("") }
        var tRole by remember { mutableStateOf("Wali Murid") }
        var tContent by remember { mutableStateOf("") }
        var tRating by remember { mutableStateOf(5) }

        AlertDialog(
            onDismissRequest = { showTestimonialDialog = false },
            title = { Text("Tulis Testimoni") },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = tName,
                        onValueChange = { tName = it },
                        label = { Text("Nama Anda") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )
                    OutlinedTextField(
                        value = tRole,
                        onValueChange = { tRole = it },
                        label = { Text("Status (Wali Santri / Alumni)") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )
                    OutlinedTextField(
                        value = tContent,
                        onValueChange = { tContent = it },
                        label = { Text("Ulasan / Testimoni Anda") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        minLines = 3
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Rating Bintang:", style = MaterialTheme.typography.labelMedium)
                    Row {
                        (1..5).forEach { star ->
                            IconButton(onClick = { tRating = star }) {
                                Icon(
                                    imageVector = if (star <= tRating) Icons.Default.Star else Icons.Default.StarBorder,
                                    contentDescription = "$star Bintang",
                                    tint = GoldAccent
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (tName.isNotBlank() && tContent.isNotBlank()) {
                            viewModel.submitTestimonial(tName, tRole, tContent, tRating)
                            showTestimonialDialog = false
                        }
                    }
                ) {
                    Text("Kirim Ulasan")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showTestimonialDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}
