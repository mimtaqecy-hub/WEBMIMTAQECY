package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import com.example.data.local.StudentEntity
import com.example.ui.components.AppFooter
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NavyDeep
import com.example.ui.theme.NavyPrimary
import com.example.ui.viewmodel.MadrasahViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentDataScreen(viewModel: MadrasahViewModel) {
    var selectedClass by remember { mutableStateOf(0) } // 0: Semua
    var selectedRombel by remember { mutableStateOf("Semua Rombel") }
    var searchQuery by remember { mutableStateOf("") }

    val allStudents by viewModel.publishedStudents.collectAsState()
    val isAdminLoggedIn by viewModel.isAdminLoggedIn.collectAsState()
    var showDeleteAllDialog by remember { mutableStateOf(false) }

    val rombelList = listOf(
        "Semua Rombel",
        "Ar Rahman", "Ar Rahiim", "Al Malik", "Al Quddus", "As Salaam",
        "Al Mu'min", "Al Muhaimin", "Al Aziz", "Al Jabbar", "Al Mutakabbir",
        "Al Khaliq", "Al Baari"
    )

    val filteredList = allStudents.filter { s ->
        (selectedClass == 0 || s.classLevel == selectedClass) &&
        (selectedRombel == "Semua Rombel" || s.rombel.equals(selectedRombel, ignoreCase = true)) &&
        (searchQuery.isBlank() || s.fullName.contains(searchQuery, ignoreCase = true) || s.nisn.contains(searchQuery))
    }

    if (showDeleteAllDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAllDialog = false },
            title = { Text("Hapus Semua Data Siswa?") },
            text = { Text("Apakah Anda yakin ingin menghapus seluruh ${allStudents.size} data siswa dari database? Tindakan ini tidak dapat dibatalkan.") },
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

    LaunchedEffect(Unit) {
        viewModel.forceCloudSync()
    }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NavyPrimary)
                    .padding(horizontal = 20.dp, vertical = 20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Data Siswa Madrasah",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Text(
                            text = "Daftar Peserta Didik MI Muhammadiyah Tanjung Qencono",
                            style = MaterialTheme.typography.bodyMedium,
                            color = GoldAccent
                        )
                    }
                    IconButton(
                        onClick = { viewModel.forceCloudSync() },
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.18f), RoundedCornerShape(8.dp))
                    ) {
                        Icon(Icons.Default.CloudSync, contentDescription = "Sinkronkan Cloud", tint = Color.White)
                    }
                }
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Cari nama siswa atau NISN...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("student_search_input")
            )

            if (isAdminLoggedIn && allStudents.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Opsi Admin: Kelola Siswa",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.error
                        )
                        OutlinedButton(
                            onClick = { showDeleteAllDialog = true },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Hapus Semua Siswa", fontSize = 11.sp, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Filter Kelas 1-6
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text("Tingkat Kelas:", style = MaterialTheme.typography.labelMedium)
                LazyRow(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedClass == 0,
                            onClick = { selectedClass = 0 },
                            label = { Text("Semua Kelas") }
                        )
                    }
                    items((1..6).toList()) { cls ->
                        FilterChip(
                            selected = selectedClass == cls,
                            onClick = { selectedClass = cls },
                            label = { Text("Kelas $cls") }
                        )
                    }
                }

                // Filter Rombel Asmaul Husna
                var expandedRombel by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expandedRombel,
                    onExpandedChange = { expandedRombel = !expandedRombel },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    OutlinedTextField(
                        value = "Filter Rombel: $selectedRombel",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedRombel) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
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

                // Privacy Notice
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Shield, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Kebijakan Privasi: NIK dan data pribadi keluarga dilindungi dan hanya dapat diakses melalui portal admin resmi.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Offline Cache Indicator
                com.example.ui.components.OfflineCacheBadge(
                    title = "Database Siswa Tersimpan di Cache Room",
                    subtitle = "Data seluruh siswa tersimpan lokal dan dapat diakses kapan saja tanpa koneksi internet",
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Text(
                    text = "Menampilkan ${filteredList.size} Siswa",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }

        if (filteredList.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                    Text("Tidak ada data siswa yang cocok dengan kriteria filter.")
                }
            }
        } else {
            itemsIndexed(filteredList) { index, student ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${index + 1}",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontSize = 12.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(student.fullName, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                            Text("NISN: ${student.nisn} • ${student.gender}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("TTL: ${student.birthPlace}, ${student.birthDate}", style = MaterialTheme.typography.labelSmall)
                        }
                        Box(
                            modifier = Modifier
                                .background(GoldAccent.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Kls ${student.classLevel} - ${student.rombel}",
                                fontWeight = FontWeight.Bold,
                                color = NavyDeep,
                                fontSize = 11.sp
                            )
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
