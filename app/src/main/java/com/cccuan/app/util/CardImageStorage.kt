package com.cccuan.app.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

object CardImageStorage {

    private const val DIR_NAME = "card_images"
    private const val MAX_DIMENSION = 1080

    fun saveCardImageFromUri(context: Context, uri: Uri): String? {
        return try {
            val resolver = context.contentResolver

            // 1. Baca dimensi asli gambar
            val boundsOptions = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            resolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, boundsOptions)
            } ?: return null

            if (boundsOptions.outWidth <= 0 || boundsOptions.outHeight <= 0) {
                return null
            }

            // 2. Hitung sample size agar hemat RAM dan cepat dimuat
            var sampleSize = 1
            while (
                (boundsOptions.outWidth / sampleSize) > MAX_DIMENSION ||
                (boundsOptions.outHeight / sampleSize) > MAX_DIMENSION
            ) {
                sampleSize *= 2
            }

            // 3. Baca orientasi EXIF agar foto tidak terputar
            val rotationDegrees = try {
                resolver.openInputStream(uri)?.use { exifStream ->
                    val exif = ExifInterface(exifStream)
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
                } ?: 0f
            } catch (_: Exception) {
                0f
            }

            // 4. Decode bitmap dengan sample size
            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
            }
            val decoded = resolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, decodeOptions)
            } ?: return null

            val finalBitmap = if (rotationDegrees != 0f) {
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

            // 5. Simpan ke internal storage aplikasi agar permanen
            val dir = File(context.filesDir, DIR_NAME).apply { mkdirs() }
            val outFile = File(dir, "card_${System.currentTimeMillis()}.jpg")
            FileOutputStream(outFile).use { fos ->
                finalBitmap.compress(Bitmap.CompressFormat.JPEG, 85, fos)
            }
            finalBitmap.recycle()

            outFile.absolutePath
        } catch (_: Exception) {
            null
        }
    }

    fun loadCardBitmap(path: String): Bitmap? {
        if (path.isBlank()) return null
        return try {
            val file = File(path)
            if (!file.exists()) return null
            BitmapFactory.decodeFile(file.absolutePath)
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
