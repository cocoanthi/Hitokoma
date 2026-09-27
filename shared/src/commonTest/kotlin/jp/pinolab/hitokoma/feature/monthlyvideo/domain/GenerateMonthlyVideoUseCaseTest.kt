package jp.pinolab.hitokoma.feature.monthlyvideo.domain

import jp.pinolab.hitokoma.core.time.firstOfMonth
import jp.pinolab.hitokoma.core.video.VideoEncoder
import jp.pinolab.hitokoma.core.video.VideoSlide
import jp.pinolab.hitokoma.domain.model.DailyPhoto
import jp.pinolab.hitokoma.domain.repository.MonthlyVideoRepository
import jp.pinolab.hitokoma.domain.repository.PhotoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GenerateMonthlyVideoUseCaseTest {

    private class FakePhotoRepository(private val photos: List<DailyPhoto>) : PhotoRepository {
        override suspend fun savePhoto(photo: DailyPhoto) = Unit
        override suspend fun getPhotoByDate(date: LocalDate): DailyPhoto? = photos.find { it.date == date }
        override fun observePhotoByDate(date: LocalDate): Flow<DailyPhoto?> = flowOf(null)
        override fun observeAllPhotos(): Flow<List<DailyPhoto>> = flowOf(photos)
        override suspend fun getPhotosInMonth(firstOfMonth: LocalDate): List<DailyPhoto> =
            photos.filter { it.date.firstOfMonth() == firstOfMonth }.sortedBy { it.date }
        override suspend fun deletePhoto(date: LocalDate) = Unit
    }

    private class FakeVideoEncoder(private val error: Throwable? = null) : VideoEncoder {
        var encodedTitle: String? = null
        var encodedSlides: List<VideoSlide>? = null
        var encodedPath: String? = null

        override suspend fun encode(title: String, slides: List<VideoSlide>, outputPath: String) {
            error?.let { throw it }
            encodedTitle = title
            encodedSlides = slides
            encodedPath = outputPath
        }
    }

    private class FakeMonthlyVideoRepository(existing: Set<LocalDate> = emptySet()) : MonthlyVideoRepository {
        val months = existing.toMutableSet()
        override fun observeVideoPath(month: LocalDate): Flow<String?> = flowOf(null)
        override fun hasVideo(month: LocalDate): Boolean = month in months
        override fun outputPath(month: LocalDate): String = "/videos/$month.mp4"
        override fun onVideoGenerated(month: LocalDate) {
            months += month
        }
    }

    private fun photo(date: LocalDate, comment: String = "") = DailyPhoto(
        date = date,
        imagePath = "/photos/$date.jpg",
        comment = comment,
        createdAtEpochMillis = 0L
    )

    @Test
    fun 先月の写真を古い日付順にタイトル付きで書き出す() = runTest {
        val photos = FakePhotoRepository(
            listOf(
                photo(LocalDate(2026, 9, 27), "夏の終わり"),
                photo(LocalDate(2026, 9, 24)),
                photo(LocalDate(2026, 10, 1)),  // 今月分は含めない
                photo(LocalDate(2026, 8, 31))   // 先々月分も含めない
            )
        )
        val encoder = FakeVideoEncoder()
        val videos = FakeMonthlyVideoRepository()

        val result = GenerateMonthlyVideoUseCase(photos, encoder, videos)(today = LocalDate(2026, 10, 1))

        assertEquals(GeneratedVideo(LocalDate(2026, 9, 1), "/videos/2026-09-01.mp4"), result.getOrThrow())
        assertEquals("2026年9月", encoder.encodedTitle)
        assertEquals(
            listOf(
                VideoSlide("/photos/2026-09-24.jpg", "2026年9月24日", ""),
                VideoSlide("/photos/2026-09-27.jpg", "2026年9月27日", "夏の終わり")
            ),
            encoder.encodedSlides
        )
        assertEquals("/videos/2026-09-01.mp4", encoder.encodedPath)
        assertTrue(videos.hasVideo(LocalDate(2026, 9, 1)))
    }

    @Test
    fun `1月に実行すると前年12月が対象になる`() = runTest {
        val photos = FakePhotoRepository(listOf(photo(LocalDate(2025, 12, 31))))
        val encoder = FakeVideoEncoder()

        val result = GenerateMonthlyVideoUseCase(photos, encoder, FakeMonthlyVideoRepository())(today = LocalDate(2026, 1, 1))

        assertEquals(LocalDate(2025, 12, 1), result.getOrThrow()?.month)
        assertEquals("2025年12月", encoder.encodedTitle)
    }

    @Test
    fun 先月の写真がなければ生成しない() = runTest {
        val photos = FakePhotoRepository(listOf(photo(LocalDate(2026, 10, 1))))
        val encoder = FakeVideoEncoder()

        val result = GenerateMonthlyVideoUseCase(photos, encoder, FakeMonthlyVideoRepository())(today = LocalDate(2026, 10, 1))

        assertNull(result.getOrThrow())
        assertNull(encoder.encodedTitle)
    }

    @Test
    fun 生成済みなら生成しない() = runTest {
        val photos = FakePhotoRepository(listOf(photo(LocalDate(2026, 9, 24))))
        val encoder = FakeVideoEncoder()
        val videos = FakeMonthlyVideoRepository(existing = setOf(LocalDate(2026, 9, 1)))

        val result = GenerateMonthlyVideoUseCase(photos, encoder, videos)(today = LocalDate(2026, 10, 15))

        assertNull(result.getOrThrow())
        assertNull(encoder.encodedTitle)
    }

    @Test
    fun 書き出しに失敗したら失敗を返し生成済みにしない() = runTest {
        val photos = FakePhotoRepository(listOf(photo(LocalDate(2026, 9, 24))))
        val encoder = FakeVideoEncoder(error = IllegalStateException("encoder error"))
        val videos = FakeMonthlyVideoRepository()

        val result = GenerateMonthlyVideoUseCase(photos, encoder, videos)(today = LocalDate(2026, 10, 1))

        assertTrue(result.isFailure)
        assertTrue(!videos.hasVideo(LocalDate(2026, 9, 1)))
    }
}
