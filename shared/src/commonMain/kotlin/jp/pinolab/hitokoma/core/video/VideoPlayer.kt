package jp.pinolab.hitokoma.core.video

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * ローカルの動画ファイルをループ再生する
 */
@Composable
expect fun VideoPlayer(path: String, modifier: Modifier = Modifier)

/**
 * 動画ファイルを他のアプリへ共有する関数を返す
 */
@Composable
expect fun rememberVideoSharer(): (path: String) -> Unit

/**
 * 呼び出し元の Dialog のウィンドウを画面いっぱいに広げる（全画面プレイヤー用）
 */
@Composable
expect fun FullScreenDialogEffect()
