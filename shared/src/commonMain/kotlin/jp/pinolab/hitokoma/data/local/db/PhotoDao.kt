package jp.pinolab.hitokoma.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PhotoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPhoto(photo: PhotoEntity)

    @Query("SELECT * FROM daily_photos WHERE dateString = :dateString LIMIT 1")
    suspend fun getPhotoByDate(dateString: String): PhotoEntity?

    // カレンダー表示用: 指定年月（例: "2026-08%"）の写真一覧を取得
    @Query("SELECT * FROM daily_photos WHERE dateString LIKE :yearMonthPrefix || '%' ORDER BY dateString ASC")
    fun observePhotosByYearMonth(yearMonthPrefix: String): Flow<List<PhotoEntity>>

    // 1年前の今日用: 指定した月日（例: "%-08-01"）の過去の写真一覧を取得
    @Query("SELECT * FROM daily_photos WHERE dateString LIKE '%-' || :monthDaySuffix ORDER BY dateString DESC")
    fun observePhotosOnThisDay(monthDaySuffix: String): Flow<List<PhotoEntity>>

    @Query("DELETE FROM daily_photos WHERE dateString = :dateString")
    suspend fun deletePhoto(dateString: String)
}