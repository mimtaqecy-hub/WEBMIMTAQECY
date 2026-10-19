package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
fun PrayerScheduleScreen(viewModel: MadrasahViewModel) {
    val prayerTimes by viewModel.prayerTimes.collectAsState()

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
                        text = "Jadwal Sholat Fardhu",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Text(
                        text = "Kecamatan Way Bungur, Kabupaten Lampung Timur",
                        style = MaterialTheme.typography.bodyMedium,
                        color = GoldAccent
                    )
                }
            }

            // Location & Date Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "${prayerTimes.dayName}, ${prayerTimes.dateString}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Standar Hisab Kemenag RI (Bujur 105.5° BT, Lintang -5.04° LS)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                    IconButton(
                        onClick = { viewModel.refreshPrayerTimes() },
                        modifier = Modifier.testTag("refresh_prayer_btn")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Perbarui", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            }

            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                SectionHeader(title = "Waktu Sholat Hari Ini", icon = Icons.Default.AccessTime)

                PrayerTimeCard("Imsak", prayerTimes.imsak, "Waktu menahan diri sebelum fajar", false)
                PrayerTimeCard("Subuh", prayerTimes.subuh, "Sholat Fardhu 2 Rakaat", true)
                PrayerTimeCard("Terbit", prayerTimes.terbit, "Matahari terbit (Syuruq)", false)
                PrayerTimeCard("Dhuha", prayerTimes.dhuha, "Sholat Sunnah Dhuha Santri", false)
                PrayerTimeCard("Dzuhur", prayerTimes.dzuhur, "Sholat Fardhu 4 Rakaat Berjamaah", true)
                PrayerTimeCard("Ashar", prayerTimes.ashar, "Sholat Fardhu 4 Rakaat", true)
                PrayerTimeCard("Maghrib", prayerTimes.maghrib, "Sholat Fardhu 3 Rakaat", true)
                PrayerTimeCard("Isya", prayerTimes.isya, "Sholat Fardhu 4 Rakaat", true)
            }

            // Doa / Hikmah
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Doa Setelah Adzan:",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Allahumma rabba hadzihid da’watit tammah, wash shalatil qa’imah, ati sayyidana Muhammadanil wasilata wal fadhilah, wab’atshu maqamam mahmudanil ladzi wa’adtah.",
                        style = MaterialTheme.typography.bodySmall,
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "“Ya Allah, Tuhan pemilik seruan yang sempurna ini dan shalat yang didirikan, berikanlah kepada Nabi Muhammad wasilah dan keutamaan...”",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            AppFooter(viewModel = viewModel)
        }
    }
}

@Composable
fun PrayerTimeCard(name: String, time: String, desc: String, isFardhu: Boolean) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isFardhu) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(if (isFardhu) GoldAccent.copy(alpha = 0.2f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isFardhu) Icons.Default.Mosque else Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = if (isFardhu) NavyDeep else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = desc,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Text(
                text = time,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = if (isFardhu) IslamicGreen else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
