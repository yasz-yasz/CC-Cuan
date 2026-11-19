package com.cccuan.app.util

import android.content.ContentValues
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.cccuan.app.data.CardRecord
import com.cccuan.app.data.CardSummary
import com.cccuan.app.data.RecordType
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

data class PdfExportResult(
    val shareUri: Uri,
    val fileName: String,
    val savedToDownloads: Boolean
)

object PdfExporter {

    private const val PAGE_WIDTH = 595   // A4 width in points
    private const val PAGE_HEIGHT = 842  // A4 height in points
    private const val MARGIN = 36f

    fun exportReport(
        context: Context,
        summaries: List<CardSummary>,
        records: List<CardRecord>,
        filterCardName: String? = null
    ): PdfExportResult {
        val document = PdfDocument()
        val cardNameMap = summaries.associate { it.card.id to it.card.name }

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(203, 213, 225)
            textSize = 10f
        }
        val sectionPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(15, 23, 42)
            textSize = 13f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val headerTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(30, 41, 59)
            textSize = 9.2f
        }
        val bodyBoldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(15, 23, 42)
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val greenPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(5, 150, 105)
            textSize = 9.2f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val redPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(185, 28, 28)
            textSize = 9.2f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(100, 116, 139)
            textSize = 8.5f
        }
        val navyFill = Paint().apply { color = Color.rgb(15, 23, 42) }
        val tableHeaderFill = Paint().apply { color = Color.rgb(30, 41, 59) }
        val zebraFill = Paint().apply { color = Color.rgb(248, 250, 252) }
        val cardBoxFill = Paint().apply { color = Color.rgb(241, 245, 249) }
        val dividerPaint = Paint().apply {
            color = Color.rgb(203, 213, 225)
            strokeWidth = 0.8f
        }

        var pageNumber = 1
        var currentPage = document.startPage(
            PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
        )
        var canvas: Canvas = currentPage.canvas
        var y = 0f

        val now = LocalDateTime.now()
        val exportTimeText = now.format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm", Locale("id", "ID")))

        fun drawFooter(c: Canvas, pageNum: Int) {
            c.drawLine(MARGIN, PAGE_HEIGHT - 34f, PAGE_WIDTH - MARGIN, PAGE_HEIGHT - 34f, dividerPaint)
            c.drawText("CC Cuan — Credit Card Tracker", MARGIN, PAGE_HEIGHT - 20f, footerPaint)
            val pageStr = "Halaman $pageNum"
            val w = footerPaint.measureText(pageStr)
            c.drawText(pageStr, PAGE_WIDTH - MARGIN - w, PAGE_HEIGHT - 20f, footerPaint)
        }

        fun startNewPage() {
            drawFooter(canvas, pageNumber)
            document.finishPage(currentPage)
            pageNumber++
            currentPage = document.startPage(
                PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
            )
            canvas = currentPage.canvas
            // Compact continuation header
            canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 36f, navyFill)
            canvas.drawText("CC CUAN — Lanjutan Laporan", MARGIN, 23f, headerTextPaint)
            y = 56f
        }

        fun ensureSpace(neededHeight: Float) {
            if (y + neededHeight > PAGE_HEIGHT - 46f) {
                startNewPage()
            }
        }

        fun clipText(text: String, paint: Paint, maxWidth: Float): String {
            if (paint.measureText(text) <= maxWidth) return text
            var cut = text
            while (cut.length > 3 && paint.measureText("$cut...") > maxWidth) {
                cut = cut.dropLast(1)
            }
            return "$cut..."
        }

        // Top Banner
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 76f, navyFill)
        canvas.drawText("CC CUAN — LAPORAN KARTU KREDIT", MARGIN, 36f, titlePaint)
        val filterLabel = filterCardName?.let { "Filter: $it" } ?: "Semua Kartu Kredit"
        canvas.drawText("$filterLabel  •  Dicetak: $exportTimeText", MARGIN, 56f, subtitlePaint)
        y = 94f

        // Overall KPI Box
        val totalLimit = summaries.sumOf { it.card.creditLimit }
        val totalBalance = summaries.sumOf { it.currentBalance }
        val totalAvailable = summaries.sumOf { it.availableLimit }

        canvas.drawRoundRect(MARGIN, y, PAGE_WIDTH - MARGIN, y + 54f, 6f, 6f, cardBoxFill)
        val colW = (PAGE_WIDTH - MARGIN * 2) / 3f
        canvas.drawText("Total Limit", MARGIN + 12f, y + 18f, footerPaint)
        canvas.drawText(Formatters.formatRupiah(totalLimit), MARGIN + 12f, y + 38f, bodyBoldPaint)

        canvas.drawText("Total Tagihan / Terpakai", MARGIN + colW + 12f, y + 18f, footerPaint)
        canvas.drawText(Formatters.formatRupiah(totalBalance), MARGIN + colW + 12f, y + 38f, redPaint)

        canvas.drawText("Sisa Limit Tersedia", MARGIN + colW * 2 + 12f, y + 18f, footerPaint)
        canvas.drawText(Formatters.formatRupiah(totalAvailable), MARGIN + colW * 2 + 12f, y + 38f, greenPaint)
        y += 74f

        // Section 1: Ringkasan Kartu Kredit
        canvas.drawText("1. Ringkasan Kartu Kredit (${summaries.size} Kartu)", MARGIN, y, sectionPaint)
        y += 12f

        // Table Header - Cards
        canvas.drawRect(MARGIN, y, PAGE_WIDTH - MARGIN, y + 20f, tableHeaderFill)
        canvas.drawText("Nama Kartu", MARGIN + 6f, y + 13f, headerTextPaint)
        canvas.drawText("Limit", MARGIN + 140f, y + 13f, headerTextPaint)
        canvas.drawText("Terpakai", MARGIN + 235f, y + 13f, headerTextPaint)
        canvas.drawText("Sisa Limit", MARGIN + 330f, y + 13f, headerTextPaint)
        canvas.drawText("Cetak / Tempo", MARGIN + 430f, y + 13f, headerTextPaint)
        y += 20f

        if (summaries.isEmpty()) {
            canvas.drawText("Belum ada data kartu kredit.", MARGIN + 6f, y + 15f, bodyPaint)
            y += 24f
        } else {
            summaries.forEachIndexed { idx, summary ->
                ensureSpace(22f)
                if (idx % 2 == 1) {
                    canvas.drawRect(MARGIN, y, PAGE_WIDTH - MARGIN, y + 20f, zebraFill)
                }
                val label = "${summary.card.name} (${summary.card.bankName})"
                canvas.drawText(clipText(label, bodyPaint, 130f), MARGIN + 6f, y + 13f, bodyPaint)
                canvas.drawText(Formatters.formatRupiah(summary.card.creditLimit), MARGIN + 140f, y + 13f, bodyPaint)
                canvas.drawText(Formatters.formatRupiah(summary.currentBalance), MARGIN + 235f, y + 13f, redPaint)
                canvas.drawText(Formatters.formatRupiah(summary.availableLimit), MARGIN + 330f, y + 13f, greenPaint)
                canvas.drawText("Tgl ${summary.card.billingDay} / Tgl ${summary.card.dueDay}", MARGIN + 430f, y + 13f, bodyPaint)
                y += 20f
                canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, dividerPaint)
            }
        }

        y += 22f

        // Section 2: Riwayat Transaksi
        val transactions = records.filter { it.type == RecordType.TRANSACTION }
        ensureSpace(46f)
        canvas.drawText("2. Riwayat Transaksi (${transactions.size} Item)", MARGIN, y, sectionPaint)
        y += 12f

        fun drawTxTableHeader() {
            canvas.drawRect(MARGIN, y, PAGE_WIDTH - MARGIN, y + 20f, tableHeaderFill)
            canvas.drawText("Tanggal", MARGIN + 6f, y + 13f, headerTextPaint)
            canvas.drawText("Kartu", MARGIN + 82f, y + 13f, headerTextPaint)
            canvas.drawText("Nama Transaksi", MARGIN + 188f, y + 13f, headerTextPaint)
            canvas.drawText("Kategori", MARGIN + 345f, y + 13f, headerTextPaint)
            canvas.drawText("Nominal", MARGIN + 425f, y + 13f, headerTextPaint)
            y += 20f
        }
        drawTxTableHeader()

        if (transactions.isEmpty()) {
            canvas.drawText("Belum ada transaksi tercatat.", MARGIN + 6f, y + 15f, bodyPaint)
            y += 24f
        } else {
            transactions.forEachIndexed { idx, rec ->
                if (y + 22f > PAGE_HEIGHT - 46f) {
                    startNewPage()
                    drawTxTableHeader()
                }
                if (idx % 2 == 1) {
                    canvas.drawRect(MARGIN, y, PAGE_WIDTH - MARGIN, y + 20f, zebraFill)
                }
                val cardName = cardNameMap[rec.cardId] ?: "-"
                canvas.drawText(Formatters.formatDateShort(rec.dateEpochDay), MARGIN + 6f, y + 13f, bodyPaint)
                canvas.drawText(clipText(cardName, bodyPaint, 100f), MARGIN + 82f, y + 13f, bodyPaint)
                canvas.drawText(clipText(rec.title, bodyPaint, 150f), MARGIN + 188f, y + 13f, bodyPaint)
                canvas.drawText(clipText(rec.category.ifBlank { "-" }, bodyPaint, 75f), MARGIN + 345f, y + 13f, bodyPaint)
                canvas.drawText(Formatters.formatRupiah(rec.amount), MARGIN + 425f, y + 13f, redPaint)
                y += 20f
                canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, dividerPaint)
            }
            ensureSpace(24f)
            val totalTxSum = transactions.sumOf { it.amount }
            canvas.drawText("Total Transaksi:", MARGIN + 335f, y + 15f, bodyBoldPaint)
            canvas.drawText(Formatters.formatRupiah(totalTxSum), MARGIN + 425f, y + 15f, redPaint)
            y += 24f
        }

        y += 18f

        // Section 3: Riwayat Pembayaran / Pelunasan
        val payments = records.filter { it.type == RecordType.PAYMENT }
        ensureSpace(46f)
        canvas.drawText("3. Riwayat Pembayaran / Pelunasan (${payments.size} Item)", MARGIN, y, sectionPaint)
        y += 12f

        fun drawPayTableHeader() {
            canvas.drawRect(MARGIN, y, PAGE_WIDTH - MARGIN, y + 20f, tableHeaderFill)
            canvas.drawText("Tanggal", MARGIN + 6f, y + 13f, headerTextPaint)
            canvas.drawText("Kartu", MARGIN + 88f, y + 13f, headerTextPaint)
            canvas.drawText("Keterangan Pembayaran", MARGIN + 205f, y + 13f, headerTextPaint)
            canvas.drawText("Nominal", MARGIN + 425f, y + 13f, headerTextPaint)
            y += 20f
        }
        drawPayTableHeader()

        if (payments.isEmpty()) {
            canvas.drawText("Belum ada pembayaran tercatat.", MARGIN + 6f, y + 15f, bodyPaint)
            y += 24f
        } else {
            payments.forEachIndexed { idx, rec ->
                if (y + 22f > PAGE_HEIGHT - 46f) {
                    startNewPage()
                    drawPayTableHeader()
                }
                if (idx % 2 == 1) {
                    canvas.drawRect(MARGIN, y, PAGE_WIDTH - MARGIN, y + 20f, zebraFill)
                }
                val cardName = cardNameMap[rec.cardId] ?: "-"
                val desc = if (rec.note.isNotBlank()) "${rec.title} (${rec.note})" else rec.title
                canvas.drawText(Formatters.formatDateShort(rec.dateEpochDay), MARGIN + 6f, y + 13f, bodyPaint)
                canvas.drawText(clipText(cardName, bodyPaint, 110f), MARGIN + 88f, y + 13f, bodyPaint)
                canvas.drawText(clipText(desc, bodyPaint, 212f), MARGIN + 205f, y + 13f, bodyPaint)
                canvas.drawText(Formatters.formatRupiah(rec.amount), MARGIN + 425f, y + 13f, greenPaint)
                y += 20f
                canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, dividerPaint)
            }
            ensureSpace(24f)
            val totalPaySum = payments.sumOf { it.amount }
            canvas.drawText("Total Pembayaran:", MARGIN + 325f, y + 15f, bodyBoldPaint)
            canvas.drawText(Formatters.formatRupiah(totalPaySum), MARGIN + 425f, y + 15f, greenPaint)
            y += 24f
        }

        drawFooter(canvas, pageNumber)
        document.finishPage(currentPage)

        val fileStamp = now.format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss", Locale.US))
        val fileName = "CC_Cuan_Laporan_$fileStamp.pdf"

        // Save to cache for FileProvider sharing/opening
        val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val cacheFile = File(exportDir, fileName)
        FileOutputStream(cacheFile).use { fos ->
            document.writeTo(fos)
        }

        // Also save a copy to Downloads folder on Android 10+
        var savedToDownloads = false
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                    put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
                    put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/CC_Cuan")
                }
                val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                if (uri != null) {
                    context.contentResolver.openOutputStream(uri)?.use { out ->
                        cacheFile.inputStream().use { input -> input.copyTo(out) }
                    }
                    savedToDownloads = true
                }
            }
        } catch (_: Exception) {
            savedToDownloads = false
        }

        document.close()

        val shareUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            cacheFile
        )

        return PdfExportResult(
            shareUri = shareUri,
            fileName = fileName,
            savedToDownloads = savedToDownloads
        )
    }
}
