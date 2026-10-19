package com.example.util

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FilePickerHelper {

    fun copyUriToInternalStorage(context: Context, uri: Uri, subfolder: String, prefix: String): String {
        return try {
            val contentResolver = context.contentResolver
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val mimeType = contentResolver.getType(uri)
            val extension = when {
                mimeType?.contains("image/png") == true -> ".png"
                mimeType?.contains("image/jpeg") == true -> ".jpg"
                mimeType?.contains("image/webp") == true -> ".webp"
                mimeType?.contains("audio/mpeg") == true || mimeType?.contains("mp3") == true -> ".mp3"
                mimeType?.contains("audio/wav") == true -> ".wav"
                mimeType?.contains("audio/ogg") == true -> ".ogg"
                mimeType?.contains("audio/m4a") == true || mimeType?.contains("audio/mp4") == true -> ".m4a"
                else -> {
                    val lastSegment = uri.lastPathSegment ?: ""
                    if (lastSegment.contains(".")) ".${lastSegment.substringAfterLast('.')}" else if (subfolder == "audio") ".mp3" else ".jpg"
                }
            }

            val dir = File(context.filesDir, subfolder)
            if (!dir.exists()) dir.mkdirs()

            val targetFile = File(dir, "${prefix}_$timeStamp$extension")
            contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(targetFile).use { output ->
                    input.copyTo(output)
                }
            }
            targetFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            uri.toString()
        }
    }
}
