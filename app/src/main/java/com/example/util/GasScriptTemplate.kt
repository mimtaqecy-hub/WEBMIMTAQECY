package com.example.util

object GasScriptTemplate {

    const val INSTRUCTIONS = """
PANDUAN LENGKAP MENGHUBUNGKAN APLIKASI KE GOOGLE SHEETS:

A. METODE 1: MENGGUNAKAN GOOGLE APPS SCRIPT (SINKRONISASI 2 ARAH OTOMATIS)
1. Buka https://script.google.com/ dan buat "Proyek Baru" (New Project).
2. Beri nama: "Backend MI Muhammadiyah Tanjung Qencono".
3. Hapus kode default di Code.gs, lalu Salin seluruh kode Code.gs di bawah ini.
4. Buat Google Spreadsheet baru di Google Drive (https://sheets.google.com).
5. Salin ID Spreadsheet dari URL browser Anda:
   Contoh URL: https://docs.google.com/spreadsheets/d/1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms/edit
   ID Spreadsheet adalah: 1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms
6. Ganti teks "MASUKKAN_SPREADSHEET_ID_DISINI" pada script dengan ID spreadsheet Anda.
7. Klik tombol "Deploy" (Terapkan) -> "New deployment" (Deployment baru).
8. Pilih jenis roda gerigi -> "Web app" (Aplikasi Web):
   - Description: "Versi 1.0"
   - Execute as: "Me" (Email Google Anda)
   - Who has access: "Anyone" (Siapa saja, bahkan tanpa login)
9. Klik "Deploy", beri izin (Authorize Access -> Advanced -> Go to ...).
10. Salin URL Web App yang berakhiran "/exec" dan tempelkan ke kolom URL Google Apps Script di aplikasi Android ini!

B. METODE 2: TAUTAN LANGSUNG SPREADSHEET (IMPOR DATA SISWA CEPAT)
1. Buka spreadsheet siswa Anda di Google Sheets.
2. Klik tombol "Bagikan" (Share) di kanan atas -> Ubah akses menjadi "Siapa saja yang memiliki tautan dapat melihat" (Anyone with link can view).
3. Salin tautan spreadsheet atau ID Spreadsheet, lalu tempelkan ke kolom "ID / Tautan Google Sheet" di aplikasi.
4. Tekan tombol "Tarik Data Siswa dari Google Sheet" untuk mengimpor seluruh data langsung ke database aplikasi!
"""

