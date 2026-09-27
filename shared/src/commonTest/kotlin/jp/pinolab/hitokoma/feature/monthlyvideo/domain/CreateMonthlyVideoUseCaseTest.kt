package jp.pinolab.hitokoma.feature.monthlyvideo.domain

import jp.pinolab.hitokoma.core.video.VideoSlide
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CreateMonthlyVideoUseCaseTest {

    @Test
    fun 指定した月の写真を古い日付順にタイトル付きで書き出す() = runTest {
        val photos = FakePhotoRepository(
            listOf(
                photo(LocalDate(2026, 9, 27), "夏の終わり"),
                photo(LocalDate(2026, 9, 24)),
                photo(LocalDate(2026, 10, 1)), // 翌月分は含めない
                photo(LocalDate(2026, 8, 31))  // 前月分も含めない
            )
        )
        val encoder = FakeVideoEncoder()
        val videos = FakeMonthlyVideoRepository()

        // 月の途中の日付を渡しても、その月が対象になる
        val result = CreateMonthlyVideoUseCase(photos, encoder, videos)(LocalDate(2026, 9, 15), overwrite = false)

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
    fun 生成済みでも上書き指定なら作り直す() = runTest {
        val photos = FakePhotoRepository(listOf(photo(LocalDate(2026, 9, 24))))
        val encoder = FakeVideoEncoder()
        val videos = FakeMonthlyVideoRepository(existing = setOf(LocalDate(2026, 9, 1)))

        val result = CreateMonthlyVideoUseCase(photos, encoder, videos)(LocalDate(2026, 9, 1), overwrite = true)

        assertEquals(LocalDate(2026, 9, 1), result.getOrThrow()?.month)
        assertEquals(1, encoder.encodeCount)
    }

    @Test
    fun 生成済みで上書き指定がなければ何もしない() = runTest {
        val photos = FakePhotoRepository(listOf(photo(LocalDate(2026, 9, 24))))
        val encoder = FakeVideoEncoder()
        val videos = FakeMonthlyVideoRepository(existing = setOf(LocalDate(2026, 9, 1)))

        val result = CreateMonthlyVideoUseCase(photos, encoder, videos)(LocalDate(2026, 9, 1), overwrite = false)

        assertNull(result.getOrThrow())
        assertEquals(0, encoder.encodeCount)
    }

    @Test
    fun その月の写真がなければ生成しない() = runTest {
        val photos = FakePhotoRepository(listOf(photo(LocalDate(2026, 9, 24))))
        val encoder = FakeVideoEncoder()

        val result = CreateMonthlyVideoUseCase(photos, encoder, FakeMonthlyVideoRepository())(LocalDate(2026, 7, 1), overwrite = true)

        assertNull(result.getOrThrow())
        assertEquals(0, encoder.encodeCount)
    }

    @Test
    fun 書き出しに失敗したら失敗を返し生成済みにしない() = runTest {
        val photos = FakePhotoRepository(listOf(photo(LocalDate(2026, 9, 24))))
        val encoder = FakeVideoEncoder(error = IllegalStateException("encoder error"))
        val videos = FakeMonthlyVideoRepository()

        val result = CreateMonthlyVideoUseCase(photos, encoder, videos)(LocalDate(2026, 9, 1), overwrite = true)

        assertTrue(result.isFailure)
        assertFalse(videos.hasVideo(LocalDate(2026, 9, 1)))
    }
}
