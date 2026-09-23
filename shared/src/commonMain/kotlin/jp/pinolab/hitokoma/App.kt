package jp.pinolab.hitokoma

import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import jp.pinolab.hitokoma.core.file.LocalImageStorage
import jp.pinolab.hitokoma.feature.gallery.presentation.PhotoListScreen
import jp.pinolab.hitokoma.feature.gallery.presentation.PhotoListViewModel
import jp.pinolab.hitokoma.feature.selector.presentation.PhotoSelectorScreen
import jp.pinolab.hitokoma.feature.selector.presentation.PhotoSelectorViewModel
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

/**
 * 画面下部のタブ
 */
private enum class AppTab(val label: String, val icon: ImageVector) {
    Today(label = "今日の一枚", icon = Icons.Default.Add),
    List(label = "一覧", icon = Icons.AutoMirrored.Filled.List),
}

@Composable
@Preview
fun App() {
    MaterialTheme {
        var selectedTab by rememberSaveable { mutableStateOf(AppTab.Today) }

        Scaffold(
            bottomBar = {
                NavigationBar {
                    AppTab.entries.forEach { tab ->
                        NavigationBarItem(
                            selected = selectedTab == tab,
                            onClick = { selectedTab = tab },
                            icon = { Icon(imageVector = tab.icon, contentDescription = null) },
                            label = { Text(tab.label) }
                        )
                    }
                }
            }
        ) { innerPadding ->
            // Scaffold が処理したインセットを消費し、各画面の safeDrawingPadding と二重にならないようにする
            val contentModifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)

            when (selectedTab) {
                AppTab.Today -> {
                    // Koinから ViewModel と LocalImageStorage を注入
                    val viewModel: PhotoSelectorViewModel = koinViewModel()
                    val imageStorage: LocalImageStorage = koinInject()

                    PhotoSelectorScreen(
                        viewModel = viewModel,
                        imageStorage = imageStorage,
                        modifier = contentModifier
                    )
                }

                AppTab.List -> {
                    val viewModel: PhotoListViewModel = koinViewModel()

                    PhotoListScreen(
                        viewModel = viewModel,
                        modifier = contentModifier
                    )
                }
            }
        }
    }
}