    const val CODE_GS = """
/**
 * BACKEND GOOGLE APPS SCRIPT & GOOGLE SHEETS
 * MI MUHAMMADIYAH TANJUNG QENCONO
 * Sinkronisasi Real-Time: Siswa, PPDB, Kontak, dan Notifikasi
 */

const SPREADSHEET_ID = "MASUKKAN_SPREADSHEET_ID_DISINI";

function getSpreadsheet() {
  if (!SPREADSHEET_ID || SPREADSHEET_ID === "MASUKKAN_SPREADSHEET_ID_DISINI") {
    // Jika script terikat langsung pada Google Sheet (Container-bound)
    try {
      return SpreadsheetApp.getActiveSpreadsheet();
    } catch (e) {
      throw new Error("Harap masukkan SPREADSHEET_ID yang valid pada baris 8!");
    }
  }
  return SpreadsheetApp.openById(SPREADSHEET_ID);
}

function doGet(e) {
  try {
    const action = (e && e.parameter && e.parameter.action) || "ping";
    const ss = getSpreadsheet();
    
    if (action === "ping") {
      return responseJson({
        status: "success",
        message: "Server Google Apps Script Aktif & Siap Digunakan",
        spreadsheetName: ss.getName(),
        timestamp: new Date().toISOString()
      });
    }
    
    if (action === "getStudents") {
      const sheet = ss.getSheetByName("Siswa") || ss.insertSheet("Siswa");
      const values = sheet.getDataRange().getValues();
      if (values.length <= 1) {
        return responseJson({ status: "success", data: [] });
      }
      const students = [];
      for (let i = 1; i < values.length; i++) {
        const row = values[i];
        if (row[0]) {
          students.push({
            fullName: String(row[0] || ""),
            nisn: String(row[1] || ""),
            birthPlace: String(row[2] || ""),
            birthDate: String(row[3] || ""),
            classLevel: Number(row[4] || 1),
            rombel: String(row[5] || "Ar Rahman"),
            gender: String(row[6] || "Laki-laki"),
            address: String(row[7] || "Tanjung Qencono")
          });
        }
      }
      return responseJson({ status: "success", data: students });
    }
    
    if (action === "getPpdb") {
      const sheet = ss.getSheetByName("PPDB") || ss.insertSheet("PPDB");
      const values = sheet.getDataRange().getValues();
      return responseJson({ status: "success", data: values });
    }
    
    return responseJson({ status: "error", message: "Action doGet tidak dikenal" });
  } catch (err) {
    return responseJson({ status: "error", message: err.toString() });
  }
}

function doPost(e) {
  try {
    let postData = {};
    if (e && e.postData && e.postData.contents) {
      postData = JSON.parse(e.postData.contents);
    }
    const action = postData.action;
    const ss = getSpreadsheet();
    
    // 1. Submit Single PPDB
    if (action === "submitPpdb") {
      const sheet = ss.getSheetByName("PPDB") || ss.insertSheet("PPDB");
      if (sheet.getLastRow() === 0) {
        sheet.appendRow([
          "No Pendaftaran", "Nama Siswa", "NISN", "NIK", "Tempat Lahir", "Tanggal Lahir",
          "Jenis Kelamin", "Alamat", "No WhatsApp", "Asal Sekolah", "Nama Ayah", "Nama Ibu",
          "Status Pendaftaran", "Waktu Pendaftaran"
        ]);
        sheet.getRange(1, 1, 1, 14).setFontWeight("bold").setBackground("#D9E1F2");
      }
      const d = postData.data;
      sheet.appendRow([
        d.regNumber || "", d.studentName || "", d.nisn || "", d.nik || "",
        d.birthPlace || "", d.birthDate || "", d.gender || "", d.address || "",
        d.whatsapp || "", d.prevSchool || "", d.fatherName || "", d.motherName || "",
        d.status || "Menunggu Verifikasi", new Date()
      ]);
      return responseJson({ status: "success", message: "Pendaftaran PPDB berhasil masuk ke Google Sheets!" });
    }
    
    // 2. Batch Sync All PPDB
    if (action === "syncAllPpdb") {
      const sheet = ss.getSheetByName("PPDB") || ss.insertSheet("PPDB");
      sheet.clearContents();
      sheet.appendRow([
        "No Pendaftaran", "Nama Siswa", "NISN", "NIK", "Tempat Lahir", "Tanggal Lahir",
        "Jenis Kelamin", "Alamat", "No WhatsApp", "Asal Sekolah", "Nama Ayah", "Nama Ibu",
        "Status Verifikasi", "Tanggal Daftar", "Catatan Verifikasi"
      ]);
      sheet.getRange(1, 1, 1, 15).setFontWeight("bold").setBackground("#D9E1F2");
      
      const list = postData.data || [];
      const rows = list.map(d => [
        d.regNumber, d.studentName, d.nisn, d.nik, d.birthPlace, d.birthDate,
        d.gender, d.address, d.whatsapp, d.prevSchool, d.fatherName, d.motherName,
        d.status, d.regDate, d.notes || ""
      ]);
      if (rows.length > 0) {
        sheet.getRange(2, 1, rows.length, 15).setValues(rows);
      }
      return responseJson({ status: "success", message: rows.length + " data PPDB berhasil diperbarui di Google Sheets" });
    }
    
    // 3. Batch Sync All Students
    if (action === "syncAllStudents") {
      const sheet = ss.getSheetByName("Siswa") || ss.insertSheet("Siswa");
      sheet.clearContents();
      sheet.appendRow([
        "Nama Lengkap", "NISN", "Tempat Lahir", "Tanggal Lahir", "Kelas", "Rombel", "Jenis Kelamin", "Alamat"
      ]);
      sheet.getRange(1, 1, 1, 8).setFontWeight("bold").setBackground("#E2EFDA");
      
      const list = postData.data || [];
      const rows = list.map(s => [
        s.fullName, s.nisn, s.birthPlace, s.birthDate, s.classLevel, s.rombel, s.gender, s.address
      ]);
      if (rows.length > 0) {
        sheet.getRange(2, 1, rows.length, 8).setValues(rows);
      }
      return responseJson({ status: "success", message: rows.length + " data siswa berhasil disinkronkan ke Google Sheets" });
    }
    
    // 4. Submit Contact Message
    if (action === "submitContact") {
      const sheet = ss.getSheetByName("PesanKontak") || ss.insertSheet("PesanKontak");
      if (sheet.getLastRow() === 0) {
        sheet.appendRow(["Nama Pengirim", "Email", "No HP/WA", "Subjek", "Isi Pesan", "Waktu Kirim"]);
        sheet.getRange(1, 1, 1, 6).setFontWeight("bold").setBackground("#FFF2CC");
      }
      const d = postData.data;
      sheet.appendRow([d.senderName || "", d.email || "", d.phone || "", d.subject || "", d.message || "", new Date()]);
      return responseJson({ status: "success", message: "Pesan kontak berhasil dicatat di Google Sheets" });
    }

    // 5. Submit Notification Log
    if (action === "submitNotification") {
      const sheet = ss.getSheetByName("Notifikasi") || ss.insertSheet("Notifikasi");
      if (sheet.getLastRow() === 0) {
        sheet.appendRow(["Judul Notifikasi", "Kategori", "Sasaran", "Pesan Lengkap", "Waktu Siar"]);
        sheet.getRange(1, 1, 1, 5).setFontWeight("bold").setBackground("#FCE4D6");
      }
      const d = postData.data;
      sheet.appendRow([d.title || "", d.category || "", d.target || "", d.message || "", new Date()]);
      return responseJson({ status: "success", message: "Notifikasi tercatat di Google Sheets" });
    }
    
    return responseJson({ status: "error", message: "Action doPost tidak dikenal: " + action });
  } catch (err) {
    return responseJson({ status: "error", message: err.toString() });
  }
}

function responseJson(data) {
  return ContentService.createTextOutput(JSON.stringify(data))
    .setMimeType(ContentService.MimeType.JSON);
}
"""
}
