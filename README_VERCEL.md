# PANDUAN DEPLOY KE VERCEL (VERCEL.APP)
## MI Muhammadiyah Tanjung Qencono

Berkas konfigurasi `vercel.json`, `package.json`, `index.html`, dan installer Android APK (`app-debug.apk`) sudah disiapkan di root proyek ini dan dioptimalkan agar **tidak terjadi tampilan blank (layar putih kosong)** saat dibuka.

---

### PENYEBAB & PERBAIKAN TAMPILAN BLANK DI VERCEL

Jika sebelumnya tampilan sempat blank (putih polos), berikut akar masalah dan perbaikan yang sudah kami terapkan:
1. **Build Command Kosong di Vercel:** Sebelumnya Vercel mendeteksi `package.json` dan mencoba menjalankan build script. Karena tidak ada bundler seperti Vite/Next.js, Vercel mencari folder output `dist/` yang tidak ada sehingga menghasilkan halaman kosong.
   👉 **Perbaikan:** `vercel.json` kini menggunakan `"buildCommand": null` dan `"outputDirectory": "."` agar Vercel langsung melayani file `index.html` secara instan tanpa proses build.
2. **Struktur File Ganda (Root & Public):** Berkas `index.html` dan `app-debug.apk` disiapkan di folder utama (`./`) dan folder `public/` sehingga Vercel pasti menemukannya apapun pengaturan default-nya.
3. **Pembersihan Header Iframe:** Header yang memblokir rendering di iframe atau pratinjau browser telah disesuaikan dengan `Access-Control-Allow-Origin: *`.

---

### METODE 1: Deploy Otomatis via GitHub (Paling Mudah & Disarankan)

1. **Upload / Push Proyek ke GitHub**:
   - Unggah atau push repositori ini ke akun GitHub Anda (misal: `github.com/username/mim-tanjung-qencono`).
2. **Buka Dashboard Vercel**:
   - Masuk ke [https://vercel.com](https://vercel.com) dengan akun GitHub atau Google Anda (`mimtaqecy@gmail.com`).
3. **Impor Proyek**:
   - Klik tombol **"Add New..."** -> **"Project"**.
   - Pilih repositori GitHub madrasah Anda, lalu klik **"Import"**.
4. **Pengaturan Project di Vercel**:
   - **Framework Preset**: Pilih **"Other"**
   - **Root Directory**: `./` (default)
   - **Build Command**: Biarkan kosong / nonaktifkan (karena sudah diatur `"buildCommand": null` di `vercel.json`)
   - **Output Directory**: Biarkan default `./`
5. **Klik "Deploy"**:
   - Dalam 15 - 30 detik, website madrasah langsung aktif dan tampil sempurna di:
     👉 **`https://mim-tanjung-qencono.vercel.app`**

---

### METODE 2: Deploy Menggunakan Vercel CLI (Lewat Komputer)

1. Buka Terminal / CMD di folder proyek ini.
2. Jalankan:
   ```bash
   npx vercel --prod
   ```
3. Website dan file APK langsung live online tanpa error!

---

### FITUR DI WEBSITE VERCEL.APP:
1. **Pendaftaran PPDB Online**: Calon wali murid bisa mengisi formulir langsung dari browser HP atau laptop dengan nomor registrasi otomatis.
2. **Unduh APK Android Langsung**: Tautan download langsung ke `/app-debug.apk` atau `/download` untuk memasang aplikasi di HP Android.
3. **Profil & Fasilitas Madrasah**: Sambutan Kepala Madrasah Ahmad Syaifuddin, S.Pd.I, foto fasilitas, visi, misi, dan sejarah.
4. **Jadwal Masuk & Apel Pagi**: Informasi jam hadir (06.45 - 07.00 WIB) dan ketentuan madrasah.
5. **Jadwal Sholat 5 Waktu & Dhuha**: Jadwal real-time wilayah Way Bungur, Lampung Timur.
6. **Hubungi Panitia via WhatsApp**: Tombol otomatis terhubung ke WhatsApp madrasah.
