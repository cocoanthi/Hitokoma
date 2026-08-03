package jp.pinolab.hitokoma

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import jp.pinolab.hitokoma.core.file.LocalImageStorage
import jp.pinolab.hitokoma.feature.selector.presentation.PhotoSelectorScreen
import jp.pinolab.hitokoma.feature.selector.presentation.PhotoSelectorViewModel
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
@Preview
fun App() {
    MaterialTheme {
        // Koinから ViewModel と LocalImageStorage を注入
        val viewModel: PhotoSelectorViewModel = koinViewModel()
        val imageStorage: LocalImageStorage = koinInject()

        // 画面の呼び出し
        PhotoSelectorScreen(
            viewModel = viewModel,
            imageStorage = imageStorage
        )
    }
}