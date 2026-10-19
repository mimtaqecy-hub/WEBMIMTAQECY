package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.components.AppFooter
import com.example.ui.components.SectionHeader
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.IslamicGreen
import com.example.ui.theme.NavyDeep
import com.example.ui.theme.NavyPrimary
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.MadrasahViewModel
import java.io.File

@Composable
fun ProfileScreen(viewModel: MadrasahViewModel) {
    val settings by viewModel.settings.collectAsState()
    val teachers by viewModel.teachers.collectAsState()
    val facilities by viewModel.publishedFacilities.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Identitas & Sejarah", "Visi Misi", "Dewan Guru", "Fasilitas")

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            // Header Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NavyPrimary)
                    .padding(horizontal = 20.dp, vertical = 24.dp)
            ) {
                Column {
                    Text(
                        text = "Profil Madrasah",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Text(
                        text = "${settings?.schoolName ?: "MI Muhammadiyah Tanjung Qencono"}, Lampung Timur",
                        style = MaterialTheme.typography.bodyMedium,
                        color = GoldAccent
                    )
                }
            }

            // Tab Row
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                edgePadding = 16.dp,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
            }
        }

        when (selectedTab) {
            0 -> {
                // Identitas & Sejarah
                item {
                    Column(modifier = Modifier.padding(16.dp)) {
                        SectionHeader(title = "Identitas & Legalitas Madrasah", icon = Icons.Default.Badge)
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                ProfileRow("Nama Madrasah", settings?.schoolName ?: "MI MUHAMMADIYAH TANJUNG QENCONO")
                                ProfileRow("NPSN", settings?.npsn?.ifBlank { "60721234" } ?: "60721234")
                                ProfileRow("NSM", settings?.nsm?.ifBlank { "111218070001" } ?: "111218070001")
                                ProfileRow("Status Madrasah", settings?.schoolStatus?.ifBlank { "Swasta (Diakui Dikdasmen Muhammadiyah)" } ?: "Swasta (Diakui Dikdasmen Muhammadiyah)")
                                ProfileRow("Akreditasi", settings?.accreditation?.ifBlank { "B (Terakreditasi Baik Sekali)" } ?: "B (Terakreditasi Baik Sekali)")
                                ProfileRow("Tahun Berdiri", settings?.foundedYear?.ifBlank { "1988" } ?: "1988")
                                ProfileRow("Tahun Pelajaran", settings?.academicYear?.ifBlank { "2025/2026" } ?: "2025/2026")
                                ProfileRow("Alamat Lengkap", settings?.address ?: "Tanjung Qencono, Way Bungur, Lampung Timur")
                                ProfileRow("Telepon/WA", settings?.phoneWa ?: "0853-7802-9852")
                                ProfileRow("Email Resmi", settings?.email ?: "mimtaqecy@gmail.com")
                                ProfileRow("Kepala Madrasah", settings?.headmasterName ?: "Ahmad Syaifuddin, S.Pd.I")
                            }
                        }

                        // Kepala Madrasah Profile Card with uploaded photo
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                val kamadPhoto = settings?.headmasterPhotoUri ?: ""
                                if (kamadPhoto.isNotBlank()) {
                                    AsyncImage(
                                        model = if (kamadPhoto.startsWith("/")) File(kamadPhoto) else kamadPhoto,
                                        contentDescription = "Foto Kepala Madrasah",
                                        modifier = Modifier
                                            .size(68.dp)
                                            .clip(RoundedCornerShape(12.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(68.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(NavyPrimary),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(38.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column {
                                    Text("Kepala Madrasah", fontSize = 11.sp, color = GoldAccent, fontWeight = FontWeight.Bold)
                                    Text(settings?.headmasterName ?: "Ahmad Syaifuddin, S.Pd.I", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Text("MI Muhammadiyah Tanjung Qencono", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        SectionHeader(title = "Sejarah Singkat", icon = Icons.Default.History)
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Text(
                                text = settings?.historyText ?: "",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(16.dp),
                                lineHeight = 22.sp
                            )
                        }
                    }
                }
            }
            1 -> {
                // Visi & Misi & Tujuan
                item {
                    Column(modifier = Modifier.padding(16.dp)) {
                        SectionHeader(title = "Visi Madrasah", icon = Icons.Default.Visibility)
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            Text(
                                text = "“${settings?.visionText ?: ""}”",
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(16.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        SectionHeader(title = "Misi Madrasah", icon = Icons.Default.Flag)
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = settings?.missionText ?: "",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(16.dp),
                                lineHeight = 22.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        SectionHeader(title = "Tujuan Pendidikan", icon = Icons.Default.GpsFixed)
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Text(
                                text = settings?.goalsText ?: "",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(16.dp),
                                lineHeight = 22.sp
                            )
                        }
                    }
                }
            }
            2 -> {
                // Dewan Guru & Staf
                item {
                    Column(modifier = Modifier.padding(16.dp)) {
                        SectionHeader(title = "Pendidik & Tenaga Kependidikan", subtitle = "Guru berdedikasi tinggi membina akhlak dan akademik santri")
                        com.example.ui.components.OfflineCacheBadge(
                            title = "Profil Guru Tersimpan di Cache Room",
                            subtitle = "Biodata dan bidang pengampu tetap dapat dilihat secara offline tanpa koneksi internet",
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
                items(teachers) { teacher ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            if (teacher.photoUrl.isNotBlank()) {
                                AsyncImage(
                                    model = if (teacher.photoUrl.startsWith("/")) File(teacher.photoUrl) else teacher.photoUrl,
                                    contentDescription = teacher.name,
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surfaceVariant),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(CircleShape)
                                        .background(NavyPrimary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = teacher.name,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = teacher.role,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = GoldAccent
                                )
                                Text(
                                    text = "Mapel: ${teacher.subject}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                if (teacher.description.isNotEmpty()) {
                                    Text(
                                        text = teacher.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            3 -> {
                // Fasilitas
                item {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SectionHeader(title = "Sarana & Prasarana Madrasah", subtitle = "Fasilitas lengkap mendukung santri berprestasi")
                            TextButton(onClick = { viewModel.navigateTo(AppScreen.FACILITIES) }) {
                                Text("Lihat Penuh", fontSize = 12.sp)
                                Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
                items(facilities) { fac ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (fac.imageUrl.isNotBlank()) {
                                AsyncImage(
                                    model = if (fac.imageUrl.startsWith("/")) File(fac.imageUrl) else fac.imageUrl,
                                    contentDescription = fac.name,
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(NavyPrimary.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = GoldAccent)
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(fac.name, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .background(GoldAccent.copy(alpha = 0.25f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 1.dp)
                                    ) {
                                        Text(fac.category, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = NavyDeep)
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Kondisi: ${fac.condition}", fontSize = 10.sp, color = IslamicGreen)
                                }
                                Text(
                                    fac.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
            AppFooter(viewModel = viewModel)
        }
    }
}

@Composable
fun ProfileRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium))
        HorizontalDivider(modifier = Modifier.padding(top = 4.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    }
}
