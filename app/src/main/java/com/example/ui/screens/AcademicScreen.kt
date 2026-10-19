package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.data.local.ClassScheduleEntity
import com.example.ui.components.AppFooter
import com.example.ui.components.SectionHeader
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NavyPrimary
import com.example.ui.viewmodel.MadrasahViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AcademicScreen(viewModel: MadrasahViewModel) {
    val settings by viewModel.settings.collectAsState()

    var selectedClass by remember { mutableStateOf(1) }
    var selectedRombel by remember { mutableStateOf("Ar Rahman") }

    val rombelList = listOf(
        "Ar Rahman", "Ar Rahiim", "Al Malik", "Al Quddus", "As Salaam",
        "Al Mu'min", "Al Muhaimin", "Al Aziz", "Al Jabbar", "Al Mutakabbir",
        "Al Khaliq", "Al Baari"
    )

    // Collect schedules dynamically based on selected class and rombel
    val scheduleList by viewModel.repository.getSchedules(selectedClass, selectedRombel)
        .collectAsState(initial = emptyList())

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NavyPrimary)
                    .padding(horizontal = 20.dp, vertical = 24.dp)
            ) {
                Column {
                    Text(
                        text = "Akademik & Pembelajaran",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Text(
                        text = "Kurikulum Merdeka Berkarakter Islami & Jadwal Pelajaran",
                        style = MaterialTheme.typography.bodyMedium,
                        color = GoldAccent
                    )
                }
            }
        }

        // INFORMASI JAM MASUK & PULANG
        item {
            Column(modifier = Modifier.padding(16.dp)) {
                SectionHeader(title = "Jadwal Masuk & Pulang", icon = Icons.Default.Schedule)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.WbSunny, contentDescription = null, tint = GoldAccent)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Apel & Sholat Dhuha Pagi: ${settings?.morningAssemblyTime ?: "07.00 - 07.15 WIB"}",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Jam Masuk Pelajaran: ${settings?.classStartTime ?: "07.15 WIB"}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "Senin - Kamis: Pulang ${settings?.classEndTimeMonThu ?: "12.30 WIB"} (Setelah Dzuhur Berjamaah)",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "Jumat: Pulang ${settings?.classEndTimeFri ?: "11.00 WIB"} (Persiapan Sholat Jumat)",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "Sabtu: Pulang ${settings?.classEndTimeSat ?: "12.00 WIB"} (Ekstrakurikuler Wajib)",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        // INFORMASI KURIKULUM
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                SectionHeader(title = "Struktur Kurikulum", icon = Icons.Default.MenuBook)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Integrasi Kurikulum Merdeka & Kemenag RI",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "MI Muhammadiyah memadukan muatan Kurikulum Nasional (Bahasa Indonesia, Matematika, IPAS, PPKn, Seni & PJOK) dengan Muatan Khusus Keagamaan Kemenag (Al-Qur'an Hadist, Akidah Akhlak, Fikih, Sejarah Kebudayaan Islam, Bahasa Arab) serta Kemuhammadiyahan & Kepanduan Hizbul Wathan.",
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = 22.sp
                        )
                    }
                }
            }
        }

        // JADWAL PELAJARAN PER KELAS & ROMBEL
        item {
            Column(modifier = Modifier.padding(16.dp)) {
                SectionHeader(title = "Jadwal Pelajaran", subtitle = "Filter jadwal berdasarkan jenjang kelas dan nama rombel", icon = Icons.Default.EventNote)

                // Filter Kelas 1-6 Chips
                Text("Pilih Kelas:", style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    (1..6).forEach { cls ->
                        FilterChip(
                            selected = selectedClass == cls,
                            onClick = { selectedClass = cls },
                            label = { Text("Kelas $cls") },
                            modifier = Modifier.testTag("filter_class_$cls")
                        )
                    }
                }

                // Filter Rombel Asmaul Husna
                Text("Pilih Rombel (12 Nama Asmaul Husna):", style = MaterialTheme.typography.labelMedium)
                var expandedRombel by remember { mutableStateOf(false) }

                ExposedDropdownMenuBox(
                    expanded = expandedRombel,
                    onExpandedChange = { expandedRombel = !expandedRombel },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
                ) {
                    OutlinedTextField(
                        value = "Rombel: $selectedRombel",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedRombel) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                            .testTag("select_rombel_dropdown")
                    )
                    ExposedDropdownMenu(
                        expanded = expandedRombel,
                        onDismissRequest = { expandedRombel = false }
                    ) {
                        rombelList.forEach { rombel ->
                            DropdownMenuItem(
                                text = { Text(rombel) },
                                onClick = {
                                    selectedRombel = rombel
                                    expandedRombel = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // LIST JADWAL
        if (scheduleList.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Jadwal Pelajaran Kelas $selectedClass Rombel $selectedRombel",
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Jadwal mata pelajaran lengkap sedang disinkronkan oleh dewan guru untuk rombel ini.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(scheduleList) { schedule ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .background(GoldAccent.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Text(schedule.day, fontWeight = FontWeight.Bold, color = NavyPrimary, fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(schedule.subject, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                            Text(schedule.timeSlot, style = MaterialTheme.typography.labelSmall, color = GoldAccent)
                            Text("Guru: ${schedule.teacherName}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
