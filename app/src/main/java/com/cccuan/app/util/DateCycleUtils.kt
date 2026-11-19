package com.cccuan.app.util

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.temporal.ChronoUnit

object DateCycleUtils {

    fun safeDateInMonth(yearMonth: YearMonth, desiredDay: Int): LocalDate {
        val clampedDay = desiredDay.coerceIn(1, yearMonth.lengthOfMonth())
        return yearMonth.atDay(clampedDay)
    }

    /**
     * Menghitung tanggal jatuh tempo terdekat (hari ini atau mendatang).
     */
    fun getNextDueDate(dueDay: Int, today: LocalDate = LocalDate.now()): LocalDate {
        val currentMonth = YearMonth.from(today)
        val candidate = safeDateInMonth(currentMonth, dueDay)
        return if (!candidate.isBefore(today)) {
            candidate
        } else {
            safeDateInMonth(currentMonth.plusMonths(1), dueDay)
        }
    }

    /**
     * Menghitung tanggal cetak tagihan berikutnya.
     */
    fun getNextBillingDate(billingDay: Int, today: LocalDate = LocalDate.now()): LocalDate {
        val currentMonth = YearMonth.from(today)
        val candidate = safeDateInMonth(currentMonth, billingDay)
        return if (!candidate.isBefore(today)) {
            candidate
        } else {
            safeDateInMonth(currentMonth.plusMonths(1), billingDay)
        }
    }

    /**
     * Sisa hari menuju jatuh tempo terdekat.
     */
    fun daysUntilDue(dueDay: Int, today: LocalDate = LocalDate.now()): Long {
        val nextDue = getNextDueDate(dueDay, today)
        return ChronoUnit.DAYS.between(today, nextDue)
    }

    /**
     * Menghitung waktu alarm pengingat tepat 7 hari sebelum jatuh tempo (pukul 08:00).
     * Jika jadwal H-7 bulan ini sudah lewat, jadwalkan ke H-7 siklus bulan berikutnya.
     */
    fun getNextReminderDateTime(
        dueDay: Int,
        now: LocalDateTime = LocalDateTime.now(),
        reminderHour: Int = 8,
        reminderMinute: Int = 0
    ): Pair<LocalDateTime, LocalDate> {
        val today = now.toLocalDate()
        val currentYm = YearMonth.from(today)

        for (monthOffset in 0L..2L) {
            val targetDue = safeDateInMonth(currentYm.plusMonths(monthOffset), dueDay)
            val reminderDateTime = targetDue.minusDays(7).atTime(reminderHour, reminderMinute)
            if (reminderDateTime.isAfter(now)) {
                return reminderDateTime to targetDue
            }
        }
        val fallbackDue = safeDateInMonth(currentYm.plusMonths(1), dueDay)
        return fallbackDue.minusDays(7).atTime(reminderHour, reminderMinute) to fallbackDue
    }
}
