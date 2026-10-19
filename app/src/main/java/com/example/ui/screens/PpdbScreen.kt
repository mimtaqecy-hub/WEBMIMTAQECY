package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.PpdbEntity
import com.example.ui.components.AppFooter
import com.example.ui.components.SectionHeader
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.IslamicGreen
import com.example.ui.theme.NavyDeep
import com.example.ui.theme.NavyPrimary
import com.example.ui.viewmodel.MadrasahViewModel
import com.example.util.FilePickerHelper
import java.io.File

@Composable
fun PpdbScreen(viewModel: MadrasahViewModel) {
    val context = LocalContext.current
    var currentStep by remember { mutableStateOf(1) } // 1: Siswa, 2: Ayah, 3: Ibu, 4: Dokumen

    // Form Fields - Siswa
    var studentName by remember { mutableStateOf("") }
    var nisn by remember { mutableStateOf("") }
    var nik by remember { mutableStateOf("") }
    var birthPlace by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("Laki-laki") }
    var address by remember { mutableStateOf("") }
    var whatsapp by remember { mutableStateOf("") }
    var prevSchool by remember { mutableStateOf("") }

    // Form Fields - Ayah
    var fatherName by remember { mutableStateOf("") }
    var fatherNik by remember { mutableStateOf("") }
    var fatherBirthPlace by remember { mutableStateOf("") }
    var fatherBirthDate by remember { mutableStateOf("") }
    var fatherEducation by remember { mutableStateOf("SMA") }
    var fatherJob by remember { mutableStateOf("") }

    // Form Fields - Ibu
    var motherName by remember { mutableStateOf("") }
    var motherNik by remember { mutableStateOf("") }
    var motherBirthPlace by remember { mutableStateOf("") }
    var motherBirthDate by remember { mutableStateOf("") }
    var motherEducation by remember { mutableStateOf("SMA") }
    var motherJob by remember { mutableStateOf("") }

    // Dokumen Files
    var kkUploaded by remember { mutableStateOf("KK_terlampir.pdf") }
    var ijazahUploaded by remember { mutableStateOf("Ijazah_TK_RA.pdf") }
    var aktaUploaded by remember { mutableStateOf("Akta_Kelahiran.pdf") }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    val lastSubmittedPpdb by viewModel.lastSubmittedPpdb.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
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
                    text = "Penerimaan Peserta Didik Baru (PPDB)",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Text(
                    text = "Tahun Pelajaran 2026/2027 • MI Muhammadiyah Tanjung Qencono",
                    style = MaterialTheme.typography.bodyMedium,
                    color = GoldAccent
                )
            }
        }

        // Stepper Progress Indicators
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StepBubble(step = 1, current = currentStep, label = "Siswa")
            HorizontalDivider(modifier = Modifier.weight(1f).padding(horizontal = 4.dp), color = if (currentStep > 1) GoldAccent else MaterialTheme.colorScheme.outline)
            StepBubble(step = 2, current = currentStep, label = "Ayah")
            HorizontalDivider(modifier = Modifier.weight(1f).padding(horizontal = 4.dp), color = if (currentStep > 2) GoldAccent else MaterialTheme.colorScheme.outline)
            StepBubble(step = 3, current = currentStep, label = "Ibu")
            HorizontalDivider(modifier = Modifier.weight(1f).padding(horizontal = 4.dp), color = if (currentStep > 3) GoldAccent else MaterialTheme.colorScheme.outline)
            StepBubble(step = 4, current = currentStep, label = "Berkas")
        }

        // Error message banner
        if (errorMessage != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(errorMessage!!, color = MaterialTheme.colorScheme.onErrorContainer, fontSize = 13.sp)
                }
            }
        }

        // STEP 1: DATA SISWA
        if (currentStep == 1) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SectionHeader(title = "Tahap 1: Data Calon Siswa", icon = Icons.Default.Person)

                    OutlinedTextField(
                        value = studentName,
                        onValueChange = { studentName = it },
                        label = { Text("Nama Lengkap Siswa *") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).testTag("ppdb_input_name")
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = nisn,
                            onValueChange = { nisn = it },
                            label = { Text("NISN (Jika ada)") },
                            modifier = Modifier.weight(1f).padding(vertical = 4.dp)
                        )
                        OutlinedTextField(
                            value = nik,
                            onValueChange = { nik = it },
                            label = { Text("NIK Siswa *") },
                            modifier = Modifier.weight(1f).padding(vertical = 4.dp)
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = birthPlace,
                            onValueChange = { birthPlace = it },
                            label = { Text("Tempat Lahir *") },
                            modifier = Modifier.weight(1f).padding(vertical = 4.dp)
                        )
                        OutlinedTextField(
                            value = birthDate,
                            onValueChange = { birthDate = it },
                            label = { Text("Tgl Lahir (Contoh: 15 Mei 2019) *") },
                            modifier = Modifier.weight(1f).padding(vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Jenis Kelamin *", style = MaterialTheme.typography.labelMedium)
                    Row(modifier = Modifier.padding(vertical = 4.dp)) {
                        FilterChip(
                            selected = gender == "Laki-laki",
                            onClick = { gender = "Laki-laki" },
                            label = { Text("Laki-laki") },
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        FilterChip(
                            selected = gender == "Perempuan",
                            onClick = { gender = "Perempuan" },
                            label = { Text("Perempuan") }
                        )
                    }

                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Alamat Tempat Tinggal Lengkap *") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )

                    OutlinedTextField(
                        value = whatsapp,
                        onValueChange = { whatsapp = it },
                        label = { Text("Nomor WhatsApp Orang Tua / Wali *") },
                        placeholder = { Text("0853xxxxxxxx") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )

                    OutlinedTextField(
                        value = prevSchool,
                        onValueChange = { prevSchool = it },
                        label = { Text("Asal Sekolah (TK / RA / PAUD) *") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            if (studentName.isBlank() || birthPlace.isBlank() || birthDate.isBlank() || address.isBlank() || whatsapp.isBlank()) {
                                errorMessage = "Mohon lengkapi seluruh kolom yang bertanda bintang (*)"
                            } else {
                                errorMessage = null
                                currentStep = 2
                            }
                        },
                        modifier = Modifier.fillMaxWidth().testTag("ppdb_btn_next_1")
                    ) {
                        Text("Lanjut ke Data Ayah")
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(Icons.Default.ArrowForward, contentDescription = null)
                    }
                }
            }
        }

        // STEP 2: DATA AYAH
        if (currentStep == 2) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SectionHeader(title = "Tahap 2: Data Ayah Kandung", icon = Icons.Default.FamilyRestroom)

                    OutlinedTextField(
                        value = fatherName,
                        onValueChange = { fatherName = it },
                        label = { Text("Nama Lengkap Ayah *") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )

                    OutlinedTextField(
                        value = fatherNik,
                        onValueChange = { fatherNik = it },
                        label = { Text("NIK Ayah *") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = fatherBirthPlace,
                            onValueChange = { fatherBirthPlace = it },
                            label = { Text("Tempat Lahir") },
                            modifier = Modifier.weight(1f).padding(vertical = 4.dp)
                        )
                        OutlinedTextField(
                            value = fatherBirthDate,
                            onValueChange = { fatherBirthDate = it },
                            label = { Text("Tanggal Lahir") },
                            modifier = Modifier.weight(1f).padding(vertical = 4.dp)
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = fatherEducation,
                            onValueChange = { fatherEducation = it },
                            label = { Text("Pendidikan") },
                            modifier = Modifier.weight(1f).padding(vertical = 4.dp)
                        )
                        OutlinedTextField(
                            value = fatherJob,
                            onValueChange = { fatherJob = it },
                            label = { Text("Pekerjaan *") },
                            modifier = Modifier.weight(1f).padding(vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { currentStep = 1 }, modifier = Modifier.weight(1f)) {
                            Text("Kembali")
                        }
                        Button(
                            onClick = {
                                if (fatherName.isBlank()) {
                                    errorMessage = "Mohon isi nama ayah kandung."
                                } else {
                                    errorMessage = null
                                    currentStep = 3
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Lanjut ke Data Ibu")
                        }
                    }
                }
            }
        }

        // STEP 3: DATA IBU
        if (currentStep == 3) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SectionHeader(title = "Tahap 3: Data Ibu Kandung", icon = Icons.Default.FamilyRestroom)

                    OutlinedTextField(
                        value = motherName,
                        onValueChange = { motherName = it },
                        label = { Text("Nama Lengkap Ibu *") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )

                    OutlinedTextField(
                        value = motherNik,
                        onValueChange = { motherNik = it },
                        label = { Text("NIK Ibu *") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = motherBirthPlace,
                            onValueChange = { motherBirthPlace = it },
                            label = { Text("Tempat Lahir") },
                            modifier = Modifier.weight(1f).padding(vertical = 4.dp)
                        )
                        OutlinedTextField(
                            value = motherBirthDate,
                            onValueChange = { motherBirthDate = it },
                            label = { Text("Tanggal Lahir") },
                            modifier = Modifier.weight(1f).padding(vertical = 4.dp)
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = motherEducation,
                            onValueChange = { motherEducation = it },
                            label = { Text("Pendidikan") },
                            modifier = Modifier.weight(1f).padding(vertical = 4.dp)
                        )
                        OutlinedTextField(
                            value = motherJob,
                            onValueChange = { motherJob = it },
                            label = { Text("Pekerjaan *") },
                            modifier = Modifier.weight(1f).padding(vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { currentStep = 2 }, modifier = Modifier.weight(1f)) {
                            Text("Kembali")
                        }
                        Button(
                            onClick = {
                                if (motherName.isBlank()) {
                                    errorMessage = "Mohon isi nama ibu kandung."
                                } else {
                                    errorMessage = null
                                    currentStep = 4
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Lanjut ke Berkas")
                        }
                    }
                }
            }
        }

        // STEP 4: DOKUMEN & FINAL SUBMIT
        if (currentStep == 4) {
            val kkPickerLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.PickVisualMedia()
            ) { uri: Uri? ->
                if (uri != null) {
                    kkUploaded = FilePickerHelper.copyUriToInternalStorage(context, uri, "ppdb", "kk")
                }
            }

            val ijazahPickerLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.PickVisualMedia()
            ) { uri: Uri? ->
                if (uri != null) {
                    ijazahUploaded = FilePickerHelper.copyUriToInternalStorage(context, uri, "ppdb", "ijazah")
                }
            }

            val aktaPickerLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.PickVisualMedia()
            ) { uri: Uri? ->
                if (uri != null) {
                    aktaUploaded = FilePickerHelper.copyUriToInternalStorage(context, uri, "ppdb", "akta")
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SectionHeader(title = "Tahap 4: Unggah Dokumen Pendukung", icon = Icons.Default.UploadFile)
                    Text(
                        text = "Unggah berkas foto dari perangkat (Kartu Keluarga, Ijazah/SKL, dan Akta Kelahiran). Berkas tersimpan aman.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    DocUploadItem(
                        title = "1. Kartu Keluarga (KK)",
                        fileName = kkUploaded,
                        onPick = { kkPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
                    )

                    DocUploadItem(
                        title = "2. Ijazah / Surat Keterangan TK/RA",
                        fileName = ijazahUploaded,
                        onPick = { ijazahPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
                    )

                    DocUploadItem(
                        title = "3. Akta Kelahiran Anak",
                        fileName = aktaUploaded,
                        onPick = { aktaPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Pernyataan Kebenaran Data:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text(
                                "Dengan mengirim formulir ini, saya menyatakan seluruh data yang diisi adalah benar dan siap mematuhi tata tertib MI Muhammadiyah Tanjung Qencono.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { currentStep = 3 }, modifier = Modifier.weight(1f)) {
                            Text("Kembali")
                        }
                        Button(
                            onClick = {
                                viewModel.submitPpdbForm(
                                    studentName = studentName,
                                    nisn = nisn,
                                    nik = nik,
                                    birthPlace = birthPlace,
                                    birthDate = birthDate,
                                    gender = gender,
                                    address = address,
                                    whatsapp = whatsapp,
                                    prevSchool = prevSchool,
                                    fatherName = fatherName,
                                    fatherNik = fatherNik,
                                    fatherBirthPlace = fatherBirthPlace,
                                    fatherBirthDate = fatherBirthDate,
                                    fatherEducation = fatherEducation,
                                    fatherJob = fatherJob,
                                    motherName = motherName,
                                    motherNik = motherNik,
                                    motherBirthPlace = motherBirthPlace,
                                    motherBirthDate = motherBirthDate,
                                    motherEducation = motherEducation,
                                    motherJob = motherJob,
                                    kkName = kkUploaded,
                                    ijazahName = ijazahUploaded,
                                    aktaName = aktaUploaded
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = IslamicGreen),
                            modifier = Modifier.weight(1f).testTag("ppdb_submit_btn")
                        ) {
                            Text("Kirim Pendaftaran", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        AppFooter(viewModel = viewModel)
    }

    // Modal Bukti Pendaftaran (Setelah submit berhasil)
    if (lastSubmittedPpdb != null) {
        val ppdb = lastSubmittedPpdb!!
        AlertDialog(
            onDismissRequest = { viewModel.dismissPpdbSuccessDialog() },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = IslamicGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Pendaftaran Berhasil!")
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("NOMOR REGISTRASI PPDB:", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            Text(ppdb.regNumber, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Nama Siswa: ${ppdb.studentName}", fontWeight = FontWeight.Bold)
                    Text("TTL: ${ppdb.birthPlace}, ${ppdb.birthDate}")
                    Text("Jenis Kelamin: ${ppdb.gender}")
                    Text("Asal Sekolah: ${ppdb.prevSchool}")
                    Text("Nama Ayah/Ibu: ${ppdb.fatherName} / ${ppdb.motherName}")
                    Text("Nomor WA: ${ppdb.whatsapp}")
                    Text("Status: ${ppdb.status}", color = GoldAccent, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CloudDone, contentDescription = null, tint = IslamicGreen, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Tersimpan di Sistem & Terhubung ke Database Cloud", fontSize = 11.sp, color = IslamicGreen, fontWeight = FontWeight.Medium)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        "Simpan nomor pendaftaran ini sebagai bukti pendaftaran. Pihak panitia PPDB madrasah akan menghubungi Anda melalui WhatsApp.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val shareText = "BUKTI PENDAFTARAN PPDB MI MUHAMMADIYAH TANJUNG QENCONO\nNo. Pendaftaran: ${ppdb.regNumber}\nNama Siswa: ${ppdb.studentName}\nTanggal: ${ppdb.regDate}\nStatus: ${ppdb.status}"
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, shareText)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Bagikan Bukti Pendaftaran"))
                    }
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Bagikan / Simpan PDF")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { viewModel.dismissPpdbSuccessDialog() }) {
                    Text("Selesai")
                }
            }
        )
    }
}

@Composable
fun StepBubble(step: Int, current: Int, label: String) {
    val isDone = current > step
    val isActive = current == step

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(
                    when {
                        isDone -> IslamicGreen
                        isActive -> NavyPrimary
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                )
                .border(
                    width = if (isActive) 2.dp else 0.dp,
                    color = GoldAccent,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isDone) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            } else {
                Text(
                    text = step.toString(),
                    color = if (isActive) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
            color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
fun DocUploadItem(
    title: String,
    fileName: String,
    onPick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        if (fileName.startsWith("/") && File(fileName).exists()) {
            AsyncImage(
                model = File(fileName),
                contentDescription = title,
                modifier = Modifier
                    .size(42.dp)
                    .background(Color.White, RoundedCornerShape(6.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(10.dp))
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
            Text(
                text = if (fileName.startsWith("/")) File(fileName).name else fileName,
                style = MaterialTheme.typography.bodySmall,
                color = IslamicGreen
            )
        }
        OutlinedButton(
            onClick = onPick,
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Pilih Foto", fontSize = 11.sp)
        }
    }
}
