package jp.pinolab.hitokoma.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_photos")
data class PhotoEntity(
    @PrimaryKey
    val dateString: String,           // ISO-8601形式 ("2026-08-01") を主キーにし、1日1件をDBレベルで担保
    val imagePath: String,
    val comment: String,
    val createdAtEpochMillis: Long
)