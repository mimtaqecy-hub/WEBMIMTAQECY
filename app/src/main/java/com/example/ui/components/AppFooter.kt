package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NavyDeep
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.MadrasahViewModel

@Composable
fun AppFooter(
    viewModel: MadrasahViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Surface(
        color = NavyDeep,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "MI MUHAMMADIYAH TANJUNG QENCONO",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                ),
                color = Color.White
            )
            Text(
                text = "Religi, Cerdas, dan Berprestasi",
                style = MaterialTheme.typography.bodySmall,
                color = GoldAccent,
                modifier = Modifier.padding(top = 2.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Address & Contact Info
            Row(verticalAlignment = Alignment.Top, modifier = Modifier.padding(vertical = 4.dp)) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = GoldAccent,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Jl. Pendidikan No.01 Desa Tanjung Qencono, Kec. Way Bungur, Kab. Lampung Timur",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = null,
                    tint = GoldAccent,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "0853-7802-9852",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.clickable {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:085378029852"))
                        context.startActivity(intent)
                    }
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = null,
                    tint = GoldAccent,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "mimtaqecy@gmail.com",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.clickable {
                        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:mimtaqecy@gmail.com"))
                        context.startActivity(intent)
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.15f))
            Spacer(modifier = Modifier.height(12.dp))

            // Quick Nav Chips
            Text("Navigasi Cepat:", style = MaterialTheme.typography.labelSmall, color = GoldAccent)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { viewModel.navigateTo(AppScreen.PPDB) },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    modifier = Modifier.weight(1f).testTag("footer_ppdb_btn")
                ) {
                    Text("PPDB Online", fontSize = 11.sp)
                }
                OutlinedButton(
                    onClick = { viewModel.navigateTo(AppScreen.STUDENT_DATA) },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    modifier = Modifier.weight(1f).testTag("footer_siswa_btn")
                ) {
                    Text("Data Siswa", fontSize = 11.sp)
                }
                OutlinedButton(
                    onClick = { viewModel.navigateTo(AppScreen.ADMIN_LOGIN) },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldAccent),
                    modifier = Modifier.weight(1f).testTag("footer_admin_btn")
                ) {
                    Text("Portal Admin", fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "© 2026 MI Muhammadiyah Tanjung Qencono. Hak Cipta Dilindungi.",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                color = Color.White.copy(alpha = 0.6f),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
