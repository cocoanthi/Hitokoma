package jp.pinolab.hitokoma

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import jp.pinolab.hitokoma.android.BuildConfig
import jp.pinolab.hitokoma.feature.monthlyvideo.MonthlyVideoNotifier
import kotlinx.datetime.LocalDate

class MainActivity : ComponentActivity() {

    // 通知から起動したときに再生するストーリー動画の月
    private var openVideoMonth by mutableStateOf<LocalDate?>(null)

    // 通知の許可は拒否されても動画の生成・再生はできるため、結果は使わない
    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // 画面回転などの再生成時は、同じ通知から再度開かないようにする
        if (savedInstanceState == null) {
            openVideoMonth = MonthlyVideoNotifier.videoMonthFrom(intent)
        }
        requestNotificationPermissionIfNeeded()

        setContent {
            App(
                openVideoMonth = openVideoMonth,
                onVideoMonthOpened = { openVideoMonth = null },
                debugMode = BuildConfig.DEBUG
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        MonthlyVideoNotifier.videoMonthFrom(intent)?.let { openVideoMonth = it }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) return
        requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
