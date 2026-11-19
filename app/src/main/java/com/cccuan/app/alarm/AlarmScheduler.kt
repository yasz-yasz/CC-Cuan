package com.cccuan.app.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.cccuan.app.data.CreditCard
import com.cccuan.app.util.DateCycleUtils
import java.time.ZoneId

object AlarmScheduler {

    const val EXTRA_CARD_ID = "extra_card_id"
    const val EXTRA_IS_TEST = "extra_is_test"

    fun scheduleDueReminder(context: Context, card: CreditCard) {
        if (!card.reminderEnabled) {
            cancelDueReminder(context, card.id)
            return
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val (reminderDateTime, _) = DateCycleUtils.getNextReminderDateTime(card.dueDay)
        val triggerMillis = reminderDateTime
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        val pendingIntent = buildPendingIntent(context, card.id, isTest = false)

        val canUseExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }

        try {
            if (canUseExact) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerMillis,
                    pendingIntent
                )
            }
        } catch (_: SecurityException) {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerMillis,
                pendingIntent
            )
        }
    }

    fun cancelDueReminder(context: Context, cardId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = buildPendingIntent(context, cardId, isTest = false)
        alarmManager.cancel(pendingIntent)
    }

    fun rescheduleAll(context: Context, cards: List<CreditCard>) {
        cards.forEach { card ->
            if (card.reminderEnabled) {
                scheduleDueReminder(context, card)
            } else {
                cancelDueReminder(context, card.id)
            }
        }
    }

    fun triggerTestAlarmNow(context: Context, cardId: Long) {
        val intent = Intent(context, DueDateAlarmReceiver::class.java).apply {
            putExtra(EXTRA_CARD_ID, cardId)
            putExtra(EXTRA_IS_TEST, true)
        }
        context.sendBroadcast(intent)
    }

    private fun buildPendingIntent(context: Context, cardId: Long, isTest: Boolean): PendingIntent {
        val intent = Intent(context, DueDateAlarmReceiver::class.java).apply {
            putExtra(EXTRA_CARD_ID, cardId)
            putExtra(EXTRA_IS_TEST, isTest)
        }
        val requestCode = (cardId.toInt() * 31) + 700
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
