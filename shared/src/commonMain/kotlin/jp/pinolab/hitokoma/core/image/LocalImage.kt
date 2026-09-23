package jp.pinolab.hitokoma.core.image

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale

/**
 * ローカルストレージの画像を、長辺が maxSize 程度になるよう縮小して読み込む。読み込めない場合は null
 */
expect suspend fun loadThumbnail(path: String, maxSize: Int): ImageBitmap?

/**
 * ローカルストレージの画像を表示する。読み込み中・失敗時はプレースホルダを表示する
 */
@Composable
fun LocalImage(
    path: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    maxSize: Int = 512,
    contentScale: ContentScale = ContentScale.Crop
) {
    val bitmap by produceState<ImageBitmap?>(initialValue = null, path, maxSize) {
        value = loadThumbnail(path, maxSize)
    }

    val image = bitmap
    if (image != null) {
        Image(
            bitmap = image,
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale
        )
    } else {
        Box(modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant))
    }
}
