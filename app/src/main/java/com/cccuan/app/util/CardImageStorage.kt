package com.cccuan.app.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

data class ImageSaveResult(
    val savedPath: String? = null,
    val errorMessage: String? = null
)

object CardImageStorage {

    private const val DIR_NAME = "card_images"
    private const val MAX_DIMENSION = 1080

    fun saveCardImageFromUri(context: Context, uri: Uri): ImageSaveResult {
        val tempFile = File(context.cacheDir, "tmp_card_import_${System.currentTimeMillis()}.bin")
        return try {
            // 1. Salin URI sekali saja ke file cache lokal agar aman dari bug multi-stream MIUI/ContentProvider
            val copiedBytes = copyUriToTempFile(context, uri, tempFile)
            if (copiedBytes <= 0L) {
                return ImageSaveResult(
                    errorMessage = "File gambar kosong atau tidak bisa dibaca dari sistem."
                )
            }

            // 2. Decode dengan ImageDecoder (Android 9+: JPG, PNG, WebP, HEIC, AVIF, GIF, BMP) + fallback BitmapFactory
            val decodedBitmap = decodeBitmapRobust(tempFile)
                ?: return ImageSaveResult(
                    errorMessage = "Format gambar tidak didukung. Gunakan file JPG, PNG, WebP, atau HEIC."
                )

            // 3. Pastikan software bitmap agar aman saat dikompres ke file
            val softwareBitmap = if (
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
                decodedBitmap.config == Bitmap.Config.HARDWARE
            ) {
                val copy = decodedBitmap.copy(Bitmap.Config.ARGB_8888, false)
                decodedBitmap.recycle()
                copy
            } else {
                decodedBitmap
            }

            // 4. Resize proporsional jika dimensi masih melebihi batas
            val finalBitmap = scaleDownIfNeeded(softwareBitmap, MAX_DIMENSION)
            if (finalBitmap != softwareBitmap) {
                softwareBitmap.recycle()
            }

            // 5. Simpan permanen ke penyimpanan internal aplikasi (PNG jika transparan, JPG jika solid)
            val dir = File(context.filesDir, DIR_NAME).apply { mkdirs() }
            val preserveAlpha = finalBitmap.hasAlpha()
            val ext = if (preserveAlpha) "png" else "jpg"
            val outFile = File(dir, "card_${System.currentTimeMillis()}.$ext")

            FileOutputStream(outFile).use { fos ->
                if (preserveAlpha) {
                    finalBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
                } else {
                    finalBitmap.compress(Bitmap.CompressFormat.JPEG, 88, fos)
                }
            }
            finalBitmap.recycle()

            if (!outFile.exists() || outFile.length() <= 0L) {
                return ImageSaveResult(
                    errorMessage = "Gagal menyimpan salinan gambar ke memori aplikasi."
                )
            }

            ImageSaveResult(savedPath = outFile.absolutePath)
        } catch (_: SecurityException) {
            ImageSaveResult(
                errorMessage = "Akses file ditolak sistem. Coba pilih gambar lewat tombol Galeri."
            )
        } catch (_: OutOfMemoryError) {
            ImageSaveResult(
                errorMessage = "Resolusi gambar terlalu besar untuk memori perangkat."
            )
        } catch (e: Exception) {
            ImageSaveResult(
                errorMessage = "Gagal memuat gambar (${e.localizedMessage ?: e.javaClass.simpleName})."
            )
        } finally {
            if (tempFile.exists()) {
                tempFile.delete()
            }
        }
    }

    private fun copyUriToTempFile(context: Context, uri: Uri, destFile: File): Long {
        val resolver = context.contentResolver

        // Cara 1: openInputStream standar
        try {
            resolver.openInputStream(uri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    return input.copyTo(output)
                }
            }
        } catch (_: Exception) {
            // Lanjut ke fallback ParcelFileDescriptor
        }

