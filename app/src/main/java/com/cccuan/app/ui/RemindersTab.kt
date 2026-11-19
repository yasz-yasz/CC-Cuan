package com.cccuan.app.ui

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cccuan.app.data.CreditCard
import com.cccuan.app.ui.theme.BrandAmber
import com.cccuan.app.ui.theme.BrandEmerald
import com.cccuan.app.ui.theme.BrandNavy
import com.cccuan.app.ui.theme.TextMuted
import com.cccuan.app.util.DateCycleUtils
import com.cccuan.app.util.Formatters

@Composable
fun RemindersTab(
    state: CcUiState,
    onToggleReminder: (CreditCard, Boolean) -> Unit,
    onTestAlarm: (Long) -> Unit,
    onExportPdf: () -> Unit,
    onRequestNotificationPermission: () -> Unit
) {
    val context = LocalContext.current
    val canExactAlarm = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.canScheduleExactAlarms()
    } else {
        true
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Info Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = BrandNavy),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Alarm,
                            contentDescription = null,
                            tint = BrandEmerald,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Alarm Pengingat H-7 Jatuh Tempo",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "CC Cuan menjadwalkan alarm pengingat otomatis tepat 7 hari sebelum tanggal jatuh tempo setiap kartu (pukul 08:00) dan otomatis dijadwalkan ulang setelah HP restart.",
                        color = Color(0xFFCBD5E1),
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = onRequestNotificationPermission,
                            colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                        ) {
                            Text("Izin Notifikasi", fontSize = 12.sp)
                        }
                        if (!canExactAlarm && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            OutlinedButton(
                                onClick = {
                                    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                        data = Uri.parse("package:${context.packageName}")
                                    }
                                    context.startActivity(intent)
                                },
                                colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFBBF24))
                            ) {
                                Text("Aktifkan Alarm Akurat", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        // Export Full PDF Card
        item {
            Card(
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
                        Text(
                            text = "Ekspor Laporan PDF",
                            fontWeight = FontWeight.Bold,
                            color = BrandNavy,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Unduh ringkasan limit, transaksi & pelunasan dalam format A4 PDF.",
                            fontSize = 12.5.sp,
                            color = TextMuted
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(onClick = onExportPdf) {
                        Icon(Icons.Filled.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Ekspor")
                    }
                }
            }
        }

        item {
            Text(
                text = "Jadwal Alarm per Kartu",
                fontWeight = FontWeight.Bold,
                color = BrandNavy,
                fontSize = 15.sp
            )
        }

        if (state.summaries.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = "Tambahkan kartu kredit di tab Kartu untuk mengaktifkan jadwal alarm pengingat H-7.",
                        modifier = Modifier.padding(18.dp),
                        color = TextMuted,
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            items(state.summaries, key = { it.card.id }) { summary ->
                val card = summary.card
                val (nextAlarmDateTime, targetDueDate) = DateCycleUtils.getNextReminderDateTime(card.dueDay)
                val daysLeft = DateCycleUtils.daysUntilDue(card.dueDay)

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = card.name,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandNavy,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = "Jatuh tempo terdekat: ${Formatters.formatDateFull(DateCycleUtils.getNextDueDate(card.dueDay))} (H-$daysLeft)",
                                    fontSize = 12.5.sp,
                                    color = if (daysLeft <= 7L) BrandAmber else TextMuted
                                )
                                Text(
                                    text = "Alarm H-7 berikutnya: ${Formatters.formatDateShort(nextAlarmDateTime.toLocalDate())} pukul 08:00 (untuk tempo ${Formatters.formatDateShort(targetDueDate)})",
                                    fontSize = 12.sp,
                                    color = TextMuted
                                )
                            }
                            Switch(
                                checked = card.reminderEnabled,
                                onCheckedChange = { onToggleReminder(card, it) }
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedButton(
                            onClick = { onTestAlarm(card.id) },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                Icons.Filled.NotificationsActive,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Tes Alarm Pengingat Sekarang", fontSize = 12.5.sp)
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(72.dp)) }
    }
}
