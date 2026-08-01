package jp.pinolab.hitokoma.domain.repository

import jp.pinolab.hitokoma.domain.model.DailyPhoto
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

interface PhotoRepository {
    suspend fun savePhoto(photo: DailyPhoto)
    suspend fun getPhotoByDate(date: LocalDate): DailyPhoto?
    fun observePhotosForMonth(year: Int, month: Int): Flow<List<DailyPhoto>>
    fun observePhotosOnThisDay(month: Int, day: Int): Flow<List<DailyPhoto>> // 1年前などの「同月同日」検索用
    suspend fun deletePhoto(date: LocalDate)
}