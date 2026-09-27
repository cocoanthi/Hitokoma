package jp.pinolab.hitokoma

import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import jp.pinolab.hitokoma.core.file.LocalImageStorage
import jp.pinolab.hitokoma.feature.gallery.presentation.PhotoCalendarScreen
import jp.pinolab.hitokoma.feature.gallery.presentation.PhotoCalendarViewModel
import jp.pinolab.hitokoma.feature.selector.presentation.PhotoSelectorScreen
import jp.pinolab.hitokoma.feature.selector.presentation.PhotoSelectorViewModel
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

/**
 * 画面下部のタブ
 */
private enum class AppTab(val label: String, val icon: ImageVector) {
    Today(label = "今日の一枚", icon = Icons.Default.Add),
    Calendar(label = "カレンダー", icon = Icons.Default.DateRange),
}

/**
 * @param openVideoMonth 通知から起動したときに再生するストーリー動画の月。指定されたらカレンダータブで再生する
 * @param onVideoMonthOpened openVideoMonth を処理したときに呼ばれる（同じ指定で何度も開かないように）
 * @param debugMode デバッグビルドのみ true。カレンダーに写真の追加・動画の手動作成を出す
 */
@Composable
@Preview
fun App(
    openVideoMonth: LocalDate? = null,
    onVideoMonthOpened: () -> Unit = {},
    debugMode: Boolean = false
) {
    MaterialTheme {
        var selectedTab by rememberSaveable { mutableStateOf(AppTab.Today) }

        LaunchedEffect(openVideoMonth) {
            if (openVideoMonth != null) selectedTab = AppTab.Calendar
        }

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

                AppTab.Calendar -> {
                    val viewModel: PhotoCalendarViewModel = koinViewModel()

                    LaunchedEffect(openVideoMonth) {
                        if (openVideoMonth != null) {
                            viewModel.openVideo(openVideoMonth)
                            onVideoMonthOpened()
                        }
                    }

                    PhotoCalendarScreen(
                        viewModel = viewModel,
                        modifier = contentModifier,
                        debugMode = debugMode
                    )
                }
            }
        }
    }
}
