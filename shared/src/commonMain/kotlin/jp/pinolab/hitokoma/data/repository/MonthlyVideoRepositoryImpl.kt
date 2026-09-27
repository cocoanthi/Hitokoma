package jp.pinolab.hitokoma.data.repository

import jp.pinolab.hitokoma.core.time.firstOfMonth
import jp.pinolab.hitokoma.core.video.VideoStorage
import jp.pinolab.hitokoma.domain.repository.MonthlyVideoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.datetime.LocalDate

/**
 * 動画の有無はファイルで判定し、生成済みの月をメモリ上でも保持する
 * （Worker もアプリと同じプロセスで動くため、生成完了が開いている画面にそのまま反映される）
 */
class MonthlyVideoRepositoryImpl(
    private val videoStorage: VideoStorage
) : MonthlyVideoRepository {

    private val generatedMonths = MutableStateFlow(videoStorage.existingMonths())

    override fun observeVideoPath(month: LocalDate): Flow<String?> {
        val target = month.firstOfMonth()
        return generatedMonths
            .map { months -> if (target in months) videoStorage.videoPath(target) else null }
            .distinctUntilChanged()
    }

    override fun hasVideo(month: LocalDate): Boolean = month.firstOfMonth() in generatedMonths.value

    override fun outputPath(month: LocalDate): String = videoStorage.videoPath(month.firstOfMonth())

    override fun onVideoGenerated(month: LocalDate) {
        generatedMonths.update { it + month.firstOfMonth() }
    }
}
