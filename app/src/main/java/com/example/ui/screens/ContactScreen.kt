package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AppFooter
import com.example.ui.components.SectionHeader
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.IslamicGreen
import com.example.ui.theme.NavyPrimary
import com.example.ui.viewmodel.MadrasahViewModel

@Composable
fun ContactScreen(viewModel: MadrasahViewModel) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsState()

    var senderName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

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
                        text = "Kontak & Layanan",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Text(
                        text = "Hubungi sekretariat dan tata usaha MI Muhammadiyah Tanjung Qencono",
                        style = MaterialTheme.typography.bodyMedium,
                        color = GoldAccent
                    )
                }
            }

            // Quick Contact Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        val url = "https://wa.me/6285378029852?text=Assalamu%27alaikum%20MI%20Muhammadiyah%20Tanjung%20Qencono"
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                    modifier = Modifier.weight(1f).testTag("btn_hubungi_wa")
                ) {
                    Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Chat WhatsApp")
                }

                Button(
                    onClick = {
                        val geoUri = Uri.parse("geo:-5.0445,105.5188?q=MI+Muhammadiyah+Tanjung+Qencono+Way+Bungur")
                        context.startActivity(Intent(Intent.ACTION_VIEW, geoUri))
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.weight(1f).testTag("btn_buka_maps")
                ) {
                    Icon(Icons.Default.Place, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Lokasi Maps")
                }
            }

            // Info Detail Cards
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                SectionHeader(title = "Informasi Sekretariat", icon = Icons.Default.Business)

                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), shape = RoundedCornerShape(12.dp)) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Alamat Madrasah:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(settings?.address ?: "Jl. Pendidikan No.01 Desa Tanjung Qencono, Kec. Way Bungur, Kab. Lampung Timur", style = MaterialTheme.typography.bodyMedium)

                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Telepon / WhatsApp Resmi:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(settings?.phoneWa ?: "0853-7802-9852", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = IslamicGreen)

                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Email Madrasah:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(settings?.email ?: "mimtaqecy@gmail.com", style = MaterialTheme.typography.bodyMedium)

                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Jam Pelayanan Tata Usaha:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Senin – Kamis: 07.15 – 12.30 WIB", style = MaterialTheme.typography.bodySmall)
                        Text("Jumat: 07.15 – 11.00 WIB", style = MaterialTheme.typography.bodySmall)
                        Text("Sabtu: 07.15 – 12.00 WIB", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            // FORM PESAN ONLINE
            Column(modifier = Modifier.padding(16.dp)) {
                SectionHeader(title = "Kirim Pesan / Pengaduan", subtitle = "Pesan akan langsung masuk ke database dashboard tata usaha madrasah", icon = Icons.Default.Email)

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        OutlinedTextField(
                            value = senderName,
                            onValueChange = { senderName = it },
                            label = { Text("Nama Lengkap Anda *") },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        )
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Alamat Email") },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        )
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Nomor WhatsApp / HP *") },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        )
                        OutlinedTextField(
                            value = subject,
                            onValueChange = { subject = it },
                            label = { Text("Subjek Pesan / Pertanyaan *") },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        )
                        OutlinedTextField(
                            value = message,
                            onValueChange = { message = it },
                            label = { Text("Isi Pesan / Pertanyaan Anda *") },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            minLines = 4
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = {
                                if (senderName.isNotBlank() && phone.isNotBlank() && message.isNotBlank()) {
                                    viewModel.sendContactMessage(
                                        name = senderName,
                                        email = email,
                                        phone = phone,
                                        subject = if (subject.isBlank()) "Pertanyaan Umum" else subject,
                                        message = message
                                    )
                                    senderName = ""
                                    email = ""
                                    phone = ""
                                    subject = ""
                                    message = ""
                                }
                            },
                            modifier = Modifier.fillMaxWidth().testTag("btn_kirim_pesan")
                        ) {
                            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Kirim Pesan ke Madrasah")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            AppFooter(viewModel = viewModel)
        }
    }
}
