package jp.pinolab.hitokoma.data.repository

import jp.pinolab.hitokoma.data.local.db.PhotoDao
import jp.pinolab.hitokoma.data.mapper.toDomain
import jp.pinolab.hitokoma.data.mapper.toEntity
import jp.pinolab.hitokoma.domain.model.DailyPhoto
import jp.pinolab.hitokoma.domain.repository.PhotoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate

class PhotoRepositoryImpl(
    private val photoDao: PhotoDao
) : PhotoRepository {

    override suspend fun savePhoto(photo: DailyPhoto) {
        photoDao.upsertPhoto(photo.toEntity())
    }

    override suspend fun getPhotoByDate(date: LocalDate): DailyPhoto? {
        return photoDao.getPhotoByDate(date.toString())?.toDomain()
    }

    override fun observePhotosForMonth(year: Int, month: Int): Flow<List<DailyPhoto>> {
        // "2026-08" 形式のプレフィックス文字列を作成
        val monthFormatted = month.toString().padStart(2, '0')
        val prefix = "$year-$monthFormatted"

        return photoDao.observePhotosByYearMonth(prefix).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun observePhotosOnThisDay(month: Int, day: Int): Flow<List<DailyPhoto>> {
        // "08-01" 形式のサフィックス文字列を作成
        val monthFormatted = month.toString().padStart(2, '0')
        val dayFormatted = day.toString().padStart(2, '0')
        val suffix = "$monthFormatted-$dayFormatted"

        return photoDao.observePhotosOnThisDay(suffix).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun deletePhoto(date: LocalDate) {
        photoDao.deletePhoto(date.toString())
    }
}