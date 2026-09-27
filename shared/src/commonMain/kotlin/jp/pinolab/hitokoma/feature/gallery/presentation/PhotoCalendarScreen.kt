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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import jp.pinolab.hitokoma.core.image.LocalImage
import jp.pinolab.hitokoma.core.time.toJapaneseString
import jp.pinolab.hitokoma.core.time.toJapaneseYearMonthString
import jp.pinolab.hitokoma.domain.model.DailyPhoto
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate

private val SaturdayBlue = Color(0xFF1E6FD9)
private val DayOfWeekLabels = listOf("日", "月", "火", "水", "木", "金", "土")

@Composable
fun PhotoCalendarScreen(
    viewModel: PhotoCalendarViewModel,
    modifier: Modifier = Modifier
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

                DayOfWeekRow()

                Spacer(modifier = Modifier.height(4.dp))

                CalendarGrid(
                    cells = uiState.cells,
                    today = uiState.today,
                    photosByDate = uiState.photosByDate,
                    onDayClick = viewModel::onDayClicked
                )
            }
        }
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

    // 削除失敗時のエラーダイアログ
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
    onDayClick: (DailyPhoto) -> Unit
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
                if (photo != null) Modifier.clickable { onClick(photo) } else Modifier
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
