package jp.pinolab.hitokoma.feature.monthlyvideo

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import jp.pinolab.hitokoma.feature.monthlyvideo.domain.GenerateMonthlyVideoUseCase
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * 先月のストーリー動画がまだなければ生成し、完成したら通知する（1日1回実行される）
 */
class MonthlyVideoWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params), KoinComponent {

    private val generateMonthlyVideoUseCase: GenerateMonthlyVideoUseCase by inject()

    override suspend fun doWork(): Result {
        // 検証用に「今日」を上書きできる（未指定なら端末の今日）
        val today = inputData.getString(KEY_TODAY)?.let(LocalDate::parse)
            ?: Clock.System.todayIn(TimeZone.currentSystemDefault())

        return generateMonthlyVideoUseCase(today).fold(
            onSuccess = { video ->
                video?.let { MonthlyVideoNotifier.notify(applicationContext, it.month) }
                Result.success()
            },
            onFailure = {
                if (runAttemptCount < MAX_RETRY) Result.retry() else Result.failure()
            }
        )
    }

    companion object {
        const val KEY_TODAY = "today"
        private const val MAX_RETRY = 3
    }
}
