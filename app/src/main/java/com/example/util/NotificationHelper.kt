package com.example.util

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import java.util.Calendar

object NotificationHelper {

    const val CHANNEL_ID = "mim_madrasah_notifications"
    const val CHANNEL_NAME = "Notifikasi MIM Tanjung Qencono"
    const val CHANNEL_DESC = "Pemberitahuan resmi pengumuman, jadwal masuk, dan info madrasah"
    const val ACTION_TRIGGER_SCHEDULE_REMINDER = "com.example.ACTION_SCHEDULE_REMINDER"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build()

            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
                enableLights(true)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 300, 200, 300)
                setSound(soundUri, audioAttributes)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.createNotificationChannel(channel)
        }
    }

    fun showNotification(
        context: Context,
        title: String,
        message: String,
        category: String = "Pengumuman",
        soundAlert: Boolean = true
    ) {
        createNotificationChannel(context)

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        // Intent to open app
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("EXTRA_NAVIGATE_TO", "NOTIFICATIONS")
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_madrasah_official_logo)
            .setContentTitle("[$category] $title")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(0, 350, 200, 350))
            .setSound(soundUri)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        val notificationId = (System.currentTimeMillis() % 100000).toInt()
        notificationManager?.notify(notificationId, builder.build())

        // Sound alert playback
        if (soundAlert) {
            playAudibleAlert(context)
        }
    }

    fun playAudibleAlert(context: Context) {
        try {
            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val ringtone = RingtoneManager.getRingtone(context.applicationContext, soundUri)
            ringtone?.play()

            // Vibrate
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createWaveform(longArrayOf(0, 250, 150, 250), -1)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 250, 150, 250), -1))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(longArrayOf(0, 250, 150, 250), -1)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Jadwal pengingat jam masuk di latar belakang via AlarmManager
     */
    fun scheduleDailySchoolEntryReminder(context: Context, hour: Int = 6, minute: Int = 30) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            val intent = Intent(context, NotificationBroadcastReceiver::class.java).apply {
                action = ACTION_TRIGGER_SCHEDULE_REMINDER
                putExtra("TITLE", "⏰ Waktunya Berangkat ke Madrasah!")
                putExtra("MESSAGE", "Jam masuk madrasah pukul 07.00 WIB. Mari persiapkan diri untuk sholat Dhuha dan apel pagi!")
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                1001,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                if (before(Calendar.getInstance())) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

class NotificationBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val title = intent?.getStringExtra("TITLE") ?: "⏰ Pengingat Madrasah"
        val message = intent?.getStringExtra("MESSAGE") ?: "Informasi terbaru dari MI Muhammadiyah Tanjung Qencono"
        NotificationHelper.showNotification(
            context = context,
            title = title,
            message = message,
            category = "Jadwal Masuk",
            soundAlert = true
        )
    }
}
