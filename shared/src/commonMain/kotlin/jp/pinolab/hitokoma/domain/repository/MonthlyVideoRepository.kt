package jp.pinolab.hitokoma.domain.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

interface MonthlyVideoRepository {
    fun observeVideoPath(month: LocalDate): Flow<String?> // 動画がなければ null
    fun hasVideo(month: LocalDate): Boolean
    fun outputPath(month: LocalDate): String              // 生成した動画の書き出し先
    fun onVideoGenerated(month: LocalDate)                // 生成完了を監視中の画面へ反映する
}
