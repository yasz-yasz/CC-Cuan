package com.cccuan.app.data

data class CreditCard(
    val id: Long = 0L,
    val name: String,
    val bankName: String,
    val lastFourDigits: String = "",
    val creditLimit: Long,
    val billingDay: Int, // 1..31 tanggal cetak tagihan
    val dueDay: Int,     // 1..31 tanggal jatuh tempo
    val colorIndex: Int = 0,
    val reminderEnabled: Boolean = true
)

enum class RecordType(val dbValue: String) {
    TRANSACTION("TRANSACTION"),
    PAYMENT("PAYMENT");

    companion object {
        fun fromDb(value: String): RecordType {
            return if (value == PAYMENT.dbValue) PAYMENT else TRANSACTION
        }
    }
}

data class CardRecord(
    val id: Long = 0L,
    val cardId: Long,
    val type: RecordType,
    val title: String,
    val amount: Long,
    val dateEpochDay: Long, // LocalDate.toEpochDay()
    val category: String = "",
    val note: String = ""
)

data class CardSummary(
    val card: CreditCard,
    val totalTransactions: Long,
    val totalPayments: Long,
    val currentBalance: Long,
    val availableLimit: Long,
    val utilizationRatio: Float
)

val CARD_COLOR_PRESETS: List<Pair<Long, Long>> = listOf(
    0xFF0F172AL to 0xFF1E3A8AL, // Midnight Navy
    0xFF581C2EL to 0xFF881337L, // Deep Maroon
    0xFF064E3BL to 0xFF0F766EL, // Emerald Slate
    0xFF1F2937L to 0xFF374151L, // Graphite Charcoal
    0xFF451A03L to 0xFF92400EL, // Dark Bronze
    0xFF312E81L to 0xFF4C1D95L  // Royal Indigo
)

val TRANSACTION_CATEGORIES: List<String> = listOf(
    "Belanja",
    "Makan & Minum",
    "Travel & Hotel",
    "Tagihan & Utilitas",
    "QRIS / Online",
    "Cicilan",
    "Lainnya"
)
