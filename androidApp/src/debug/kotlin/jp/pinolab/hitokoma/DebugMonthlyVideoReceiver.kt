package jp.pinolab.hitokoma

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import jp.pinolab.hitokoma.feature.monthlyvideo.MonthlyVideoScheduler
import kotlinx.datetime.LocalDate

/**
 * デバッグビルド専用: 端末の日付を変えずに月のストーリー動画の生成を試す
 *
 * adb shell am broadcast -n jp.pinolab.hitokoma.android/jp.pinolab.hitokoma.DebugMonthlyVideoReceiver --es today 2026-10-01
 */
class DebugMonthlyVideoReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val today = intent.getStringExtra("today")?.let(LocalDate::parse)
        MonthlyVideoScheduler.runOnce(context, today)
    }
}
