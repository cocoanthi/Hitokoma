package jp.pinolab.hitokoma.feature.monthlyvideo

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import jp.pinolab.hitokoma.R
import kotlinx.datetime.LocalDate

object MonthlyVideoNotifier {

    /**
     * 通知から起動したときに、再生する動画の月（"2026-09-01"）を渡す Intent extra
     */
    const val EXTRA_VIDEO_MONTH = "jp.pinolab.hitokoma.extra.VIDEO_MONTH"

    private const val CHANNEL_ID = "monthly_video"

    /**
     * 通知から起動した Intent なら、再生する動画の月を返す
     */
    fun videoMonthFrom(intent: Intent?): LocalDate? =
        intent?.getStringExtra(EXTRA_VIDEO_MONTH)?.let(LocalDate::parse)

    /**
     * 「9月のストーリーができました」を通知する（通知が許可されていなければ何もしない）
     */
    @SuppressLint("MissingPermission") // 直前で許可を確認している
    fun notify(context: Context, month: LocalDate) {
        if (!canNotify(context)) return
        createChannel(context)

        val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
            ?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            ?.putExtra(EXTRA_VIDEO_MONTH, month.toString())
            ?: return
        val notificationId = month.year * 100 + month.monthNumber // 月ごとに別の通知にする
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_story)
            .setContentTitle("${month.monthNumber}月のストーリーができました")
            .setContentText("先月の「今日の一枚」を動画でふりかえりましょう")
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }

    private fun canNotify(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return false
        }
        return NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    private fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(CHANNEL_ID, "月のストーリー", NotificationManager.IMPORTANCE_DEFAULT).apply {
            description = "毎月1日に先月の写真をまとめた動画ができたことをお知らせします"
        }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }
}
