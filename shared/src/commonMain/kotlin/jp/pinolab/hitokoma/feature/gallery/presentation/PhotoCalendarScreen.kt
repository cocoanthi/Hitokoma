package jp.pinolab.hitokoma.feature.gallery.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import io.github.vinceglb.filekit.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.core.PickerMode
import io.github.vinceglb.filekit.core.PickerType
import jp.pinolab.hitokoma.core.image.LocalImage
import jp.pinolab.hitokoma.core.time.toJapaneseString
import jp.pinolab.hitokoma.core.time.toJapaneseYearMonthString
import jp.pinolab.hitokoma.core.video.FullScreenDialogEffect
import jp.pinolab.hitokoma.core.video.VideoPlayer
import jp.pinolab.hitokoma.core.video.rememberVideoSharer
import jp.pinolab.hitokoma.domain.model.DailyPhoto
import kotlinx.coroutines.launch
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate

private val SaturdayBlue = Color(0xFF1E6FD9)
private val DayOfWeekLabels = listOf("日", "月", "火", "水", "木", "金", "土")

@Composable
fun PhotoCalendarScreen(
    viewModel: PhotoCalendarViewModel,
    modifier: Modifier = Modifier,
    debugMode: Boolean = false // デバッグビルドのみ true（写真の追加・動画の手動作成を出す）
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .safeDrawingPadding(), // ステータスバー・ノッチを避ける
        contentAlignment = Alignment.Center
    ) {
        if (uiState.isLoading) {
            CircularProgressIndicator()
        } else {
            // 6週ある月でも収まるよう縦スクロール可能にする
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                MonthHeader(
                    month = uiState.displayedMonth,
                    canGoNextMonth = uiState.canGoNextMonth,
                    onPreviousMonth = viewModel::onPreviousMonth,
                    onNextMonth = viewModel::onNextMonth
                )

                // 先月以前のストーリー動画が生成済みなら再生ボタンを出す
                uiState.videoPath?.let {
                    FilledTonalButton(
                        onClick = viewModel::onPlayVideo,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("${uiState.displayedMonth.monthNumber}月のストーリーを見る")
                    }
                }

                if (debugMode) {
                    DebugPanel(
                        month = uiState.displayedMonth,
                        isCreatingVideo = uiState.isCreatingVideo,
                        onCreateVideoClick = viewModel::onDebugCreateVideoClicked
                    )
                }

                DayOfWeekRow()

                Spacer(modifier = Modifier.height(4.dp))

                CalendarGrid(
                    cells = uiState.cells,
                    today = uiState.today,
                    photosByDate = uiState.photosByDate,
                    onDayClick = viewModel::onDayClicked,
                    // デバッグモードでは写真のない日もタップして写真を追加できる
                    onEmptyDayClick = if (debugMode) viewModel::onDebugEmptyDayClicked else null
                )
            }
        }
    }

    // ストーリー動画のプレイヤー
    val videoPath = uiState.videoPath
    if (uiState.isVideoPlaying && videoPath != null) {
        VideoPlayerDialog(
            path = videoPath,
            onDismiss = viewModel::onDismissVideo
        )
    }

    // デバッグ用: 写真追加シート
    uiState.debugAddPhoto?.let { addPhoto ->
        DebugAddPhotoSheet(
            state = addPhoto,
            onImagePicked = viewModel::onDebugImagePicked,
            onCommentChange = viewModel::onDebugCommentChanged,
            onSaveClick = viewModel::onDebugSaveClicked,
            onDismiss = viewModel::onDismissDebugAdd
        )
    }

    // 写真の詳細シート
    uiState.selectedPhoto?.let { photo ->
        PhotoDetailSheet(
            photo = photo,
            onDismiss = viewModel::onDismissDetail,
            onDeleteClick = { viewModel.onDeleteClicked(photo) }
        )
    }

    // 削除確認ダイアログ
    uiState.photoPendingDelete?.let { photo ->
        AlertDialog(
            onDismissRequest = viewModel::onDismissDeleteDialog,
            title = { Text("写真を削除しますか？") },
            text = { Text("${photo.date.toJapaneseString()}の写真を削除します。この操作は取り消せません。") },
            confirmButton = {
                TextButton(onClick = viewModel::onConfirmDelete) {
                    Text(
                        text = "削除",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onDismissDeleteDialog) {
                    Text("キャンセル")
                }
            }
        )
    }

    // 削除・動画作成などの失敗時のエラーダイアログ
    uiState.errorMessage?.let { message ->
        AlertDialog(
            onDismissRequest = viewModel::onErrorDismissed,
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = viewModel::onErrorDismissed) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
private fun MonthHeader(
    month: LocalDate,
    canGoNextMonth: Boolean,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPreviousMonth) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "前の月"
            )
        }

        Text(
            text = month.toJapaneseYearMonthString(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )

        IconButton(onClick = onNextMonth, enabled = canGoNextMonth) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "次の月"
            )
        }
    }
}

@Composable
private fun DayOfWeekRow() {
    Row(modifier = Modifier.fillMaxWidth()) {
        DayOfWeekLabels.forEachIndexed { index, label ->
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = when (index) {
                    0 -> MaterialTheme.colorScheme.error
                    6 -> SaturdayBlue
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                },
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 8.dp)
            )
        }
    }
}

