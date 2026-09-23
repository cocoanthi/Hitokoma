package jp.pinolab.hitokoma.feature.gallery.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import jp.pinolab.hitokoma.core.image.LocalImage
import jp.pinolab.hitokoma.core.time.toJapaneseString
import jp.pinolab.hitokoma.domain.model.DailyPhoto

@Composable
fun PhotoListScreen(
    viewModel: PhotoListViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .safeDrawingPadding(), // ステータスバー・ノッチを避ける
        contentAlignment = Alignment.Center
    ) {
        when {
            uiState.isLoading -> CircularProgressIndicator()
            uiState.isEmpty -> EmptyPhotoList()
            else -> PhotoGrid(photos = uiState.photos)
        }
    }
}

@Composable
private fun PhotoGrid(photos: List<DailyPhoto>) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 160.dp), // 画面幅に応じて列数を変える（縦向きで2列）
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(photos, key = { it.date.toString() }) { photo ->
            PhotoCard(photo = photo)
        }
    }
}

@Composable
private fun PhotoCard(photo: DailyPhoto) {
    Card(shape = RoundedCornerShape(16.dp)) {
        LocalImage(
            path = photo.imagePath,
            contentDescription = "${photo.date.toJapaneseString()}の写真",
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
        )

        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = photo.date.toJapaneseString(),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )

            if (photo.comment.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = photo.comment,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun EmptyPhotoList() {
    Column(
        modifier = Modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "まだ写真がありません",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "「今日の一枚」タブから写真を登録しましょう",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
