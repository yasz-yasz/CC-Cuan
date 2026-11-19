package com.cccuan.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cccuan.app.data.CARD_COLOR_PRESETS
import com.cccuan.app.data.CardSummary
import com.cccuan.app.data.CreditCard
import com.cccuan.app.ui.theme.BrandAmber
import com.cccuan.app.ui.theme.BrandEmerald
import com.cccuan.app.ui.theme.BrandNavy
import com.cccuan.app.ui.theme.BrandRed
import com.cccuan.app.ui.theme.TextMuted
import com.cccuan.app.util.DateCycleUtils
import com.cccuan.app.util.Formatters

@Composable
fun CardsTab(
    state: CcUiState,
    onAddCard: () -> Unit,
    onEditCard: (CreditCard) -> Unit,
    onDeleteCard: (CreditCard) -> Unit,
    onAddTransactionForCard: (CreditCard) -> Unit,
    onAddPaymentForCard: (CardSummary) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Overview Portfolio Card
        item {
            PortfolioSummaryCard(state = state)
        }

        // Upcoming H-7 Alert Banner if any card is within 7 days of due date
        val urgentCards = state.summaries.filter {
            DateCycleUtils.daysUntilDue(it.card.dueDay) <= 7L
        }
        if (urgentCards.isNotEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Warning,
                            contentDescription = null,
                            tint = BrandAmber,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Perhatian Jatuh Tempo (≤ 7 Hari)",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF92400E),
                                fontSize = 14.sp
                            )
                            urgentCards.forEach { summary ->
                                val d = DateCycleUtils.daysUntilDue(summary.card.dueDay)
                                val label = if (d == 0L) "HARI INI" else "H-$d"
                                Text(
                                    text = "• ${summary.card.name}: $label (Tagihan ${Formatters.formatRupiah(summary.currentBalance)})",
                                    color = Color(0xFF78350F),
                                    fontSize = 12.5.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Daftar Kartu Kredit (${state.summaries.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = BrandNavy
                )
                FilledTonalButton(
                    onClick = onAddCard,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Kartu Baru", fontSize = 13.sp)
                }
            }
        }

        if (state.summaries.isEmpty()) {
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
                            imageVector = Icons.Filled.CreditCard,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(52.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Belum Ada Kartu Kredit",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = BrandNavy
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tambahkan kartu kredit pertamamu untuk memantau limit, transaksi, pelunasan, dan pengingat jatuh tempo H-7.",
                            color = TextMuted,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(onClick = onAddCard) {
                            Icon(Icons.Filled.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Tambah Kartu Kredit")
                        }
                    }
                }
            }
        } else {
            items(state.summaries, key = { it.card.id }) { summary ->
                CreditCardItem(
                    summary = summary,
                    onEdit = { onEditCard(summary.card) },
                    onDelete = { onDeleteCard(summary.card) },
                    onAddTransaction = { onAddTransactionForCard(summary.card) },
                    onAddPayment = { onAddPaymentForCard(summary) }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(72.dp)) }
    }
}

@Composable
private fun PortfolioSummaryCard(state: CcUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = BrandNavy)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "TOTAL TAGIHAN BERJALAN",
                color = Color(0xFF94A3B8),
                fontSize = 11.5.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.8.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = Formatters.formatRupiah(state.totalBalance),
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(14.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Total Limit",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.5.sp
                    )
                    Text(
                        text = Formatters.formatRupiah(state.totalLimit),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Sisa Limit",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.5.sp
                    )
                    Text(
                        text = Formatters.formatRupiah(state.totalAvailable),
                        color = BrandEmerald,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun CreditCardItem(
    summary: CardSummary,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onAddTransaction: () -> Unit,
    onAddPayment: () -> Unit
) {
    val card = summary.card
    val colors = CARD_COLOR_PRESETS[card.colorIndex.coerceIn(0, CARD_COLOR_PRESETS.lastIndex)]
    val nextDue = DateCycleUtils.getNextDueDate(card.dueDay)
    val daysLeft = DateCycleUtils.daysUntilDue(card.dueDay)
    val utilizationPercent = (summary.utilizationRatio * 100).toInt()
    val progressColor = when {
        summary.utilizationRatio >= 0.8f -> Color(0xFFF87171)
        summary.utilizationRatio >= 0.5f -> Color(0xFFFBBF24)
        else -> BrandEmerald
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Visual Credit Card Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(colors.first), Color(colors.second))
                        )
                    )
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = card.bankName.uppercase(),
                                color = Color.White.copy(alpha = 0.78f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = card.name,
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (card.lastFourDigits.isNotBlank()) {
                                Text(
                                    text = "•••• •••• •••• ${card.lastFourDigits}",
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontSize = 12.5.sp
                                )
                            }
                        }
                        Row {
                            IconButton(onClick = onEdit, modifier = Modifier.size(34.dp)) {
                                Icon(
                                    Icons.Filled.Edit,
                                    contentDescription = "Edit kartu",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            IconButton(onClick = onDelete, modifier = Modifier.size(34.dp)) {
                                Icon(
                                    Icons.Filled.Delete,
                                    contentDescription = "Hapus kartu",
                                    tint = Color.White.copy(alpha = 0.85f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Tagihan / Terpakai",
                                color = Color.White.copy(alpha = 0.75f),
                                fontSize = 11.sp
                            )
                            Text(
                                text = Formatters.formatRupiah(summary.currentBalance),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Sisa Limit",
                                color = Color.White.copy(alpha = 0.75f),
                                fontSize = 11.sp
                            )
                            Text(
                                text = Formatters.formatRupiah(summary.availableLimit),
                                color = Color(0xFFA7F3D0),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LinearProgressIndicator(
                        progress = { summary.utilizationRatio },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(7.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = progressColor,
                        trackColor = Color.White.copy(alpha = 0.22f)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Limit: ${Formatters.formatRupiah(card.creditLimit)}",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 11.5.sp
                        )
                        Text(
                            text = "Terpakai $utilizationPercent%",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Cycle & Actions Body
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Cetak Tagihan: Setiap tgl ${card.billingDay}",
                            fontSize = 12.5.sp,
                            color = TextMuted
                        )
                        Text(
                            text = "Jatuh Tempo: Tgl ${card.dueDay} (${Formatters.formatDateShort(nextDue)})",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (daysLeft <= 7L) BrandRed else BrandNavy
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = when {
                            daysLeft <= 3L -> Color(0xFFFEE2E2)
                            daysLeft <= 7L -> Color(0xFFFEF3C7)
                            else -> Color(0xFFD1FAE5)
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (card.reminderEnabled) {
                                Icon(
                                    imageVector = Icons.Filled.NotificationsActive,
                                    contentDescription = null,
                                    modifier = Modifier.size(13.dp),
                                    tint = if (daysLeft <= 7L) BrandAmber else BrandEmerald
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Text(
                                text = if (daysLeft == 0L) "Hari Ini!" else "H-$daysLeft",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    daysLeft <= 3L -> BrandRed
                                    daysLeft <= 7L -> Color(0xFF92400E)
                                    else -> Color(0xFF065F46)
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onAddTransaction,
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        Icon(Icons.Filled.ShoppingCart, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Transaksi", fontSize = 13.sp)
                    }
                    Button(
                        onClick = onAddPayment,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandEmerald),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        Icon(Icons.Filled.Payment, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Bayar Tagihan", fontSize = 13.sp)
                    }
                }
            }
        }
    }
}
