package jp.pinolab.hitokoma.feature.monthlyvideo.domain

import jp.pinolab.hitokoma.core.time.firstOfMonth
import jp.pinolab.hitokoma.core.video.VideoEncoder
import jp.pinolab.hitokoma.core.video.VideoSlide
import jp.pinolab.hitokoma.domain.model.DailyPhoto
import jp.pinolab.hitokoma.domain.repository.MonthlyVideoRepository
import jp.pinolab.hitokoma.domain.repository.PhotoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.LocalDate

internal class FakePhotoRepository(private val photos: List<DailyPhoto>) : PhotoRepository {
    override suspend fun savePhoto(photo: DailyPhoto) = Unit
    override suspend fun getPhotoByDate(date: LocalDate): DailyPhoto? = photos.find { it.date == date }
    override fun observePhotoByDate(date: LocalDate): Flow<DailyPhoto?> = flowOf(null)
    override fun observeAllPhotos(): Flow<List<DailyPhoto>> = flowOf(photos)
    override suspend fun getPhotosInMonth(firstOfMonth: LocalDate): List<DailyPhoto> =
        photos.filter { it.date.firstOfMonth() == firstOfMonth }.sortedBy { it.date }
    override suspend fun deletePhoto(date: LocalDate) = Unit
}

internal class FakeVideoEncoder(private val error: Throwable? = null) : VideoEncoder {
    var encodeCount = 0
    var encodedTitle: String? = null
    var encodedSlides: List<VideoSlide>? = null
    var encodedPath: String? = null

    override suspend fun encode(title: String, slides: List<VideoSlide>, outputPath: String) {
        error?.let { throw it }
        encodeCount++
        encodedTitle = title
        encodedSlides = slides
        encodedPath = outputPath
    }
}

internal class FakeMonthlyVideoRepository(existing: Set<LocalDate> = emptySet()) : MonthlyVideoRepository {
    val months = existing.toMutableSet()
    override fun observeVideoPath(month: LocalDate): Flow<String?> = flowOf(null)
    override fun hasVideo(month: LocalDate): Boolean = month in months
    override fun outputPath(month: LocalDate): String = "/videos/$month.mp4"
    override fun onVideoGenerated(month: LocalDate) {
        months += month
    }
}

internal fun photo(date: LocalDate, comment: String = "") = DailyPhoto(
    date = date,
    imagePath = "/photos/$date.jpg",
    comment = comment,
    createdAtEpochMillis = 0L
)