@Composable
private fun CalendarGrid(
    cells: List<LocalDate?>,
    today: LocalDate,
    photosByDate: Map<LocalDate, DailyPhoto>,
    onDayClick: (DailyPhoto) -> Unit,
    onEmptyDayClick: ((LocalDate) -> Unit)?
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        cells.chunked(7).forEach { week ->
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                week.forEach { date ->
                    val cellModifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f)

                    if (date == null) {
                        Spacer(modifier = cellModifier)
                    } else {
                        DayCell(
                            date = date,
                            photo = photosByDate[date],
                            isToday = date == today,
                            onClick = onDayClick,
                            onEmptyClick = onEmptyDayClick,
                            modifier = cellModifier
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    date: LocalDate,
    photo: DailyPhoto?,
    isToday: Boolean,
    onClick: (DailyPhoto) -> Unit,
    onEmptyClick: ((LocalDate) -> Unit)?,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(8.dp)
    val dayColor = when (date.dayOfWeek) {
        DayOfWeek.SUNDAY -> MaterialTheme.colorScheme.error
        DayOfWeek.SATURDAY -> SaturdayBlue
        else -> MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = modifier
            .clip(shape)
            .then(
                if (isToday) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, shape)
                else Modifier
            )
            .then(
                when {
                    photo != null -> Modifier.clickable { onClick(photo) }
                    onEmptyClick != null -> Modifier.clickable { onEmptyClick(date) }
                    else -> Modifier
                }
            )
    ) {
        if (photo != null) {
            LocalImage(
                path = photo.imagePath,
                contentDescription = "${date.toJapaneseString()}の写真",
                maxSize = 128,
                modifier = Modifier.fillMaxSize()
            )

            // 写真の上でも読めるよう半透明の背景を敷く
            Text(
                text = date.dayOfMonth.toString(),
                style = MaterialTheme.typography.labelSmall,
                color = dayColor,
                modifier = Modifier
                    .padding(3.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f),
                        shape = RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 4.dp, vertical = 1.dp)
            )
        } else {
            Text(
                text = date.dayOfMonth.toString(),
                style = MaterialTheme.typography.bodyMedium,
                color = dayColor,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PhotoDetailSheet(
    photo: DailyPhoto,
    onDismiss: () -> Unit,
    onDeleteClick: () -> Unit
) {
    // 半分の高さで開くと削除ボタンが隠れるため、最初から全体を表示する
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(start = 24.dp, end = 24.dp, bottom = 24.dp)
        ) {
            LocalImage(
                path = photo.imagePath,
                contentDescription = "${photo.date.toJapaneseString()}の写真",
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(16.dp))
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = photo.date.toJapaneseString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            if (photo.comment.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = photo.comment,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onDeleteClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("削除")
            }
        }
    }
}

@Composable
private fun VideoPlayerDialog(
    path: String,
    onDismiss: () -> Unit
) {
    val shareVideo = rememberVideoSharer()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        FullScreenDialogEffect()

        // 動画は画面いっぱいに表示し、操作ボタンは下端に重ねる
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            VideoPlayer(
                path = path,
                modifier = Modifier.fillMaxSize()
            )

            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .safeDrawingPadding()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.Black.copy(alpha = 0.5f),
                        contentColor = Color.White
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("閉じる")
                }

                Button(
                    onClick = { shareVideo(path) },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("共有")
                }
            }
        }
    }
}

/**
 * デバッグ用: 表示中の月の動画を手動で作成するボタンと、写真追加の案内
 */
@Composable
private fun DebugPanel(
    month: LocalDate,
    isCreatingVideo: Boolean,
    onCreateVideoClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Text(
            text = "デバッグ",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.error
        )

        Text(
            text = "空いている日をタップすると写真を追加できます",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = onCreateVideoClick,
            enabled = !isCreatingVideo,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isCreatingVideo) {
                CircularProgressIndicator(
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("動画を作成中…")
            } else {
                Text("${month.monthNumber}月の動画を作成")
            }
        }
    }
}

/**
 * デバッグ用: 任意の日付に写真を追加するシート
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DebugAddPhotoSheet(
    state: DebugAddPhotoState,
    onImagePicked: (ByteArray) -> Unit,
    onCommentChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()

    // 「今日の一枚」と同じく FileKit のギャラリーピッカーを使う
    val launcher = rememberFilePickerLauncher(
        type = PickerType.Image,
        mode = PickerMode.Single
    ) { file ->
        file?.let { platformFile ->
            scope.launch { onImagePicked(platformFile.readBytes()) }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(start = 24.dp, end = 24.dp, bottom = 24.dp)
        ) {
            Text(
                text = "${state.date.toJapaneseString()}に写真を追加",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(16.dp))

            state.imagePath?.let { path ->
                LocalImage(
                    path = path,
                    contentDescription = "選択した写真",
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(16.dp))
                )

                Spacer(modifier = Modifier.height(12.dp))
            }

            OutlinedButton(
                onClick = { launcher.launch() },
                enabled = !state.isSaving,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (state.imagePath == null) "画像を選択" else "画像を選び直す")
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = state.comment,
                onValueChange = onCommentChange,
                label = { Text("一言コメント") },
                enabled = !state.isSaving,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onSaveClick,
                enabled = state.canSave,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("登録")
            }
        }
    }
}
