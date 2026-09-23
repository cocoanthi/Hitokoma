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

    override fun observePhotoByDate(date: LocalDate): Flow<DailyPhoto?> {
        return photoDao.observePhotoByDate(date.toString()).map { it?.toDomain() }
    }

    override fun observeAllPhotos(): Flow<List<DailyPhoto>> {
        return photoDao.observeAllPhotos().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun deletePhoto(date: LocalDate) {
        photoDao.deletePhoto(date.toString())
    }
}