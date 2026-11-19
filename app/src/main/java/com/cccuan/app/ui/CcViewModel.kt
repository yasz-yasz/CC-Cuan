package com.cccuan.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.cccuan.app.alarm.AlarmScheduler
import com.cccuan.app.data.CardRecord
import com.cccuan.app.data.CardSummary
import com.cccuan.app.data.CcRepository
import com.cccuan.app.data.CreditCard
import com.cccuan.app.data.RecordType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class RecordFilter {
    ALL, TRANSACTION, PAYMENT
}

data class CcUiState(
    val cards: List<CreditCard> = emptyList(),
    val summaries: List<CardSummary> = emptyList(),
    val records: List<CardRecord> = emptyList(),
    val selectedCardFilterId: Long? = null,
    val recordFilter: RecordFilter = RecordFilter.ALL
) {
    val totalLimit: Long get() = summaries.sumOf { it.card.creditLimit }
    val totalBalance: Long get() = summaries.sumOf { it.currentBalance }
    val totalAvailable: Long get() = summaries.sumOf { it.availableLimit }

    val filteredRecords: List<CardRecord>
        get() = records.filter { rec ->
            val cardOk = selectedCardFilterId == null || rec.cardId == selectedCardFilterId
            val typeOk = when (recordFilter) {
                RecordFilter.ALL -> true
                RecordFilter.TRANSACTION -> rec.type == RecordType.TRANSACTION
                RecordFilter.PAYMENT -> rec.type == RecordType.PAYMENT
            }
            cardOk && typeOk
        }

    val filteredSummaries: List<CardSummary>
        get() = if (selectedCardFilterId == null) {
            summaries
        } else {
            summaries.filter { it.card.id == selectedCardFilterId }
        }
}

class CcViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = CcRepository(application)
    private val _uiState = MutableStateFlow(CcUiState())
    val uiState: StateFlow<CcUiState> = _uiState.asStateFlow()

    init {
        refreshData()
    }

    fun refreshData() {
        val cards = repository.getAllCards()
        val records = repository.getAllRecords()
        val summaries = repository.buildCardSummaries(cards, records)
        _uiState.update { current ->
            val validSelectedCard = current.selectedCardFilterId?.takeIf { id ->
                cards.any { it.id == id }
            }
            current.copy(
                cards = cards,
                records = records,
                summaries = summaries,
                selectedCardFilterId = validSelectedCard
            )
        }
    }

    fun saveCard(card: CreditCard) {
        val savedCard = if (card.id == 0L) {
            val newId = repository.insertCard(card)
            card.copy(id = newId)
        } else {
            repository.updateCard(card)
            card
        }
        AlarmScheduler.scheduleDueReminder(getApplication(), savedCard)
        refreshData()
    }

    fun deleteCard(cardId: Long) {
        AlarmScheduler.cancelDueReminder(getApplication(), cardId)
        repository.deleteCard(cardId)
        refreshData()
    }

    fun toggleCardReminder(card: CreditCard, enabled: Boolean) {
        val updated = card.copy(reminderEnabled = enabled)
        repository.updateCard(updated)
        AlarmScheduler.scheduleDueReminder(getApplication(), updated)
        refreshData()
    }

    fun saveRecord(record: CardRecord) {
        if (record.id == 0L) {
            repository.insertRecord(record)
        } else {
            repository.updateRecord(record)
        }
        refreshData()
    }

    fun deleteRecord(recordId: Long) {
        repository.deleteRecord(recordId)
        refreshData()
    }

    fun setCardFilter(cardId: Long?) {
        _uiState.update { it.copy(selectedCardFilterId = cardId) }
    }

    fun setRecordFilter(filter: RecordFilter) {
        _uiState.update { it.copy(recordFilter = filter) }
    }

    fun triggerTestAlarm(cardId: Long) {
        AlarmScheduler.triggerTestAlarmNow(getApplication(), cardId)
    }

    fun rescheduleAllAlarms() {
        AlarmScheduler.rescheduleAll(getApplication(), _uiState.value.cards)
    }
}
