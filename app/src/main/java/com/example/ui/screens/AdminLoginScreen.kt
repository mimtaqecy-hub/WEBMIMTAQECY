package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.AppFooter
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NavyDeep
import com.example.ui.theme.NavyPrimary
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.MadrasahViewModel

@Composable
fun AdminLoginScreen(viewModel: MadrasahViewModel) {
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val loginError by viewModel.loginError.collectAsState()
    val isAdminLoggedIn by viewModel.isAdminLoggedIn.collectAsState()

    LaunchedEffect(isAdminLoggedIn) {
        if (isAdminLoggedIn) {
            viewModel.navigateTo(AppScreen.ADMIN_DASHBOARD)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Banner Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(NavyPrimary)
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            Column {
                Text(
                    text = "Login Administrator",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Text(
                    text = "Portal Manajemen Data & Sistem Informasi Madrasah",
                    style = MaterialTheme.typography.bodyMedium,
                    color = GoldAccent
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Card Login
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            shape = RoundedCornerShape(18.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_madrasah_official_logo),
                    contentDescription = "Logo",
                    modifier = Modifier
                        .size(72.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Autentikasi Petugas Madrasah",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Akses terbatas hanya untuk dewan guru, operator, dan pimpinan MI Muhammadiyah Tanjung Qencono.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp, bottom = 18.dp)
                )

                if (loginError != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                    ) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(loginError!!, color = MaterialTheme.colorScheme.onErrorContainer, fontSize = 12.sp)
                        }
                    }
                }

                // Password Input Field with prompt specification: placeholder "Masukkan Sandi Di Sini"
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = { Text("Masukkan Sandi Di Sini") },
                    label = { Text("Kata Sandi Admin") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    trailingIcon = {
                        val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(imageVector = image, contentDescription = if (passwordVisible) "Sembunyikan sandi" else "Tampilkan sandi")
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { viewModel.loginAdmin(password) }),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_password_input")
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Petunjuk sandi default madrasah: MIMTAQECY",
                    style = MaterialTheme.typography.labelSmall,
                    color = GoldAccent
                )

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = { viewModel.loginAdmin(password) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("admin_login_submit_btn"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Login, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Masuk ke Dashboard", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(12.dp))
                TextButton(onClick = { viewModel.navigateTo(AppScreen.HOME) }) {
                    Text("Kembali ke Beranda Publik")
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
        AppFooter(viewModel = viewModel)
    }
}
