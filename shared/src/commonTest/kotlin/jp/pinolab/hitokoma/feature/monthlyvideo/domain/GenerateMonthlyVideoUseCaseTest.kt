package jp.pinolab.hitokoma.feature.monthlyvideo.domain

import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class GenerateMonthlyVideoUseCaseTest {

    private fun useCase(
        photos: FakePhotoRepository,
        encoder: FakeVideoEncoder,
        videos: FakeMonthlyVideoRepository = FakeMonthlyVideoRepository()
    ) = GenerateMonthlyVideoUseCase(CreateMonthlyVideoUseCase(photos, encoder, videos))

    @Test
    fun 先月の動画を生成する() = runTest {
        val photos = FakePhotoRepository(listOf(photo(LocalDate(2026, 9, 24)), photo(LocalDate(2026, 10, 1))))
        val encoder = FakeVideoEncoder()

        val result = useCase(photos, encoder)(today = LocalDate(2026, 10, 1))

        assertEquals(LocalDate(2026, 9, 1), result.getOrThrow()?.month)
        assertEquals(1, encoder.encodedSlides?.size)
    }

    @Test
    fun `1月に実行すると前年12月が対象になる`() = runTest {
        val photos = FakePhotoRepository(listOf(photo(LocalDate(2025, 12, 31))))
        val encoder = FakeVideoEncoder()

        val result = useCase(photos, encoder)(today = LocalDate(2026, 1, 1))

        assertEquals(LocalDate(2025, 12, 1), result.getOrThrow()?.month)
        assertEquals("2025年12月", encoder.encodedTitle)
    }

    @Test
    fun 生成済みなら作り直さない() = runTest {
        val photos = FakePhotoRepository(listOf(photo(LocalDate(2026, 9, 24))))
        val encoder = FakeVideoEncoder()
        val videos = FakeMonthlyVideoRepository(existing = setOf(LocalDate(2026, 9, 1)))

        val result = useCase(photos, encoder, videos)(today = LocalDate(2026, 10, 15))

        assertNull(result.getOrThrow())
        assertEquals(0, encoder.encodeCount)
    }
}
