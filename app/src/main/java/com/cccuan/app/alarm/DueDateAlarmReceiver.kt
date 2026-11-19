package com.cccuan.app.alarm

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.cccuan.app.MainActivity
import com.cccuan.app.R
import com.cccuan.app.data.CcDatabaseHelper
import com.cccuan.app.util.DateCycleUtils
import com.cccuan.app.util.Formatters

class DueDateAlarmReceiver : BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID = "cc_cuan_due_date_alarm_v1"
        private const val CHANNEL_NAME = "Alarm Jatuh Tempo Kartu Kredit"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val cardId = intent.getLongExtra(AlarmScheduler.EXTRA_CARD_ID, -1L)
        val isTest = intent.getBooleanExtra(AlarmScheduler.EXTRA_IS_TEST, false)

        createAlarmChannel(context)

        val db = CcDatabaseHelper(context.applicationContext)
        val card = if (cardId > 0L) db.getCardById(cardId) else db.getAllCards().firstOrNull()

        val title: String
        val body: String

        if (card != null) {
            val balance = db.getCardOutstandingBalance(card.id)
            val nextDue = DateCycleUtils.getNextDueDate(card.dueDay)
            val daysLeft = DateCycleUtils.daysUntilDue(card.dueDay)
            val prefix = if (isTest) "[TES ALARM] " else ""
            title = "${prefix}Pengingat Jatuh Tempo: ${card.name}"
            val dueText = Formatters.formatDateFull(nextDue)
            val countdownText = when {
                daysLeft == 0L -> "Jatuh tempo HARI INI ($dueText)."
                else -> "Jatuh tempo H-$daysLeft pada $dueText."
            }
            body = "$countdownText Estimasi tagihan berjalan: ${Formatters.formatRupiah(balance)}. Segera siapkan pembayaran agar bebas bunga & denda."

            // Jadwalkan ulang alarm untuk siklus berikutnya jika bukan tes manual
            if (!isTest && card.reminderEnabled) {
                AlarmScheduler.scheduleDueReminder(context, card)
            }
        } else {
            title = "Pengingat Jatuh Tempo CC Cuan"
            body = "Cek tagihan kartu kredit kamu di aplikasi CC Cuan sekarang."
        }

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            cardId.toInt(),
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setSound(alarmSound)
            .setVibrate(longArrayOf(0, 500, 250, 500, 250, 500))
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)
            .build()

        val hasPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

        if (hasPermission) {
            try {
                val notifId = if (isTest) 99999 else (cardId.toInt() + 1000)
                NotificationManagerCompat.from(context).notify(notifId, notification)
            } catch (_: SecurityException) {
                // Abaikan jika izin ditolak sistem
            }
        }
    }

    private fun createAlarmChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (manager.getNotificationChannel(CHANNEL_ID) != null) return

            val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alarm pengingat otomatis 7 hari sebelum tanggal jatuh tempo kartu kredit."
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 250, 500, 250, 500)
                setSound(alarmSound, audioAttributes)
            }
            manager.createNotificationChannel(channel)
        }
    }
}
