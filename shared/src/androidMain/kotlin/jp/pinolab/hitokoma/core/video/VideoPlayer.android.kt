package jp.pinolab.hitokoma.core.video

import android.content.Intent
import android.os.Build
import android.view.WindowManager
import android.widget.VideoView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.content.FileProvider
import androidx.core.view.WindowCompat
import java.io.File

@Composable
actual fun VideoPlayer(path: String, modifier: Modifier) {
    AndroidView(
        factory = { context ->
            VideoView(context).apply {
                setVideoPath(path)
                setOnPreparedListener { player ->
                    player.isLooping = true
                    start()
                }
            }
        },
        modifier = modifier,
        onRelease = { it.stopPlayback() }
    )
}

@Composable
actual fun rememberVideoSharer(): (path: String) -> Unit {
    val context = LocalContext.current
    return remember(context) {
        { path ->
            // アプリ内ストレージのファイルは FileProvider 経由で他アプリへ渡す
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", File(path))
            val send = Intent(Intent.ACTION_SEND)
                .setType("video/mp4")
                .putExtra(Intent.EXTRA_STREAM, uri)
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            context.startActivity(Intent.createChooser(send, null))
        }
    }
}

@Composable
actual fun FullScreenDialogEffect() {
    // Dialog のウィンドウは画面の高さで作られるが、既定ではステータスバーの分だけ下にずらして配置されるため、
    // 下端（操作ボタン）が画面外に切れる。システムバーの裏まで広げ、余白は内容側の safeDrawingPadding に任せる
    val view = LocalView.current
    SideEffect {
        val window = (view.parent as? DialogWindowProvider)?.window ?: return@SideEffect
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.attributes = window.attributes.apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                fitInsetsTypes = 0
            }
            // 既定ではカメラの切り欠き（ステータスバー）の領域を避けて配置されるため、その領域も使う
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        }
    }
}
