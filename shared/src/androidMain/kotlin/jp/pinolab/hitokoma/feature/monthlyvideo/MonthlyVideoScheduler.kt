package jp.pinolab.hitokoma.feature.monthlyvideo

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.datetime.LocalDate
import java.util.concurrent.TimeUnit

object MonthlyVideoScheduler {

    private const val PERIODIC_WORK_NAME = "monthly_video"
    private const val ONE_TIME_WORK_NAME = "monthly_video_once"

    /**
     * 1日1回の定期実行を登録する（登録済みならそのまま）。
     * 各実行で先月の動画がなければ生成するため、月初に端末が止まっていても翌日以降に追いつく
     */
    fun schedule(context: Context) {
        val request = PeriodicWorkRequestBuilder<MonthlyVideoWorker>(1, TimeUnit.DAYS).build()
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(PERIODIC_WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request)
    }

    /**
     * すぐに1回だけ実行する（検証用。today を渡すとその日付を「今日」として扱う）
     */
    fun runOnce(context: Context, today: LocalDate? = null) {
        val inputData = Data.Builder().apply {
            if (today != null) putString(MonthlyVideoWorker.KEY_TODAY, today.toString())
        }.build()
        val request = OneTimeWorkRequestBuilder<MonthlyVideoWorker>()
            .setInputData(inputData)
            .build()
        WorkManager.getInstance(context)
            .enqueueUniqueWork(ONE_TIME_WORK_NAME, ExistingWorkPolicy.REPLACE, request)
    }
}
