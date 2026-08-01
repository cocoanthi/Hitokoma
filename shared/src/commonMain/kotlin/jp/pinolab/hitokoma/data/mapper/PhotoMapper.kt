package jp.pinolab.hitokoma.data.mapper

import jp.pinolab.hitokoma.data.local.db.PhotoEntity
import jp.pinolab.hitokoma.domain.model.DailyPhoto
import kotlinx.datetime.LocalDate

fun PhotoEntity.toDomain(): DailyPhoto {
    return DailyPhoto(
        date = LocalDate.parse(this.dateString),
        imagePath = this.imagePath,
        comment = this.comment,
        createdAtEpochMillis = this.createdAtEpochMillis
    )
}

fun DailyPhoto.toEntity(): PhotoEntity {
    return PhotoEntity(
        dateString = this.date.toString(), // YYYY-MM-DD
        imagePath = this.imagePath,
        comment = this.comment,
        createdAtEpochMillis = this.createdAtEpochMillis
    )
}