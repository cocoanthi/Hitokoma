package jp.pinolab.hitokoma.domain.repository

import jp.pinolab.hitokoma.domain.model.DailyPhoto
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

interface PhotoRepository {
    suspend fun savePhoto(photo: DailyPhoto)
    suspend fun getPhotoByDate(date: LocalDate): DailyPhoto?
    fun observePhotoByDate(date: LocalDate): Flow<DailyPhoto?> // 今日の一枚表示用
    fun observePhotosForMonth(year: Int, month: Int): Flow<List<DailyPhoto>>
    fun observePhotosOnThisDay(month: Int, day: Int): Flow<List<DailyPhoto>> // 1年前などの「同月同日」検索用
    fun observeAllPhotos(): Flow<List<DailyPhoto>> // 一覧表示用（新しい日付順）
    suspend fun deletePhoto(date: LocalDate)
}