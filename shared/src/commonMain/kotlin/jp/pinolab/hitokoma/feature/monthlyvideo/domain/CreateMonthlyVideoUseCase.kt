package jp.pinolab.hitokoma.feature.monthlyvideo.domain

import jp.pinolab.hitokoma.core.time.firstOfMonth
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
 * 指定した月の写真をまとめたストーリー動画を生成する。
 * その月の写真が1枚もない場合、または生成済みで overwrite = false の場合は何もせず null を返す
 */
class CreateMonthlyVideoUseCase(
    private val photoRepository: PhotoRepository,
    private val videoEncoder: VideoEncoder,
    private val monthlyVideoRepository: MonthlyVideoRepository
) {
    suspend operator fun invoke(month: LocalDate, overwrite: Boolean): Result<GeneratedVideo?> = runCatching {
        val target = month.firstOfMonth()
        if (!overwrite && monthlyVideoRepository.hasVideo(target)) return@runCatching null

        val photos = photoRepository.getPhotosInMonth(target)
        if (photos.isEmpty()) return@runCatching null

        val outputPath = monthlyVideoRepository.outputPath(target)
        videoEncoder.encode(
            title = target.toJapaneseYearMonthString(),
            slides = photos.map { photo ->
                VideoSlide(
                    imagePath = photo.imagePath,
                    dateLabel = photo.date.toJapaneseString(),
                    comment = photo.comment
                )
            },
            outputPath = outputPath
        )
        monthlyVideoRepository.onVideoGenerated(target)

        GeneratedVideo(month = target, path = outputPath)
    }
}
