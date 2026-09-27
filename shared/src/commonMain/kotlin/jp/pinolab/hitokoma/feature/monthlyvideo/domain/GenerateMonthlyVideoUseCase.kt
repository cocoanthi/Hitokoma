package jp.pinolab.hitokoma.feature.monthlyvideo.domain

import jp.pinolab.hitokoma.core.time.previousMonth
import jp.pinolab.hitokoma.core.time.toJapaneseString
import jp.pinolab.hitokoma.core.time.toJapaneseYearMonthString
import jp.pinolab.hitokoma.core.video.VideoEncoder
import jp.pinolab.hitokoma.core.video.VideoSlide
import jp.pinolab.hitokoma.domain.repository.MonthlyVideoRepository
import jp.pinolab.hitokoma.domain.repository.PhotoRepository
import kotlinx.datetime.LocalDate

/**
 * 生成した動画（month はその月の1日）
 */
data class GeneratedVideo(val month: LocalDate, val path: String)

/**
 * 先月の写真をまとめたストーリー動画を生成する。
 * 生成済み、または先月の写真が1枚もない場合は何もせず null を返す
 */
class GenerateMonthlyVideoUseCase(
    private val photoRepository: PhotoRepository,
    private val videoEncoder: VideoEncoder,
    private val monthlyVideoRepository: MonthlyVideoRepository
) {
    suspend operator fun invoke(today: LocalDate): Result<GeneratedVideo?> = runCatching {
        val month = today.previousMonth()
        if (monthlyVideoRepository.hasVideo(month)) return@runCatching null

        val photos = photoRepository.getPhotosInMonth(month)
        if (photos.isEmpty()) return@runCatching null

        val outputPath = monthlyVideoRepository.outputPath(month)
        videoEncoder.encode(
            title = month.toJapaneseYearMonthString(),
            slides = photos.map { photo ->
                VideoSlide(
                    imagePath = photo.imagePath,
                    dateLabel = photo.date.toJapaneseString(),
                    comment = photo.comment
                )
            },
            outputPath = outputPath
        )
        monthlyVideoRepository.onVideoGenerated(month)

        GeneratedVideo(month = month, path = outputPath)
    }
}
