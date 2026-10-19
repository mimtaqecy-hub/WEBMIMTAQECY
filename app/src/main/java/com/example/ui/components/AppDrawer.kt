package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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

data class NavItem(val screen: AppScreen, val icon: ImageVector, val label: String)

@Composable
fun AppDrawerContent(
    viewModel: MadrasahViewModel,
    onCloseDrawer: () -> Unit
) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val isAdminLoggedIn by viewModel.isAdminLoggedIn.collectAsState()

    val navItems = listOf(
        NavItem(AppScreen.HOME, Icons.Default.Home, "1. Beranda"),
        NavItem(AppScreen.PROFILE, Icons.Default.AccountBalance, "2. Profil"),
        NavItem(AppScreen.FACILITIES, Icons.Default.Business, "3. Fasilitas Madrasah"),
        NavItem(AppScreen.SCHOOL_SCHEDULE, Icons.Default.AccessTime, "4. Jadwal Masuk"),
        NavItem(AppScreen.NOTIFICATIONS, Icons.Default.Notifications, "5. Notifikasi Madrasah"),
        NavItem(AppScreen.ACADEMIC, Icons.Default.School, "6. Akademik"),
        NavItem(AppScreen.NEWS, Icons.Default.Article, "7. Berita & Info"),
        NavItem(AppScreen.GALLERY, Icons.Default.Collections, "8. Galeri Foto"),
        NavItem(AppScreen.PPDB, Icons.Default.AppRegistration, "9. PPDB Online"),
        NavItem(AppScreen.STUDENT_DATA, Icons.Default.People, "10. Data Siswa"),
        NavItem(AppScreen.PRAYER_SCHEDULE, Icons.Default.Schedule, "11. Jadwal Sholat"),
        NavItem(AppScreen.CONTACT, Icons.Default.ContactPhone, "12. Kontak"),
        if (isAdminLoggedIn) {
            NavItem(AppScreen.ADMIN_DASHBOARD, Icons.Default.Dashboard, "13. Dashboard Admin")
        } else {
            NavItem(AppScreen.ADMIN_LOGIN, Icons.Default.Lock, "13. Login Admin")
        }
    )

    ModalDrawerSheet(
        drawerContainerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.widthIn(max = 320.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
        ) {
            // Header Drawer with Logo & School Identity
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NavyPrimary)
                    .padding(20.dp)
            ) {
                Column {
                    Image(
                        painter = painterResource(id = R.drawable.ic_madrasah_official_logo),
                        contentDescription = "Logo",
                        modifier = Modifier
                            .size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "MI MUHAMMADIYAH",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "TANJUNG QENCONO",
                        fontWeight = FontWeight.SemiBold,
                        color = GoldAccent,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "Kec. Way Bungur, Lampung Timur",
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            navItems.forEach { item ->
                val selected = currentScreen == item.screen
                NavigationDrawerItem(
                    icon = {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label,
                            tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    label = {
                        Text(
                            text = item.label,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    },
                    selected = selected,
                    onClick = {
                        viewModel.navigateTo(item.screen)
                        onCloseDrawer()
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
                        selectedTextColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 3.dp)
                        .testTag("drawer_item_${item.screen.name.lowercase()}")
                )
            }

            if (isAdminLoggedIn) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                Spacer(modifier = Modifier.height(8.dp))

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.ExitToApp, contentDescription = "Keluar", tint = MaterialTheme.colorScheme.error) },
                    label = { Text("Logout Admin", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold) },
                    selected = false,
                    onClick = {
                        viewModel.logoutAdmin()
                        onCloseDrawer()
                    },
                    modifier = Modifier.padding(horizontal = 12.dp).testTag("drawer_logout_btn")
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
