package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AppFooter
import com.example.ui.components.SectionHeader
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.IslamicGreen
import com.example.ui.theme.NavyDeep
import com.example.ui.theme.NavyPrimary
import com.example.ui.viewmodel.MadrasahViewModel

@Composable
fun SchoolScheduleScreen(viewModel: MadrasahViewModel) {
    val settings by viewModel.settings.collectAsState()

    val morningAssembly = settings?.morningAssemblyTime ?: "07.00 - 07.15 WIB"
    val classStart = settings?.classStartTime ?: "07.15 WIB"
    val monThuEnd = settings?.classEndTimeMonThu ?: "12.30 WIB"
    val friEnd = settings?.classEndTimeFri ?: "11.00 WIB"
    val satEnd = settings?.classEndTimeSat ?: "12.00 WIB"
    val rules = settings?.schoolScheduleRules ?: "Siswa wajib hadir di madrasah pukul 06.45 - 07.00 WIB. Pukul 07.00 WIB gerbang ditutup untuk apel pagi dan sholat dhuha berjamaah."

    val uniforms = listOf(
        Pair("Senin", "Seragam Putih - Merah / Hijau Madrasah lengkap Dasi & Peci / Jilbab"),
        Pair("Selasa", "Seragam Batik Khas MI Muhammadiyah"),
        Pair("Rabu", "Seragam Pandu Hizbul Wathan (HW) Lengkap Atribut"),
        Pair("Kamis", "Seragam Batik Nasional Muhammadiyah"),
        Pair("Jumat", "Busana Muslim Koko / Gamis Putih Bersih & Santun"),
        Pair("Sabtu", "Kaos Olahraga Madrasah / Seragam Tapak Suci Putera Muhammadiyah")
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("school_schedule_screen")
    ) {
        item {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NavyPrimary)
                    .padding(20.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AccessTime, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Jadwal Masuk & Jam Belajar",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }
                    Text(
                        text = "Pedoman resmi jam kehadiran, apel pagi, kegiatan belajar mengajar, serta jadwal kepulangan santri MIM Tanjung Qencono.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Background Alarm Button
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = GoldAccent.copy(alpha = 0.15f))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Alarm, contentDescription = null, tint = NavyDeep, modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Pengingat Jam Masuk Latar Belakang", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NavyDeep)
                        Text("Aktifkan alarm pemberitahuan setiap pukul 06.30 WIB agar santri siap berangkat tepat waktu.", fontSize = 11.sp, color = NavyDeep.copy(alpha = 0.8f))
                        Spacer(modifier = Modifier.height(6.dp))
                        Button(
                            onClick = { viewModel.scheduleEntryReminder() },
                            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                        ) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Pasang Pengingat (06.30 WIB)", fontSize = 11.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Jadwal Masuk & Apel Pagi Card
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                SectionHeader(title = "Jam Kedatangan & Apel Pagi", icon = Icons.Default.WbSunny)

                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Pintu Gerbang Madrasah Dibuka", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Santri mulai disambut salam & adab pagi oleh ustadz/ustadzah", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Box(
                                modifier = Modifier
                                    .background(IslamicGreen.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text("06.30 WIB", fontWeight = FontWeight.ExtraBold, color = IslamicGreen, fontSize = 13.sp)
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Apel Pagi & Pembiasaan Sholat Dhuha", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Tadarus Juz 30, ikrar santri, dan pembinaan karakter", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Box(
                                modifier = Modifier
                                    .background(GoldAccent.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(morningAssembly, fontWeight = FontWeight.ExtraBold, color = NavyDeep, fontSize = 12.sp)
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Jam Pembelajaran Kelas Dimulai", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("KBM aktif sesuai jadwal kurikulum terpadu", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Box(
                                modifier = Modifier
                                    .background(NavyPrimary.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(classStart, fontWeight = FontWeight.ExtraBold, color = NavyPrimary, fontSize = 13.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Jadwal Kepulangan Harian
                SectionHeader(title = "Jadwal Kepulangan Santri", icon = Icons.Default.DirectionsWalk)

                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        ScheduleRowItem("Senin - Kamis", monThuEnd, "Setelah Sholat Dzuhur Berjamaah & Doa Penutup")
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        ScheduleRowItem("Jumat", friEnd, "Kepulangan lebih awal untuk persiapan Sholat Jumat")
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        ScheduleRowItem("Sabtu", satEnd, "Kegiatan Pengembangan Minat, Bakat & Tapak Suci")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Ketentuan Seragam Harian
                SectionHeader(title = "Ketentuan Seragam Masuk Harian", icon = Icons.Default.Checkroom)

                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        uniforms.forEachIndexed { index, (day, uniform) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(72.dp)
                                        .background(NavyPrimary.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                                        .padding(vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(day, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = NavyPrimary)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(uniform, fontSize = 12.sp, style = MaterialTheme.typography.bodyMedium)
                            }
                            if (index < uniforms.size - 1) {
                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Tata Tertib Kehadiran & Gerbang
                SectionHeader(title = "Tata Tertib Kehadiran Santri", icon = Icons.Default.Rule)

                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = rules,
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = 22.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Santri yang berhalangan hadir wajib mengirimkan surat izin / konfirmasi via WhatsApp madrasah.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
fun ScheduleRowItem(day: String, time: String, note: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(day, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(note, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .background(GoldAccent.copy(alpha = 0.25f), RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(time, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = NavyDeep)
        }
    }
}
