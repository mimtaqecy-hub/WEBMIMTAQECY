package com.example

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.ui.components.AppDrawerContent
import com.example.ui.components.AppTopBar
import com.example.ui.components.FloatingWhatsappButton
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.MadrasahViewModel
import com.example.util.NotificationHelper
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: MadrasahViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Create Android Notification Channel
        NotificationHelper.createNotificationChannel(this)

        // Handle direct navigation from notification click
        if (intent?.getStringExtra("EXTRA_NAVIGATE_TO") == "NOTIFICATIONS") {
            viewModel.navigateTo(AppScreen.NOTIFICATIONS)
        }

        setContent {
            val isDark by viewModel.isDarkTheme.collectAsState()
            val currentScreen by viewModel.currentScreen.collectAsState()
            val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
            val scope = rememberCoroutineScope()
            val snackbarHostState = remember { SnackbarHostState() }

            // Request Notification Permission on Android 13+ (API 33+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val notifPermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { /* Permission result handled */ }

                LaunchedEffect(Unit) {
                    notifPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                }
            }

            // Listen to snackbar messages
            LaunchedEffect(Unit) {
                viewModel.snackbarMessage.collect { message ->
                    snackbarHostState.showSnackbar(message)
                }
            }

            // Handle hardware or system back button
            BackHandler(enabled = currentScreen != AppScreen.HOME) {
                if (drawerState.isOpen) {
                    scope.launch { drawerState.close() }
                } else {
                    viewModel.navigateTo(AppScreen.HOME)
                }
            }

            MyApplicationTheme(darkTheme = isDark) {
                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        AppDrawerContent(
                            viewModel = viewModel,
                            onCloseDrawer = { scope.launch { drawerState.close() } }
                        )
                    }
                ) {
                    Scaffold(
                        snackbarHost = { SnackbarHost(snackbarHostState) },
                        topBar = {
                            if (currentScreen != AppScreen.ADMIN_DASHBOARD) {
                                AppTopBar(
                                    viewModel = viewModel,
                                    onMenuClick = { scope.launch { drawerState.open() } }
                                )
                            }
                        },
                        floatingActionButton = {
                            if (currentScreen != AppScreen.ADMIN_DASHBOARD) {
                                FloatingWhatsappButton()
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            Crossfade(targetState = currentScreen, label = "ScreenTransition") { screen ->
                                when (screen) {
                                    AppScreen.HOME -> HomeScreen(viewModel)
                                    AppScreen.PROFILE -> ProfileScreen(viewModel)
                                    AppScreen.FACILITIES -> FacilitiesScreen(viewModel)
                                    AppScreen.SCHOOL_SCHEDULE -> SchoolScheduleScreen(viewModel)
                                    AppScreen.NOTIFICATIONS -> NotificationsScreen(viewModel)
                                    AppScreen.ACADEMIC -> AcademicScreen(viewModel)
                                    AppScreen.NEWS -> NewsScreen(viewModel)
                                    AppScreen.GALLERY -> GalleryScreen(viewModel)
                                    AppScreen.PPDB -> PpdbScreen(viewModel)
                                    AppScreen.STUDENT_DATA -> StudentDataScreen(viewModel)
                                    AppScreen.PRAYER_SCHEDULE -> PrayerScheduleScreen(viewModel)
                                    AppScreen.CONTACT -> ContactScreen(viewModel)
                                    AppScreen.ADMIN_LOGIN -> AdminLoginScreen(viewModel)
                                    AppScreen.ADMIN_DASHBOARD -> AdminDashboardScreen(viewModel)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
