package com.cccuan.app.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cccuan.app.data.CardRecord
import com.cccuan.app.data.RecordType
import com.cccuan.app.ui.theme.BrandEmerald
import com.cccuan.app.ui.theme.BrandNavy
import com.cccuan.app.ui.theme.BrandRed
import com.cccuan.app.ui.theme.TextMuted
import com.cccuan.app.util.Formatters

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordsTab(
    state: CcUiState,
    onSelectCardFilter: (Long?) -> Unit,
    onSelectRecordFilter: (RecordFilter) -> Unit,
    onAddTransaction: () -> Unit,
    onAddPayment: () -> Unit,
    onEditRecord: (CardRecord) -> Unit,
    onDeleteRecord: (CardRecord) -> Unit,
    onExportPdf: () -> Unit
) {
    val cardNameMap = state.cards.associate { it.id to it.name }
    val filtered = state.filteredRecords
    val sumTx = filtered.filter { it.type == RecordType.TRANSACTION }.sumOf { it.amount }
    val sumPay = filtered.filter { it.type == RecordType.PAYMENT }.sumOf { it.amount }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Action Bar: Add Transaction, Pay Bill, Export PDF
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onAddTransaction,
                    enabled = state.cards.isNotEmpty(),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp)
                ) {
                    Icon(Icons.Filled.ShoppingCart, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Transaksi", fontSize = 12.5.sp)
                }
                Button(
                    onClick = onAddPayment,
                    enabled = state.cards.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandEmerald),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp)
                ) {
                    Icon(Icons.Filled.Payment, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Bayar", fontSize = 12.5.sp)
                }
                OutlinedButton(
                    onClick = onExportPdf,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp)
                ) {
                    Icon(Icons.Filled.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("PDF", fontSize = 12.5.sp)
                }
            }
        }

        // Card Filter Chips
        if (state.cards.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = state.selectedCardFilterId == null,
                        onClick = { onSelectCardFilter(null) },
                        label = { Text("Semua Kartu") }
                    )
                    state.cards.forEach { card ->
                        FilterChip(
                            selected = state.selectedCardFilterId == card.id,
                            onClick = { onSelectCardFilter(card.id) },
                            label = { Text(card.name) }
                        )
                    }
                }
            }
        }

        // Record Type Filter Chips
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = state.recordFilter == RecordFilter.ALL,
                    onClick = { onSelectRecordFilter(RecordFilter.ALL) },
                    label = { Text("Semua") }
                )
                FilterChip(
                    selected = state.recordFilter == RecordFilter.TRANSACTION,
                    onClick = { onSelectRecordFilter(RecordFilter.TRANSACTION) },
                    label = { Text("Transaksi") }
                )
                FilterChip(
                    selected = state.recordFilter == RecordFilter.PAYMENT,
                    onClick = { onSelectRecordFilter(RecordFilter.PAYMENT) },
                    label = { Text("Pembayaran") }
                )
            }
        }

        // Filtered Totals Strip
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Total Transaksi", fontSize = 11.5.sp, color = TextMuted)
                        Text(
                            Formatters.formatRupiah(sumTx),
                            fontWeight = FontWeight.Bold,
                            color = BrandRed,
                            fontSize = 14.5.sp
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Total Pembayaran", fontSize = 11.5.sp, color = TextMuted)
                        Text(
                            Formatters.formatRupiah(sumPay),
                            fontWeight = FontWeight.Bold,
                            color = BrandEmerald,
                            fontSize = 14.5.sp
                        )
                    }
                }
            }
        }

        if (filtered.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ReceiptLong,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(46.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (state.cards.isEmpty()) "Tambahkan kartu kredit terlebih dahulu" else "Belum ada riwayat tercatat",
                            fontWeight = FontWeight.SemiBold,
                            color = BrandNavy
                        )
                        Text(
                            text = "Catat transaksi belanja atau pembayaran tagihan di tombol atas.",
                            fontSize = 12.5.sp,
                            color = TextMuted
                        )
                    }
                }
            }
        } else {
            items(filtered, key = { it.id }) { record ->
                RecordRowCard(
                    record = record,
                    cardName = cardNameMap[record.cardId] ?: "Kartu",
                    onEdit = { onEditRecord(record) },
                    onDelete = { onDeleteRecord(record) }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(72.dp)) }
    }
}

@Composable
private fun RecordRowCard(
    record: CardRecord,
    cardName: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isPayment = record.type == RecordType.PAYMENT
    val badgeBg = if (isPayment) Color(0xFFD1FAE5) else Color(0xFFFEE2E2)
    val badgeColor = if (isPayment) Color(0xFF065F46) else BrandRed
    val amountPrefix = if (isPayment) "-" else "+"

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        color = badgeBg,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = if (isPayment) "Pelunasan / Bayar" else (record.category.ifBlank { "Transaksi" }),
                            color = badgeColor,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Text(
                        text = cardName,
                        fontSize = 11.5.sp,
                        color = TextMuted,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = record.title,
                    fontWeight = FontWeight.Bold,
                    color = BrandNavy,
                    fontSize = 15.sp
                )
                Text(
                    text = Formatters.formatDateShort(record.dateEpochDay) +
                        if (record.note.isNotBlank()) " • ${record.note}" else "",
                    fontSize = 12.sp,
                    color = TextMuted
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$amountPrefix${Formatters.formatRupiah(record.amount)}",
                    fontWeight = FontWeight.Bold,
                    color = if (isPayment) BrandEmerald else BrandRed,
                    fontSize = 14.5.sp
                )
                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(30.dp)) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit", tint = TextMuted, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(30.dp)) {
                        Icon(Icons.Filled.Delete, contentDescription = "Hapus", tint = BrandRed, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}
