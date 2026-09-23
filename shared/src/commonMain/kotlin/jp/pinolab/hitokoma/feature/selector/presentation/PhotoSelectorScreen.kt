package jp.pinolab.hitokoma.feature.selector.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.vinceglb.filekit.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.core.PickerMode
import io.github.vinceglb.filekit.core.PickerType
import jp.pinolab.hitokoma.core.file.LocalImageStorage
import jp.pinolab.hitokoma.core.image.LocalImage
import jp.pinolab.hitokoma.core.time.toJapaneseString
import jp.pinolab.hitokoma.domain.model.DailyPhoto
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

@Composable
fun PhotoSelectorScreen(
    viewModel: PhotoSelectorViewModel,
    imageStorage: LocalImageStorage, // DI(Koin等)またはLocalProviderから渡す
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    // FileKit の画像ピッカーランチャーを初期化
    val launcher = rememberFilePickerLauncher(
        type = PickerType.Image,
        mode = PickerMode.Single
    ) { file ->
        file?.let { platformFile ->
            scope.launch {
                // 1. 選択された画像からバイト配列を読み込む
                val bytes = platformFile.readBytes()

                // 2. ユニークなファイル名（タイムスタンプなど）を生成して内部ストレージへ保存
                val fileName = "photo_${Clock.System.now().toEpochMilliseconds()}.jpg"
                val savedLocalPath = imageStorage.saveImage(bytes, fileName)

                // 3. 永続化可能なローカル絶対パスを ViewModel に渡す
                viewModel.onImageSelected(savedLocalPath)
            }
        }
    }

    // 今日すでに登録済みなら、その日のあいだは登録した写真を表示する
    uiState.todayPhoto?.let { todayPhoto ->
        RegisteredTodayPhoto(
            photo = todayPhoto,
            modifier = modifier
        )
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .safeDrawingPadding() // ステータスバー・ナビゲーションバー・ノッチ・IMEを避ける
            .padding(16.dp)
    ) {
        // 画像選択ボタン
        Button(onClick = { launcher.launch() }) {
            Text("ギャラリーから写真を選択")
        }

        // 選択された画像のパス表示（実際は Coil 3 等で画像プレビューを表示）
        uiState.selectedImagePath?.let { path ->
            Text(text = "保存先パス: $path")
        }

        // コメント入力ボックス
        OutlinedTextField(
            value = uiState.comment,
            onValueChange = { viewModel.onCommentChanged(it) },
            label = { Text("一言コメント") }
        )

        // 保存ボタン
        Button(
            onClick = { viewModel.onSaveClicked() },
            enabled = uiState.canSave
        ) {
            Text("今日の一枚として登録")
        }
    }
}
/**
 * 今日登録済みの写真を表示する
 */
@Composable
private fun RegisteredTodayPhoto(
    photo: DailyPhoto,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .safeDrawingPadding() // ステータスバー・ナビゲーションバー・ノッチ・IMEを避ける
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "今日の一枚",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = photo.date.toJapaneseString(),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(24.dp))

        LocalImage(
            path = photo.imagePath,
            contentDescription = "${photo.date.toJapaneseString()}の写真",
            maxSize = 1080,
            modifier = Modifier
                .widthIn(max = 480.dp) // 横向きで大きくなりすぎないようにする
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(24.dp))
        )

        if (photo.comment.isNotBlank()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = photo.comment,
                style = MaterialTheme.typography.bodyLarge,
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "明日また新しい一枚を選びましょう",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
