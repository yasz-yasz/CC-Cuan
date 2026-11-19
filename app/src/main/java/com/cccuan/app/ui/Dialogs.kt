package com.cccuan.app.ui

import android.app.DatePickerDialog
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cccuan.app.data.CARD_COLOR_PRESETS
import com.cccuan.app.data.CardRecord
import com.cccuan.app.data.CardSummary
import com.cccuan.app.data.CreditCard
import com.cccuan.app.data.RecordType
import com.cccuan.app.data.TRANSACTION_CATEGORIES
import com.cccuan.app.ui.theme.BrandEmerald
import com.cccuan.app.ui.theme.BrandNavy
import com.cccuan.app.ui.theme.BrandRed
import com.cccuan.app.ui.theme.TextMuted
import com.cccuan.app.util.CardImageStorage
import com.cccuan.app.util.Formatters
import java.time.LocalDate

@Composable
fun CardFormDialog(
    initialCard: CreditCard?,
    onDismiss: () -> Unit,
    onSave: (CreditCard) -> Unit
) {
    val context = LocalContext.current
    val originalImagePath = initialCard?.customImagePath ?: ""

    var name by remember { mutableStateOf(initialCard?.name ?: "") }
    var bankName by remember { mutableStateOf(initialCard?.bankName ?: "") }
    var lastFour by remember { mutableStateOf(initialCard?.lastFourDigits ?: "") }
    var limitText by remember { mutableStateOf(initialCard?.creditLimit?.toString() ?: "") }
    var billingDayText by remember { mutableStateOf(initialCard?.billingDay?.toString() ?: "15") }
    var dueDayText by remember { mutableStateOf(initialCard?.dueDay?.toString() ?: "5") }
    var colorIndex by remember { mutableIntStateOf(initialCard?.colorIndex ?: 0) }
    var reminderEnabled by remember { mutableStateOf(initialCard?.reminderEnabled ?: true) }
    var customImagePath by remember { mutableStateOf(originalImagePath) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val previewBitmap = remember(customImagePath) {
        CardImageStorage.loadCardBitmap(customImagePath)?.asImageBitmap()
    }
    val selectedGradient = CARD_COLOR_PRESETS[colorIndex.coerceIn(0, CARD_COLOR_PRESETS.lastIndex)]

    fun cleanupUnsavedTempImage() {
        if (customImagePath.isNotBlank() && customImagePath != originalImagePath) {
            CardImageStorage.deleteImageFile(customImagePath)
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            val savedPath = CardImageStorage.saveCardImageFromUri(context, uri)
            if (savedPath != null) {
                cleanupUnsavedTempImage()
                customImagePath = savedPath
                errorMsg = null
            } else {
                errorMsg = "Gagal memuat gambar. Pastikan file berupa gambar valid."
            }
        }
    }

    AlertDialog(
        onDismissRequest = {
            cleanupUnsavedTempImage()
            onDismiss()
        },
        title = {
            Text(
                text = if (initialCard == null) "Tambah Kartu Kredit" else "Edit Kartu Kredit",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Preview Tampilan Kartu (Custom Image atau Warna)
                Text(
                    text = "Tampilan Kartu",
                    fontSize = 12.5.sp,
                    color = TextMuted,
                    fontWeight = FontWeight.SemiBold
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(115.dp)
                        .clip(RoundedCornerShape(14.dp))
                ) {
                    if (previewBitmap != null) {
                        Image(
                            bitmap = previewBitmap,
                            contentDescription = "Preview gambar kartu",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Black.copy(alpha = 0.35f),
                                            Color.Black.copy(alpha = 0.68f)
                                        )
                                    )
                                )
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            Color(selectedGradient.first),
                                            Color(selectedGradient.second)
                                        )
                                    )
                                )
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = bankName.ifBlank { "BANK PENERBIT" }.uppercase(),
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = name.ifBlank { "Nama Kartu Kredit" },
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = if (lastFour.isNotBlank()) "•••• •••• •••• $lastFour" else "•••• •••• •••• 0000",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 12.sp
                        )
                    }
                }

                // Tombol Pilih / Hapus Gambar Custom
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { imagePickerLauncher.launch("image/*") },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            Icons.Filled.AddPhotoAlternate,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (customImagePath.isBlank()) "Pilih Gambar" else "Ganti Gambar",
                            fontSize = 12.5.sp
                        )
                    }
                    if (customImagePath.isNotBlank()) {
                        OutlinedButton(
                            onClick = {
                                cleanupUnsavedTempImage()
                                customImagePath = ""
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = BrandRed),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                Icons.Filled.Delete,
                                contentDescription = "Hapus gambar",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Hapus", fontSize = 12.5.sp)
                        }
                    }
                }

                // Warna Kartu (digunakan saat tidak memakai gambar custom)
                Text(
                    text = if (customImagePath.isBlank()) "Warna Kartu" else "Warna Cadangan (jika gambar dihapus)",
                    fontSize = 12.sp,
                    color = TextMuted,
                    fontWeight = FontWeight.SemiBold
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    CARD_COLOR_PRESETS.forEachIndexed { idx, pair ->
                        val selected = idx == colorIndex
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(Color(pair.first))
                                .border(
                                    width = if (selected) 2.5.dp else 0.dp,
                                    color = if (selected) BrandEmerald else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { colorIndex = idx },
                            contentAlignment = Alignment.Center
                        ) {
                            if (selected) {
                                Icon(
                                    Icons.Filled.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; errorMsg = null },
                    label = { Text("Nama Kartu *") },
                    placeholder = { Text("Contoh: JCB Precious / Signature") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = bankName,
                    onValueChange = { bankName = it; errorMsg = null },
                    label = { Text("Bank Penerbit *") },
                    placeholder = { Text("Contoh: Danamon, Mandiri, BCA") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = lastFour,
                    onValueChange = { if (it.length <= 4) lastFour = it.filter { c -> c.isDigit() } },
                    label = { Text("4 Digit Terakhir (Opsional)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = limitText,
                    onValueChange = { limitText = it.filter { c -> c.isDigit() }; errorMsg = null },
                    label = { Text("Limit Kartu Kredit (Rp) *") },
                    placeholder = { Text("Contoh: 25000000") },
                    supportingText = {
                        Formatters.parseRupiahInput(limitText)?.let {
                            Text(Formatters.formatRupiah(it), color = BrandEmerald, fontWeight = FontWeight.SemiBold)
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = billingDayText,
                        onValueChange = { if (it.length <= 2) billingDayText = it.filter { c -> c.isDigit() } },
                        label = { Text("Tgl Cetak (1-31)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = dueDayText,
                        onValueChange = { if (it.length <= 2) dueDayText = it.filter { c -> c.isDigit() } },
                        label = { Text("Jatuh Tempo (1-31)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Alarm Pengingat H-7", fontWeight = FontWeight.SemiBold, fontSize = 13.5.sp)
                        Text("Notifikasi otomatis 7 hari sebelum tempo", fontSize = 11.5.sp, color = TextMuted)
                    }
                    Switch(checked = reminderEnabled, onCheckedChange = { reminderEnabled = it })
                }

                if (errorMsg != null) {
                    Text(errorMsg!!, color = BrandRed, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val limit = Formatters.parseRupiahInput(limitText)
                    val billing = billingDayText.toIntOrNull()
                    val due = dueDayText.toIntOrNull()
                    when {
                        name.isBlank() -> errorMsg = "Nama kartu wajib diisi."
                        bankName.isBlank() -> errorMsg = "Nama bank wajib diisi."
                        limit == null || limit <= 0L -> errorMsg = "Limit kartu harus lebih dari 0."
                        billing == null || billing !in 1..31 -> errorMsg = "Tanggal cetak harus antara 1 - 31."
                        due == null || due !in 1..31 -> errorMsg = "Tanggal jatuh tempo harus antara 1 - 31."
                        else -> {
                            onSave(
                                CreditCard(
                                    id = initialCard?.id ?: 0L,
                                    name = name.trim(),
                                    bankName = bankName.trim(),
                                    lastFourDigits = lastFour.trim(),
                                    creditLimit = limit,
                                    billingDay = billing,
                                    dueDay = due,
                                    colorIndex = colorIndex,
                                    reminderEnabled = reminderEnabled,
                                    customImagePath = customImagePath
                                )
                            )
                        }
                    }
                }
            ) {
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    cleanupUnsavedTempImage()
                    onDismiss()
                }
            ) {
                Text("Batal")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordFormDialog(
    type: RecordType,
    summaries: List<CardSummary>,
    defaultCardId: Long?,
    initialRecord: CardRecord?,
    onDismiss: () -> Unit,
    onSave: (CardRecord) -> Unit
) {
    val context = LocalContext.current
    val isPayment = type == RecordType.PAYMENT

    var selectedCardId by remember {
        mutableLongStateOf(
            initialRecord?.cardId
                ?: defaultCardId
                ?: summaries.firstOrNull()?.card?.id
                ?: 0L
        )
    }
    var cardDropdownExpanded by remember { mutableStateOf(false) }

    var title by remember {
        mutableStateOf(
            initialRecord?.title ?: if (isPayment) "Pembayaran Tagihan" else ""
        )
    }
    var amountText by remember { mutableStateOf(initialRecord?.amount?.toString() ?: "") }
    var selectedDate by remember {
        mutableStateOf(
            initialRecord?.let { LocalDate.ofEpochDay(it.dateEpochDay) } ?: LocalDate.now()
        )
    }
    var category by remember {
        mutableStateOf(
            initialRecord?.category?.ifBlank { TRANSACTION_CATEGORIES.first() }
                ?: if (isPayment) "Pelunasan" else TRANSACTION_CATEGORIES.first()
        )
    }
    var catDropdownExpanded by remember { mutableStateOf(false) }
    var note by remember { mutableStateOf(initialRecord?.note ?: "") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val selectedSummary = summaries.find { it.card.id == selectedCardId }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = when {
                    initialRecord != null && isPayment -> "Edit Pembayaran"
                    initialRecord != null -> "Edit Transaksi"
                    isPayment -> "Catat Pembayaran / Pelunasan"
                    else -> "Catat Transaksi Kartu"
                },
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ExposedDropdownMenuBox(
                    expanded = cardDropdownExpanded,
                    onExpandedChange = { cardDropdownExpanded = !cardDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedSummary?.card?.name ?: "Pilih Kartu",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Kartu Kredit") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = cardDropdownExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = cardDropdownExpanded,
                        onDismissRequest = { cardDropdownExpanded = false }
                    ) {
                        summaries.forEach { summary ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(summary.card.name, fontWeight = FontWeight.SemiBold)
                                        Text(
                                            "Tagihan: ${Formatters.formatRupiah(summary.currentBalance)}",
                                            fontSize = 11.5.sp,
                                            color = TextMuted
                                        )
                                    }
                                },
                                onClick = {
                                    selectedCardId = summary.card.id
                                    cardDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                if (isPayment && selectedSummary != null && selectedSummary.currentBalance > 0L) {
                    OutlinedButton(
                        onClick = {
                            amountText = selectedSummary.currentBalance.toString()
                            title = "Pelunasan Penuh Tagihan"
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            "Isi Lunas Penuh (${Formatters.formatRupiah(selectedSummary.currentBalance)})",
                            fontSize = 12.5.sp,
                            color = BrandEmerald,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it; errorMsg = null },
                    label = { Text(if (isPayment) "Keterangan Pembayaran *" else "Nama Transaksi *") },
                    placeholder = {
                        Text(if (isPayment) "Contoh: Transfer Pelunasan" else "Contoh: Belanja Supermarket / Tiket")
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { c -> c.isDigit() }; errorMsg = null },
                    label = { Text(if (isPayment) "Nominal Pembayaran (Rp) *" else "Jumlah Transaksi (Rp) *") },
                    supportingText = {
                        Formatters.parseRupiahInput(amountText)?.let {
                            Text(Formatters.formatRupiah(it), color = BrandNavy, fontWeight = FontWeight.SemiBold)
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedButton(
                    onClick = {
                        DatePickerDialog(
                            context,
                            { _, y, m, d ->
                                selectedDate = LocalDate.of(y, m + 1, d)
                            },
                            selectedDate.year,
                            selectedDate.monthValue - 1,
                            selectedDate.dayOfMonth
                        ).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.CalendarMonth, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Tanggal: ${Formatters.formatDateFull(selectedDate)}")
                }

                if (!isPayment) {
                    ExposedDropdownMenuBox(
                        expanded = catDropdownExpanded,
                        onExpandedChange = { catDropdownExpanded = !catDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = category,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Kategori") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = catDropdownExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = catDropdownExpanded,
                            onDismissRequest = { catDropdownExpanded = false }
                        ) {
                            TRANSACTION_CATEGORIES.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat) },
                                    onClick = {
                                        category = cat
                                        catDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Catatan (Opsional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMsg != null) {
                    Text(errorMsg!!, color = BrandRed, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = Formatters.parseRupiahInput(amountText)
                    when {
                        selectedCardId <= 0L -> errorMsg = "Pilih kartu kredit terlebih dahulu."
                        title.isBlank() -> errorMsg = "Nama/keterangan wajib diisi."
                        amount == null || amount <= 0L -> errorMsg = "Nominal harus lebih dari 0."
                        else -> {
                            onSave(
                                CardRecord(
                                    id = initialRecord?.id ?: 0L,
                                    cardId = selectedCardId,
                                    type = type,
                                    title = title.trim(),
                                    amount = amount,
                                    dateEpochDay = selectedDate.toEpochDay(),
                                    category = if (isPayment) "Pelunasan" else category,
                                    note = note.trim()
                                )
                            )
                        }
                    }
                }
            ) {
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

@Composable
fun ConfirmDeleteDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = { Text(message) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = BrandRed)
            ) {
                Text("Hapus")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}
