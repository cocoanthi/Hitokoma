package jp.pinolab.hitokoma.feature.monthlyvideo.domain

import jp.pinolab.hitokoma.core.time.previousMonth
import kotlinx.datetime.LocalDate

/**
 * 月初の自動生成用: 先月のストーリー動画がまだなければ生成する。
 * 生成済み、または先月の写真が1枚もない場合は何もせず null を返す
 */
class GenerateMonthlyVideoUseCase(
    private val createMonthlyVideoUseCase: CreateMonthlyVideoUseCase
) {
    suspend operator fun invoke(today: LocalDate): Result<GeneratedVideo?> =
        createMonthlyVideoUseCase(month = today.previousMonth(), overwrite = false)
}
