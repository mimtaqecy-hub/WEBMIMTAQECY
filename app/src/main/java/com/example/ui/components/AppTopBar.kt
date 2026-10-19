package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NavyDeep
import com.example.ui.theme.NavyPrimary
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.MadrasahViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    viewModel: MadrasahViewModel,
    onMenuClick: () -> Unit
) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val isDark by viewModel.isDarkTheme.collectAsState()
    val isMusicPlaying by viewModel.isMusicPlaying.collectAsState()
    val isAdminLoggedIn by viewModel.isAdminLoggedIn.collectAsState()
    var isSearchExpanded by remember { mutableStateOf(false) }
    val globalSearch by viewModel.globalSearch.collectAsState()

    Surface(
        color = MaterialTheme.colorScheme.primary,
        shadowElevation = 4.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth().statusBarsPadding()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Navigation Drawer Toggle
                IconButton(
                    onClick = onMenuClick,
                    modifier = Modifier.testTag("nav_drawer_toggle")
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Buka Menu",
                        tint = Color.White
                    )
                }

                // Madrasah Logo & Name
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewModel.navigateTo(AppScreen.HOME) },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_madrasah_official_logo),
                        contentDescription = "Logo MI Muhammadiyah",
                        modifier = Modifier
                            .size(38.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "MIM TANJUNG QENCONO",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                ),
                                color = Color.White,
                                maxLines = 1
                            )
                            if (isAdminLoggedIn) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .background(GoldAccent, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text("ADMIN", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = NavyDeep)
                                }
                            }
                        }
                        Text(
                            text = "Religi, Cerdas, dan Berprestasi",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = GoldAccent,
                            maxLines = 1
                        )
                    }
                }

                // Background Music Toggle Button
                IconButton(
                    onClick = { viewModel.toggleMusic() },
                    modifier = Modifier.testTag("toggle_music_button")
                ) {
                    Icon(
                        imageVector = if (isMusicPlaying) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                        contentDescription = "Musik Latar",
                        tint = if (isMusicPlaying) GoldAccent else Color.White.copy(alpha = 0.7f)
                    )
                }

                // Dark/Light Theme Toggle
                IconButton(
                    onClick = { viewModel.toggleDarkTheme() },
                    modifier = Modifier.testTag("toggle_dark_theme")
                ) {
                    Icon(
                        imageVector = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                        contentDescription = "Ganti Tema",
                        tint = Color.White
                    )
                }

                // Bell Notification Icon (Icon Lonceng)
                val unreadNotifCount by viewModel.unreadNotificationCount.collectAsState()
                IconButton(
                    onClick = { viewModel.navigateTo(AppScreen.NOTIFICATIONS) },
                    modifier = Modifier.testTag("notification_bell_button")
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
                        Icon(
                            imageVector = if (unreadNotifCount > 0) Icons.Default.NotificationsActive else Icons.Default.Notifications,
                            contentDescription = "Notifikasi Madrasah",
                            tint = if (unreadNotifCount > 0) GoldAccent else Color.White
                        )
                    }
                }

                // Search toggle
                IconButton(
                    onClick = { isSearchExpanded = !isSearchExpanded },
                    modifier = Modifier.testTag("search_toggle_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Pencarian Global",
                        tint = Color.White
                    )
                }
            }

            // Global search bar expandable
            AnimatedVisibility(visible = isSearchExpanded) {
                OutlinedTextField(
                    value = globalSearch,
                    onValueChange = { viewModel.setGlobalSearch(it) },
                    placeholder = { Text("Cari berita, agenda, siswa, guru...", fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = NavyPrimary) },
                    trailingIcon = {
                        if (globalSearch.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setGlobalSearch("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Hapus")
                            }
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = GoldAccent
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                        .testTag("global_search_input")
                )
            }
        }
    }
}
