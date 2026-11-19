package com.cccuan.app.data

import android.content.Context

class CcRepository(context: Context) {
    private val db = CcDatabaseHelper(context.applicationContext)

    fun getAllCards(): List<CreditCard> = db.getAllCards()
    fun getCardById(cardId: Long): CreditCard? = db.getCardById(cardId)
    fun insertCard(card: CreditCard): Long = db.insertCard(card)
    fun updateCard(card: CreditCard): Int = db.updateCard(card)
    fun deleteCard(cardId: Long): Int = db.deleteCard(cardId)

    fun getAllRecords(): List<CardRecord> = db.getAllRecords()
    fun insertRecord(record: CardRecord): Long = db.insertRecord(record)
    fun updateRecord(record: CardRecord): Int = db.updateRecord(record)
    fun deleteRecord(recordId: Long): Int = db.deleteRecord(recordId)

    fun buildCardSummaries(
        cards: List<CreditCard>,
        records: List<CardRecord>
    ): List<CardSummary> {
        val recordsByCard = records.groupBy { it.cardId }
        return cards.map { card ->
            val cardRecs = recordsByCard[card.id].orEmpty()
            val totalTx = cardRecs.filter { it.type == RecordType.TRANSACTION }.sumOf { it.amount }
            val totalPay = cardRecs.filter { it.type == RecordType.PAYMENT }.sumOf { it.amount }
            val balance = (totalTx - totalPay).coerceAtLeast(0L)
            val available = (card.creditLimit - balance).coerceAtLeast(0L)
            val ratio = if (card.creditLimit > 0L) {
                (balance.toFloat() / card.creditLimit.toFloat()).coerceIn(0f, 1f)
            } else {
                0f
            }
            CardSummary(
                card = card,
                totalTransactions = totalTx,
                totalPayments = totalPay,
                currentBalance = balance,
                availableLimit = available,
                utilizationRatio = ratio
            )
        }
    }
}
