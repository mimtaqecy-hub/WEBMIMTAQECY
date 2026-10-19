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
import com.example.data.local.NewsEntity
import com.example.ui.components.AppFooter
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NavyDeep
import com.example.ui.theme.NavyPrimary
import com.example.ui.viewmodel.MadrasahViewModel
import java.io.File

@Composable
fun NewsScreen(viewModel: MadrasahViewModel) {
    val newsList by viewModel.publishedNews.collectAsState()
    var selectedCategory by remember { mutableStateOf("Semua") }
    var searchQuery by remember { mutableStateOf("") }
    var selectedNewsDetail by remember { mutableStateOf<NewsEntity?>(null) }

    val categories = listOf("Semua", "Kegiatan", "Prestasi", "Pengumuman", "Madrasah")

    val filteredList = newsList.filter { item ->
        (selectedCategory == "Semua" || item.category.equals(selectedCategory, ignoreCase = true)) &&
        (searchQuery.isBlank() || item.title.contains(searchQuery, ignoreCase = true) || item.content.contains(searchQuery, ignoreCase = true))
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
                        text = "Berita & Pengumuman",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Text(
                        text = "Kabar aktivitas dan informasi resmi MI Muhammadiyah Tanjung Qencono",
                        style = MaterialTheme.typography.bodyMedium,
                        color = GoldAccent
                    )
                }
            }

            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Cari judul berita atau konten...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("news_search_input")
            )

            // Category Filter Row
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat) },
                        modifier = Modifier.testTag("category_chip_$cat")
                    )
                }
            }

            // Offline Cache Indicator
            com.example.ui.components.OfflineCacheBadge(
                title = "Artikel Berita Tersimpan di Cache Room",
                subtitle = "Semua berita dan pengumuman dapat dibaca lengkap dalam keadaan offline",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )
        }

        if (filteredList.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Tidak ada berita yang sesuai pencarian atau filter.")
                }
            }
        } else {
            items(filteredList) { news ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .clickable { selectedNewsDetail = news }
                        .testTag("news_card_${news.id}"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
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
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                            if (news.imageUrl.isNotBlank()) {
                                AsyncImage(
                                    model = if (news.imageUrl.startsWith("/")) File(news.imageUrl) else news.imageUrl,
                                    contentDescription = news.title,
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(12.dp))
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
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text(
                                text = "Baca Selengkapnya",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
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

    // Modal Detail Berita
    if (selectedNewsDetail != null) {
        val news = selectedNewsDetail!!
        AlertDialog(
            onDismissRequest = { selectedNewsDetail = null },
            title = {
                Text(news.title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            },
            text = {
                Column {
                    if (news.imageUrl.isNotBlank()) {
                        AsyncImage(
                            model = if (news.imageUrl.startsWith("/")) File(news.imageUrl) else news.imageUrl,
                            contentDescription = news.title,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(news.category, fontWeight = FontWeight.Bold, color = GoldAccent, fontSize = 12.sp)
                        Text(news.date, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(news.content, style = MaterialTheme.typography.bodyMedium, lineHeight = 22.sp)
                }
            },
            confirmButton = {
                Button(onClick = { selectedNewsDetail = null }) {
                    Text("Tutup")
                }
            }
        )
    }
}
