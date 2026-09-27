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

    // 今日の一枚表示用: 指定日の写真を監視（未登録なら null）
    @Query("SELECT * FROM daily_photos WHERE dateString = :dateString LIMIT 1")
    fun observePhotoByDate(dateString: String): Flow<PhotoEntity?>

    // 一覧表示用: 登録済みの写真を新しい日付順ですべて取得
    @Query("SELECT * FROM daily_photos ORDER BY dateString DESC")
    fun observeAllPhotos(): Flow<List<PhotoEntity>>

    // 月のストーリー動画用: 期間内の写真を古い日付順に取得（ISO 形式の日付文字列は辞書順で比較できる）
    @Query("SELECT * FROM daily_photos WHERE dateString BETWEEN :start AND :end ORDER BY dateString ASC")
    suspend fun getPhotosBetween(start: String, end: String): List<PhotoEntity>

    @Query("DELETE FROM daily_photos WHERE dateString = :dateString")
    suspend fun deletePhoto(dateString: String)
}