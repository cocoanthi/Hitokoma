package jp.pinolab.hitokoma.core.file

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

actual class LocalImageStorage(private val context: Context) {
    actual suspend fun saveImage(bytes: ByteArray, fileName: String): String = withContext(Dispatchers.IO) {
        val photosDir = File(context.filesDir, "photos").apply { if (!exists()) mkdirs() }
        val targetFile = File(photosDir, fileName)
        targetFile.writeBytes(bytes)
        targetFile.absolutePath // 例: /data/user/0/com.example/files/photos/2026-08-01.jpg
    }
}