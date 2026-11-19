package com.cccuan.app.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class CcDatabaseHelper(context: Context) : SQLiteOpenHelper(
    context,
    DATABASE_NAME,
    null,
    DATABASE_VERSION
) {
    companion object {
        private const val DATABASE_NAME = "cc_cuan.db"
        private const val DATABASE_VERSION = 2

        const val TABLE_CARDS = "credit_cards"
        const val COL_CARD_ID = "id"
        const val COL_CARD_NAME = "name"
        const val COL_BANK_NAME = "bank_name"
        const val COL_LAST_FOUR = "last_four"
        const val COL_CREDIT_LIMIT = "credit_limit"
        const val COL_BILLING_DAY = "billing_day"
        const val COL_DUE_DAY = "due_day"
        const val COL_COLOR_INDEX = "color_index"
        const val COL_REMINDER_ENABLED = "reminder_enabled"
        const val COL_CUSTOM_IMAGE_PATH = "custom_image_path"

        const val TABLE_RECORDS = "card_records"
        const val COL_REC_ID = "id"
        const val COL_REC_CARD_ID = "card_id"
        const val COL_REC_TYPE = "type"
        const val COL_REC_TITLE = "title"
        const val COL_REC_AMOUNT = "amount"
        const val COL_REC_DATE = "date_epoch_day"
        const val COL_REC_CATEGORY = "category"
        const val COL_REC_NOTE = "note"
    }

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE $TABLE_CARDS (
                $COL_CARD_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_CARD_NAME TEXT NOT NULL,
                $COL_BANK_NAME TEXT NOT NULL,
                $COL_LAST_FOUR TEXT NOT NULL DEFAULT '',
                $COL_CREDIT_LIMIT INTEGER NOT NULL,
                $COL_BILLING_DAY INTEGER NOT NULL,
                $COL_DUE_DAY INTEGER NOT NULL,
                $COL_COLOR_INDEX INTEGER NOT NULL DEFAULT 0,
                $COL_REMINDER_ENABLED INTEGER NOT NULL DEFAULT 1,
                $COL_CUSTOM_IMAGE_PATH TEXT NOT NULL DEFAULT ''
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE $TABLE_RECORDS (
                $COL_REC_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_REC_CARD_ID INTEGER NOT NULL,
                $COL_REC_TYPE TEXT NOT NULL,
                $COL_REC_TITLE TEXT NOT NULL,
                $COL_REC_AMOUNT INTEGER NOT NULL,
                $COL_REC_DATE INTEGER NOT NULL,
                $COL_REC_CATEGORY TEXT NOT NULL DEFAULT '',
                $COL_REC_NOTE TEXT NOT NULL DEFAULT '',
                FOREIGN KEY($COL_REC_CARD_ID) REFERENCES $TABLE_CARDS($COL_CARD_ID) ON DELETE CASCADE
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL(
                "ALTER TABLE $TABLE_CARDS ADD COLUMN $COL_CUSTOM_IMAGE_PATH TEXT NOT NULL DEFAULT ''"
            )
        }
    }

    fun getAllCards(): List<CreditCard> {
        val list = mutableListOf<CreditCard>()
        readableDatabase.query(
            TABLE_CARDS,
            null,
            null,
            null,
            null,
            null,
            "$COL_CARD_ID DESC"
        ).use { cursor ->
            val imgIdx = cursor.getColumnIndex(COL_CUSTOM_IMAGE_PATH)
            while (cursor.moveToNext()) {
                list.add(
                    CreditCard(
                        id = cursor.getLong(cursor.getColumnIndexOrThrow(COL_CARD_ID)),
                        name = cursor.getString(cursor.getColumnIndexOrThrow(COL_CARD_NAME)),
                        bankName = cursor.getString(cursor.getColumnIndexOrThrow(COL_BANK_NAME)),
                        lastFourDigits = cursor.getString(cursor.getColumnIndexOrThrow(COL_LAST_FOUR)),
                        creditLimit = cursor.getLong(cursor.getColumnIndexOrThrow(COL_CREDIT_LIMIT)),
                        billingDay = cursor.getInt(cursor.getColumnIndexOrThrow(COL_BILLING_DAY)),
                        dueDay = cursor.getInt(cursor.getColumnIndexOrThrow(COL_DUE_DAY)),
                        colorIndex = cursor.getInt(cursor.getColumnIndexOrThrow(COL_COLOR_INDEX)),
                        reminderEnabled = cursor.getInt(cursor.getColumnIndexOrThrow(COL_REMINDER_ENABLED)) == 1,
                        customImagePath = if (imgIdx >= 0) cursor.getString(imgIdx) ?: "" else ""
                    )
                )
            }
        }
        return list
    }

    fun getCardById(cardId: Long): CreditCard? {
        readableDatabase.query(
            TABLE_CARDS,
            null,
            "$COL_CARD_ID = ?",
            arrayOf(cardId.toString()),
            null,
            null,
            null
        ).use { cursor ->
            if (cursor.moveToFirst()) {
                val imgIdx = cursor.getColumnIndex(COL_CUSTOM_IMAGE_PATH)
                return CreditCard(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow(COL_CARD_ID)),
                    name = cursor.getString(cursor.getColumnIndexOrThrow(COL_CARD_NAME)),
                    bankName = cursor.getString(cursor.getColumnIndexOrThrow(COL_BANK_NAME)),
                    lastFourDigits = cursor.getString(cursor.getColumnIndexOrThrow(COL_LAST_FOUR)),
                    creditLimit = cursor.getLong(cursor.getColumnIndexOrThrow(COL_CREDIT_LIMIT)),
                    billingDay = cursor.getInt(cursor.getColumnIndexOrThrow(COL_BILLING_DAY)),
                    dueDay = cursor.getInt(cursor.getColumnIndexOrThrow(COL_DUE_DAY)),
                    colorIndex = cursor.getInt(cursor.getColumnIndexOrThrow(COL_COLOR_INDEX)),
                    reminderEnabled = cursor.getInt(cursor.getColumnIndexOrThrow(COL_REMINDER_ENABLED)) == 1,
                    customImagePath = if (imgIdx >= 0) cursor.getString(imgIdx) ?: "" else ""
                )
            }
        }
        return null
    }

    fun insertCard(card: CreditCard): Long {
        val values = ContentValues().apply {
            put(COL_CARD_NAME, card.name.trim())
            put(COL_BANK_NAME, card.bankName.trim())
            put(COL_LAST_FOUR, card.lastFourDigits.trim())
            put(COL_CREDIT_LIMIT, card.creditLimit)
            put(COL_BILLING_DAY, card.billingDay.coerceIn(1, 31))
            put(COL_DUE_DAY, card.dueDay.coerceIn(1, 31))
            put(COL_COLOR_INDEX, card.colorIndex)
            put(COL_REMINDER_ENABLED, if (card.reminderEnabled) 1 else 0)
            put(COL_CUSTOM_IMAGE_PATH, card.customImagePath.trim())
        }
        return writableDatabase.insert(TABLE_CARDS, null, values)
    }

    fun updateCard(card: CreditCard): Int {
        val values = ContentValues().apply {
            put(COL_CARD_NAME, card.name.trim())
            put(COL_BANK_NAME, card.bankName.trim())
            put(COL_LAST_FOUR, card.lastFourDigits.trim())
            put(COL_CREDIT_LIMIT, card.creditLimit)
            put(COL_BILLING_DAY, card.billingDay.coerceIn(1, 31))
            put(COL_DUE_DAY, card.dueDay.coerceIn(1, 31))
            put(COL_COLOR_INDEX, card.colorIndex)
            put(COL_REMINDER_ENABLED, if (card.reminderEnabled) 1 else 0)
            put(COL_CUSTOM_IMAGE_PATH, card.customImagePath.trim())
        }
        return writableDatabase.update(
            TABLE_CARDS,
            values,
            "$COL_CARD_ID = ?",
            arrayOf(card.id.toString())
        )
    }

    fun deleteCard(cardId: Long): Int {
        return writableDatabase.delete(
            TABLE_CARDS,
            "$COL_CARD_ID = ?",
            arrayOf(cardId.toString())
        )
    }

    fun getAllRecords(): List<CardRecord> {
        val list = mutableListOf<CardRecord>()
        readableDatabase.query(
            TABLE_RECORDS,
            null,
            null,
            null,
            null,
            null,
            "$COL_REC_DATE DESC, $COL_REC_ID DESC"
        ).use { cursor ->
            while (cursor.moveToNext()) {
                list.add(
                    CardRecord(
                        id = cursor.getLong(cursor.getColumnIndexOrThrow(COL_REC_ID)),
                        cardId = cursor.getLong(cursor.getColumnIndexOrThrow(COL_REC_CARD_ID)),
                        type = RecordType.fromDb(cursor.getString(cursor.getColumnIndexOrThrow(COL_REC_TYPE))),
                        title = cursor.getString(cursor.getColumnIndexOrThrow(COL_REC_TITLE)),
                        amount = cursor.getLong(cursor.getColumnIndexOrThrow(COL_REC_AMOUNT)),
                        dateEpochDay = cursor.getLong(cursor.getColumnIndexOrThrow(COL_REC_DATE)),
                        category = cursor.getString(cursor.getColumnIndexOrThrow(COL_REC_CATEGORY)),
                        note = cursor.getString(cursor.getColumnIndexOrThrow(COL_REC_NOTE))
                    )
                )
            }
        }
        return list
    }

    fun getRecordsByCard(cardId: Long): List<CardRecord> {
        val list = mutableListOf<CardRecord>()
        readableDatabase.query(
            TABLE_RECORDS,
            null,
            "$COL_REC_CARD_ID = ?",
            arrayOf(cardId.toString()),
            null,
            null,
            "$COL_REC_DATE DESC, $COL_REC_ID DESC"
        ).use { cursor ->
            while (cursor.moveToNext()) {
                list.add(
                    CardRecord(
                        id = cursor.getLong(cursor.getColumnIndexOrThrow(COL_REC_ID)),
                        cardId = cursor.getLong(cursor.getColumnIndexOrThrow(COL_REC_CARD_ID)),
                        type = RecordType.fromDb(cursor.getString(cursor.getColumnIndexOrThrow(COL_REC_TYPE))),
                        title = cursor.getString(cursor.getColumnIndexOrThrow(COL_REC_TITLE)),
                        amount = cursor.getLong(cursor.getColumnIndexOrThrow(COL_REC_AMOUNT)),
                        dateEpochDay = cursor.getLong(cursor.getColumnIndexOrThrow(COL_REC_DATE)),
                        category = cursor.getString(cursor.getColumnIndexOrThrow(COL_REC_CATEGORY)),
                        note = cursor.getString(cursor.getColumnIndexOrThrow(COL_REC_NOTE))
                    )
                )
            }
        }
        return list
    }

    fun getCardOutstandingBalance(cardId: Long): Long {
        val records = getRecordsByCard(cardId)
        val totalTx = records.filter { it.type == RecordType.TRANSACTION }.sumOf { it.amount }
        val totalPay = records.filter { it.type == RecordType.PAYMENT }.sumOf { it.amount }
        return (totalTx - totalPay).coerceAtLeast(0L)
    }

    fun insertRecord(record: CardRecord): Long {
        val values = ContentValues().apply {
            put(COL_REC_CARD_ID, record.cardId)
            put(COL_REC_TYPE, record.type.dbValue)
            put(COL_REC_TITLE, record.title.trim())
            put(COL_REC_AMOUNT, record.amount)
            put(COL_REC_DATE, record.dateEpochDay)
            put(COL_REC_CATEGORY, record.category.trim())
            put(COL_REC_NOTE, record.note.trim())
        }
        return writableDatabase.insert(TABLE_RECORDS, null, values)
    }

    fun updateRecord(record: CardRecord): Int {
        val values = ContentValues().apply {
            put(COL_REC_CARD_ID, record.cardId)
            put(COL_REC_TYPE, record.type.dbValue)
            put(COL_REC_TITLE, record.title.trim())
            put(COL_REC_AMOUNT, record.amount)
            put(COL_REC_DATE, record.dateEpochDay)
            put(COL_REC_CATEGORY, record.category.trim())
            put(COL_REC_NOTE, record.note.trim())
        }
        return writableDatabase.update(
            TABLE_RECORDS,
            values,
            "$COL_REC_ID = ?",
            arrayOf(record.id.toString())
        )
    }

    fun deleteRecord(recordId: Long): Int {
        return writableDatabase.delete(
            TABLE_RECORDS,
            "$COL_REC_ID = ?",
            arrayOf(recordId.toString())
        )
    }
}
