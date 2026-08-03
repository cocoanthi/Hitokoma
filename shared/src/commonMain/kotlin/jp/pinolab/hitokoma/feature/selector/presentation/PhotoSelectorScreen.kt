package jp.pinolab.hitokoma.feature.selector.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import jp.pinolab.hitokoma.core.file.LocalImageStorage
import io.github.vinceglb.filekit.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.core.PickerMode
import io.github.vinceglb.filekit.core.PickerType
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

@Composable
private fun TodayPhotoScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(
            modifier = Modifier.height(48.dp),
        )

        Text(
            text = "Hitokoma",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
        )

        Spacer(
            modifier = Modifier.height(40.dp),
        )

        Text(
            text = "今日の一枚",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
        )

        Spacer(
            modifier = Modifier.height(8.dp),
        )

        Text(
            text = "2026年7月30日",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(
            modifier = Modifier.height(28.dp),
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(24.dp),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = "＋",
                    fontSize = 48.sp,
                    color = MaterialTheme.colorScheme.primary,
                )

                Spacer(
                    modifier = Modifier.height(8.dp),
                )

                Text(
                    text = "今日はまだ写真がありません",
                )

                Text(
                    text = "一日の代表写真を1枚選びましょう",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Spacer(
            modifier = Modifier.height(24.dp),
        )

        Button(
            onClick = {
                // 後で写真選択処理を実装する
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Text(
                text = "写真を選ぶ",
            )
        }

        Spacer(
            modifier = Modifier.weight(1f),
        )

        Text(
            text = "1日1枚。今日の記憶を選ぶ。",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 24.dp),
        )
    }
}

@Composable
fun PhotoSelectorScreen(
    viewModel: PhotoSelectorViewModel,
    imageStorage: LocalImageStorage // DI(Koin等)またはLocalProviderから渡す
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

    Column(modifier = Modifier.padding(16.dp)) {
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