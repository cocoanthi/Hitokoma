package jp.pinolab.hitokoma.core.image

import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

actual suspend fun loadThumbnail(path: String, maxSize: Int): ImageBitmap? = withContext(Dispatchers.IO) {
    // 1. 画像サイズだけを先に読み取る
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(path, bounds)
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return@withContext null

    // 2. 長辺が maxSize を下回らない範囲で 2 の累乗ずつ縮小してデコード（メモリ節約のため）
    var sampleSize = 1
    while (maxOf(bounds.outWidth, bounds.outHeight) / (sampleSize * 2) >= maxSize) {
        sampleSize *= 2
    }
    val options = BitmapFactory.Options().apply { inSampleSize = sampleSize }
    BitmapFactory.decodeFile(path, options)?.asImageBitmap()
}
