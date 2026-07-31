package jp.pinolab.hitokoma

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
import androidx.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    MaterialTheme {
        TodayPhotoScreen()
    }
}

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