        // Cara 2: openFileDescriptor (sering berhasil pada beberapa Gallery/File Manager MIUI/HyperOS)
        return try {
            resolver.openFileDescriptor(uri, "r")?.use { pfd ->
                FileInputStream(pfd.fileDescriptor).use { input ->
                    FileOutputStream(destFile).use { output ->
                        input.copyTo(output)
                    }
                }
            } ?: 0L
        } catch (_: Exception) {
            0L
        }
    }

    private fun decodeBitmapRobust(file: File): Bitmap? {
        // Primary: Android 9+ ImageDecoder (dukung WebP, HEIC, AVIF, PNG, JPG & auto-rotasi EXIF)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                val source = ImageDecoder.createSource(file)
                return ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                    decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                    val w = info.size.width
                    val h = info.size.height
                    if (w > MAX_DIMENSION || h > MAX_DIMENSION) {
                        val ratioW = MAX_DIMENSION.toFloat() / w.toFloat()
                        val ratioH = MAX_DIMENSION.toFloat() / h.toFloat()
                        val ratio = if (ratioW < ratioH) ratioW else ratioH
                        val targetW = (w * ratio).toInt().coerceAtLeast(1)
                        val targetH = (h * ratio).toInt().coerceAtLeast(1)
                        decoder.setTargetSize(targetW, targetH)
                    }
                }
            } catch (_: Exception) {
                // Lanjut ke fallback BitmapFactory
            }
        }

        // Fallback: BitmapFactory dari file lokal + koreksi rotasi EXIF
        return decodeWithBitmapFactory(file)
    }

    private fun decodeWithBitmapFactory(file: File): Bitmap? {
        return try {
            val boundsOptions = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeFile(file.absolutePath, boundsOptions)

            var sampleSize = 1
            if (boundsOptions.outWidth > 0 && boundsOptions.outHeight > 0) {
                while (
                    (boundsOptions.outWidth / sampleSize) > MAX_DIMENSION ||
                    (boundsOptions.outHeight / sampleSize) > MAX_DIMENSION
                ) {
                    sampleSize *= 2
                }
            }

            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            val decoded = BitmapFactory.decodeFile(file.absolutePath, decodeOptions)
                ?: return null

            val rotationDegrees = try {
                val exif = ExifInterface(file.absolutePath)
                when (
                    exif.getAttributeInt(
                        ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_NORMAL
                    )
                ) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                    else -> 0f
                }
            } catch (_: Exception) {
                0f
            }

            if (rotationDegrees != 0f) {
                val matrix = Matrix().apply { postRotate(rotationDegrees) }
                val rotated = Bitmap.createBitmap(
                    decoded,
                    0,
                    0,
                    decoded.width,
                    decoded.height,
                    matrix,
                    true
                )
                if (rotated != decoded) {
                    decoded.recycle()
                }
                rotated
            } else {
                decoded
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun scaleDownIfNeeded(bitmap: Bitmap, maxDim: Int): Bitmap {
        val w = bitmap.width
        val h = bitmap.height
        if (w <= maxDim && h <= maxDim) return bitmap
        val ratioW = maxDim.toFloat() / w.toFloat()
        val ratioH = maxDim.toFloat() / h.toFloat()
        val ratio = if (ratioW < ratioH) ratioW else ratioH
        val targetW = (w * ratio).toInt().coerceAtLeast(1)
        val targetH = (h * ratio).toInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(bitmap, targetW, targetH, true)
    }

    fun loadCardBitmap(path: String): Bitmap? {
        if (path.isBlank()) return null
        return try {
            val file = File(path)
            if (!file.exists()) return null
            val opts = BitmapFactory.Options().apply {
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            BitmapFactory.decodeFile(file.absolutePath, opts)
        } catch (_: Exception) {
            null
        }
    }

    fun deleteImageFile(path: String) {
        if (path.isBlank()) return
        try {
            val file = File(path)
            if (file.exists() && file.parentFile?.name == DIR_NAME) {
                file.delete()
            }
        } catch (_: Exception) {
            // Abaikan error penghapusan file
        }
    }
}
