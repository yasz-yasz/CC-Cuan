package com.cccuan.app.ui

import android.content.Intent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import com.cccuan.app.data.CardRecord
import com.cccuan.app.data.CreditCard
import com.cccuan.app.data.RecordType
import com.cccuan.app.ui.theme.BrandEmerald
import com.cccuan.app.ui.theme.BrandNavy
import com.cccuan.app.util.Formatters
import com.cccuan.app.util.PdfExporter
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: CcViewModel,
    onRequestNotificationPermission: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var selectedTab by remember { mutableIntStateOf(0) }

    var showCardDialog by remember { mutableStateOf(false) }
    var editingCard by remember { mutableStateOf<CreditCard?>(null) }
    var deletingCard by remember { mutableStateOf<CreditCard?>(null) }

    var showRecordDialogType by remember { mutableStateOf<RecordType?>(null) }
    var defaultRecordCardId by remember { mutableStateOf<Long?>(null) }
    var editingRecord by remember { mutableStateOf<CardRecord?>(null) }
    var deletingRecord by remember { mutableStateOf<CardRecord?>(null) }

    fun handleExportPdf() {
        try {
            val filterName = state.selectedCardFilterId?.let { id ->
                state.cards.find { it.id == id }?.name
            }
            val result = PdfExporter.exportReport(
                context = context,
                summaries = state.filteredSummaries,
                records = state.filteredRecords,
                filterCardName = filterName
            )
            val savedText = if (result.savedToDownloads) {
                "PDF tersimpan di Downloads/CC_Cuan/${result.fileName}"
            } else {
                "PDF siap dibuka: ${result.fileName}"
            }

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, result.shareUri)
                putExtra(Intent.EXTRA_SUBJECT, "Laporan Kartu Kredit - CC Cuan")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            scope.launch {
                val action = snackbarHostState.showSnackbar(
                    message = savedText,
                    actionLabel = "Bagikan / Buka",
                    duration = SnackbarDuration.Long
                )
                if (action == SnackbarResult.ActionPerformed) {
                    context.startActivity(
                        Intent.createChooser(shareIntent, "Buka atau Bagikan Laporan PDF")
                    )
                }
            }
        } catch (e: Exception) {
            scope.launch {
                snackbarHostState.showSnackbar(
                    "Gagal ekspor PDF: ${e.message ?: "Error tidak diketahui"}"
                )
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "CC Cuan",
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BrandNavy,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                ),
                actions = {
                    IconButton(onClick = { handleExportPdf() }) {
                        Icon(
                            imageVector = Icons.Filled.PictureAsPdf,
                            contentDescription = "Ekspor PDF"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            when (selectedTab) {
                0 -> FloatingActionButton(
                    onClick = {
                        editingCard = null
                        showCardDialog = true
                    },
                    containerColor = BrandEmerald,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Tambah Kartu")
                }

                1 -> if (state.cards.isNotEmpty()) {
                    FloatingActionButton(
                        onClick = {
                            editingRecord = null
                            defaultRecordCardId = state.selectedCardFilterId
                            showRecordDialogType = RecordType.TRANSACTION
                        },
                        containerColor = BrandNavy,
                        contentColor = Color.White
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "Catat Transaksi")
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Filled.CreditCard, contentDescription = null) },
                    label = { Text("Kartu") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Filled.ReceiptLong, contentDescription = null) },
                    label = { Text("Transaksi") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Filled.NotificationsActive, contentDescription = null) },
                    label = { Text("Alarm & PDF") }
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedTab) {
                0 -> CardsTab(
                    state = state,
                    onAddCard = {
                        editingCard = null
                        showCardDialog = true
                    },
                    onEditCard = { card ->
                        editingCard = card
                        showCardDialog = true
                    },
                    onDeleteCard = { card ->
                        deletingCard = card
                    },
                    onAddTransactionForCard = { card ->
                        editingRecord = null
                        defaultRecordCardId = card.id
                        showRecordDialogType = RecordType.TRANSACTION
                    },
                    onAddPaymentForCard = { summary ->
                        editingRecord = null
                        defaultRecordCardId = summary.card.id
                        showRecordDialogType = RecordType.PAYMENT
                    }
                )

                1 -> RecordsTab(
                    state = state,
                    onSelectCardFilter = { viewModel.setCardFilter(it) },
                    onSelectRecordFilter = { viewModel.setRecordFilter(it) },
                    onAddTransaction = {
                        editingRecord = null
                        defaultRecordCardId = state.selectedCardFilterId
                        showRecordDialogType = RecordType.TRANSACTION
                    },
                    onAddPayment = {
                        editingRecord = null
                        defaultRecordCardId = state.selectedCardFilterId
                        showRecordDialogType = RecordType.PAYMENT
                    },
                    onEditRecord = { rec ->
                        editingRecord = rec
                        defaultRecordCardId = rec.cardId
                        showRecordDialogType = rec.type
                    },
                    onDeleteRecord = { rec ->
                        deletingRecord = rec
                    },
                    onExportPdf = { handleExportPdf() }
                )

                2 -> RemindersTab(
                    state = state,
                    onToggleReminder = { card, enabled ->
                        viewModel.toggleCardReminder(card, enabled)
                        scope.launch {
                            val status = if (enabled) "diaktifkan (H-7)" else "dinonaktifkan"
                            snackbarHostState.showSnackbar("Alarm ${card.name} $status")
                        }
                    },
                    onTestAlarm = { cardId ->
                        onRequestNotificationPermission()
                        viewModel.triggerTestAlarm(cardId)
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                "Alarm uji coba dikirim! Cek notifikasi HP kamu."
                            )
                        }
                    },
                    onExportPdf = { handleExportPdf() },
                    onRequestNotificationPermission = onRequestNotificationPermission
                )
            }
        }
    }

    if (showCardDialog) {
        CardFormDialog(
            initialCard = editingCard,
            onDismiss = { showCardDialog = false },
            onSave = { card ->
                viewModel.saveCard(card)
                showCardDialog = false
                scope.launch {
                    snackbarHostState.showSnackbar(
                        "Kartu ${card.name} disimpan & alarm H-7 dijadwalkan."
                    )
                }
            }
        )
    }

    val activeRecordType = showRecordDialogType
    if (activeRecordType != null && state.summaries.isNotEmpty()) {
        RecordFormDialog(
            type = activeRecordType,
            summaries = state.summaries,
            defaultCardId = defaultRecordCardId,
            initialRecord = editingRecord,
            onDismiss = {
                showRecordDialogType = null
                editingRecord = null
            },
            onSave = { record ->
                viewModel.saveRecord(record)
                showRecordDialogType = null
                editingRecord = null
                scope.launch {
                    val label = if (record.type == RecordType.PAYMENT) {
                        "Pembayaran"
                    } else {
                        "Transaksi"
                    }
                    snackbarHostState.showSnackbar("$label berhasil dicatat.")
                }
            }
        )
    }

    val cardToDelete = deletingCard
    if (cardToDelete != null) {
        ConfirmDeleteDialog(
            title = "Hapus Kartu Kredit?",
            message = "Kartu \"${cardToDelete.name}\" beserta seluruh riwayat transaksi dan pembayarannya akan dihapus permanen.",
            onDismiss = { deletingCard = null },
            onConfirm = {
                viewModel.deleteCard(cardToDelete.id)
                deletingCard = null
                scope.launch {
                    snackbarHostState.showSnackbar("Kartu ${cardToDelete.name} dihapus.")
                }
            }
        )
    }

    val recordToDelete = deletingRecord
    if (recordToDelete != null) {
        ConfirmDeleteDialog(
            title = "Hapus Riwayat?",
            message = "Hapus catatan \"${recordToDelete.title}\" sebesar ${Formatters.formatRupiah(recordToDelete.amount)}?",
            onDismiss = { deletingRecord = null },
            onConfirm = {
                viewModel.deleteRecord(recordToDelete.id)
                deletingRecord = null
                scope.launch {
                    snackbarHostState.showSnackbar("Catatan dihapus.")
                }
            }
        )
    }
}
