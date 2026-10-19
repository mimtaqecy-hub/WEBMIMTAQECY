package com.example.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.GalleryEntity
import com.example.ui.components.AppFooter
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NavyDeep
import com.example.ui.theme.NavyPrimary
import com.example.ui.viewmodel.MadrasahViewModel
import coil.compose.AsyncImage
import java.io.File

@Composable
fun GalleryScreen(viewModel: MadrasahViewModel) {
    val galleryList by viewModel.publishedGallery.collectAsState()
    var selectedCategory by remember { mutableStateOf("Semua") }
    var zoomItem by remember { mutableStateOf<GalleryEntity?>(null) }

    val categories = listOf("Semua", "Kegiatan", "Pembelajaran", "Upacara", "Prestasi", "Ekstrakurikuler", "Fasilitas")

    val filteredList = galleryList.filter { item ->
        selectedCategory == "Semua" || item.category.equals(selectedCategory, ignoreCase = true)
    }

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
                        text = "Galeri Dokumentasi",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Text(
                        text = "Dokumentasi kegiatan dan fasilitas MI Muhammadiyah Tanjung Qencono",
                        style = MaterialTheme.typography.bodyMedium,
                        color = GoldAccent
                    )
                }
            }

            // Categories
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat) },
                        modifier = Modifier.testTag("gallery_filter_$cat")
                    )
                }
            }
        }

        if (filteredList.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Belum ada foto dalam kategori ini.")
                }
            }
        } else {
            // Group into pairs for a 2-column grid effect inside LazyColumn
            val chunkedList = filteredList.chunked(2)
            items(chunkedList) { rowItems ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    rowItems.forEach { item ->
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { zoomItem = item }
                                .testTag("gallery_item_${item.id}"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(130.dp)
                                        .background(NavyDeep),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (item.imageUrl.isNotBlank()) {
                                        AsyncImage(
                                            model = if (item.imageUrl.startsWith("/")) File(item.imageUrl) else item.imageUrl,
                                            contentDescription = item.title,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Image(
                                            painter = painterResource(id = R.drawable.hero_madrasah_1792255583606),
                                            contentDescription = item.title,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomStart)
                                            .padding(6.dp)
                                            .background(NavyDeep.copy(alpha = 0.8f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(item.category, color = GoldAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = item.title,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        maxLines = 2
                                    )
                                    Text(
                                        text = item.date,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                    if (rowItems.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
            AppFooter(viewModel = viewModel)
        }
    }

    // Lightbox / Zoom Dialog
    if (zoomItem != null) {
        val item = zoomItem!!
        AlertDialog(
            onDismissRequest = { zoomItem = null },
            title = { Text(item.title, style = MaterialTheme.typography.titleMedium) },
            text = {
                Column {
                    if (item.imageUrl.isNotBlank()) {
                        AsyncImage(
                            model = if (item.imageUrl.startsWith("/")) File(item.imageUrl) else item.imageUrl,
                            contentDescription = item.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.hero_madrasah_1792255583606),
                            contentDescription = item.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Kategori: ${item.category}", fontWeight = FontWeight.Bold, color = GoldAccent, fontSize = 12.sp)
                    Text("Tanggal: ${item.date}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(item.description, style = MaterialTheme.typography.bodyMedium)
                }
            },
            confirmButton = {
                Button(onClick = { zoomItem = null }) {
                    Text("Tutup Pratinjau")
                }
            }
        )
    }
}
