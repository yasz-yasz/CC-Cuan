package com.cccuan.app.util

import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

object Formatters {
    private val idLocale = Locale("id", "ID")
    private val numberFormat: NumberFormat
        get() = NumberFormat.getNumberInstance(idLocale).apply {
            maximumFractionDigits = 0
        }

    private val shortDateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy", idLocale)
    private val fullDateFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", idLocale)

    fun formatRupiah(amount: Long): String {
        val prefix = if (amount < 0L) "-Rp " else "Rp "
        val absVal = kotlin.math.abs(amount)
        return prefix + numberFormat.format(absVal)
    }

    fun formatDateShort(epochDay: Long): String {
        return LocalDate.ofEpochDay(epochDay).format(shortDateFormatter)
    }

    fun formatDateShort(date: LocalDate): String {
        return date.format(shortDateFormatter)
    }

    fun formatDateFull(date: LocalDate): String {
        return date.format(fullDateFormatter)
    }

    fun parseRupiahInput(raw: String): Long? {
        val digits = raw.filter { it.isDigit() }
        if (digits.isEmpty()) return null
        return digits.toLongOrNull()
    }
}
