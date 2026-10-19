package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import com.example.data.local.AppNotificationEntity
import com.example.ui.components.AppFooter
import com.example.ui.components.SectionHeader
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.IslamicGreen
import com.example.ui.theme.NavyDeep
import com.example.ui.theme.NavyPrimary
import com.example.ui.viewmodel.MadrasahViewModel

@Composable
fun NotificationsScreen(viewModel: MadrasahViewModel) {
    val notifications by viewModel.allNotifications.collectAsState()
    val unreadCount by viewModel.unreadNotificationCount.collectAsState()
    var selectedCategory by remember { mutableStateOf("Semua") }
    var selectedNotifDetail by remember { mutableStateOf<AppNotificationEntity?>(null) }

    val categories = listOf("Semua", "Pengumuman", "Jadwal Masuk", "Akademik", "Kegiatan", "Darurat")

    val filteredList = notifications.filter { notif ->
        selectedCategory == "Semua" || notif.category.equals(selectedCategory, ignoreCase = true)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("notifications_screen")
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(28.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Pusat Notifikasi Madrasah",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                        }

                        if (unreadCount > 0) {
                            Box(
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.error, RoundedCornerShape(12.dp))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text("$unreadCount Baru", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }

                    Text(
                        text = "Pemberitahuan resmi pengumuman, jadwal masuk madrasah, kegiatan santri, dan informasi penting lainnya.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            }

            // Quick Actions Bar
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = { viewModel.markAllNotificationsRead() },
                        enabled = unreadCount > 0
                    ) {
                        Icon(Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Tandai Semua Dibaca", fontSize = 12.sp)
                    }

                    FilledTonalButton(
                        onClick = { viewModel.testAudibleNotification() }
                    ) {
                        Icon(Icons.Default.VolumeUp, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Uji Suara Notifikasi", fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, fontSize = 12.sp) },
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
                        .padding(50.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.NotificationsNone, contentDescription = null, modifier = Modifier.size(54.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Belum Ada Notifikasi", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("Pemberitahuan resmi madrasah akan muncul di sini.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        } else {
            items(filteredList) { notif ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 5.dp)
                        .clickable {
                            if (!notif.isRead) {
                                viewModel.markNotificationRead(notif.id)
                            }
                            selectedNotifDetail = notif
                        }
                        .testTag("notif_card_${notif.id}"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (!notif.isRead) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surface
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .background(
                                    when (notif.category) {
                                        "Darurat" -> MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                                        "Jadwal Masuk" -> GoldAccent.copy(alpha = 0.35f)
                                        else -> NavyPrimary.copy(alpha = 0.15f)
                                    },
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (!notif.isRead) Icons.Default.NotificationsActive else Icons.Default.Notifications,
                                contentDescription = null,
                                tint = if (notif.category == "Darurat") MaterialTheme.colorScheme.error else NavyPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .background(GoldAccent.copy(alpha = 0.25f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(notif.category, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = NavyDeep)
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(notif.dateFormatted, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    if (!notif.isRead) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .background(MaterialTheme.colorScheme.primary, CircleShape)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = notif.title,
                                fontWeight = if (!notif.isRead) FontWeight.Bold else FontWeight.SemiBold,
                                fontSize = 14.sp
                            )

                            Spacer(modifier = Modifier.height(2.dp))

                            Text(
                                text = notif.message,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 18.sp
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

    selectedNotifDetail?.let { notif ->
        AlertDialog(
            onDismissRequest = { selectedNotifDetail = null },
            icon = {
                Icon(
                    Icons.Default.Notifications,
                    contentDescription = null,
                    tint = NavyPrimary,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(notif.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            text = {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .background(GoldAccent.copy(alpha = 0.25f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(notif.category, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = NavyDeep)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(notif.dateFormatted, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        notif.message,
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 22.sp
                    )
                }
            },
            confirmButton = {
                Button(onClick = { selectedNotifDetail = null }) {
                    Text("Tutup")
                }
            }
        )
    }
}
