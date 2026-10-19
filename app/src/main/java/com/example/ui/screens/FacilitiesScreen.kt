package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.FacilityEntity
import com.example.ui.components.AppFooter
import com.example.ui.components.SectionHeader
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.IslamicGreen
import com.example.ui.theme.NavyDeep
import com.example.ui.theme.NavyPrimary
import com.example.ui.viewmodel.MadrasahViewModel
import java.io.File

@Composable
fun FacilitiesScreen(viewModel: MadrasahViewModel) {
    val facilities by viewModel.publishedFacilities.collectAsState()
    var selectedCategory by remember { mutableStateOf("Semua") }
    var searchQuery by remember { mutableStateOf("") }
    var selectedFacilityDetail by remember { mutableStateOf<FacilityEntity?>(null) }

    val categories = listOf("Semua", "Ruang Belajar", "Sarana Ibadah", "Laboratorium & Komputer", "Olahraga & Seni", "Penunjang")

    val filteredList = facilities.filter { facility ->
        val matchesCategory = selectedCategory == "Semua" || facility.category.contains(selectedCategory, ignoreCase = true)
        val matchesSearch = searchQuery.isBlank() || facility.name.contains(searchQuery, ignoreCase = true) || facility.description.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("facilities_screen")
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
                        Icon(Icons.Default.Business, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Fasilitas & Sarana Madrasah",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }
                    Text(
                        text = "Sarana dan prasarana modern representatif mendukung proses belajar mengajar santri MI Muhammadiyah Tanjung Qencono.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            }

            // Search Bar & Categories
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Cari fasilitas (misal: kelas, masjid, lab)...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = NavyPrimary) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Hapus")
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("facilities_search")
                )

                Spacer(modifier = Modifier.height(10.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(categories) { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = { selectedCategory = category },
                            label = { Text(category, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NavyPrimary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }
        }

        if (filteredList.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.SearchOff, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Fasilitas tidak ditemukan", fontWeight = FontWeight.Bold)
                        Text("Coba pilih kategori lain atau ubah kata kunci pencarian", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        } else {
            items(filteredList) { facility ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .clickable { selectedFacilityDetail = facility }
                        .testTag("facility_card_${facility.id}"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column {
                        if (facility.imageUrl.isNotBlank()) {
                            AsyncImage(
                                model = if (facility.imageUrl.startsWith("/")) File(facility.imageUrl) else facility.imageUrl,
                                contentDescription = facility.name,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp)),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(110.dp)
                                    .background(NavyPrimary.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        when {
                                            facility.category.contains("Ibadah") -> Icons.Default.Mosque
                                            facility.category.contains("Komputer") -> Icons.Default.Computer
                                            facility.category.contains("Olahraga") -> Icons.Default.SportsBasketball
                                            else -> Icons.Default.School
                                        },
                                        contentDescription = null,
                                        tint = NavyPrimary,
                                        modifier = Modifier.size(36.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(facility.name, fontWeight = FontWeight.Bold, color = NavyPrimary)
                                }
                            }
                        }

                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .background(GoldAccent.copy(alpha = 0.25f), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(facility.category, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NavyDeep)
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = IslamicGreen, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(facility.condition, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = IslamicGreen)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = facility.name,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )

                            if (facility.capacity.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Groups, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Kapasitas: ${facility.capacity}", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = facility.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 20.sp
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

    selectedFacilityDetail?.let { fac ->
        AlertDialog(
            onDismissRequest = { selectedFacilityDetail = null },
            title = { Text(fac.name, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    if (fac.imageUrl.isNotBlank()) {
                        AsyncImage(
                            model = if (fac.imageUrl.startsWith("/")) File(fac.imageUrl) else fac.imageUrl,
                            contentDescription = fac.name,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    Text("Kategori: ${fac.category}", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Text("Kondisi: ${fac.condition}", color = IslamicGreen, fontWeight = FontWeight.Medium, fontSize = 13.sp)
                    if (fac.capacity.isNotBlank()) {
                        Text("Kapasitas: ${fac.capacity}", fontSize = 13.sp)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(fac.description, style = MaterialTheme.typography.bodyMedium)
                }
            },
            confirmButton = {
                Button(onClick = { selectedFacilityDetail = null }) {
                    Text("Tutup")
                }
            }
        )
    }
}
