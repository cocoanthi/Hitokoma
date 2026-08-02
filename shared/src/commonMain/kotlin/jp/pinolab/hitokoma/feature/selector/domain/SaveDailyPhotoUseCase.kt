package jp.pinolab.hitokoma.feature.selector.domain

import jp.pinolab.hitokoma.domain.model.DailyPhoto
import jp.pinolab.hitokoma.domain.repository.PhotoRepository

class SaveDailyPhotoUseCase(
    private val photoRepository: PhotoRepository
) {
    /**
     * @param photo 保存対象の写真
     * @param allowOverwrite 上書きを許可するか（デフォルトは false: 1日1枚制約を適用）
     * @return 成功時は Result.success(Unit)、既に存在する場合は Result.failure(PhotoAlreadyExistsException)
     */
    suspend operator fun invoke(
        photo: DailyPhoto,
        allowOverwrite: Boolean = false
    ): Result<Unit> = runCatching {
        // 1. 指定された日付に既存の写真があるかチェック
        val existingPhoto = photoRepository.getPhotoByDate(photo.date)

        // 2. すでに存在し、かつ上書きが許可されていない場合は例外をスロー
        if (existingPhoto != null && !allowOverwrite) {
            throw PhotoAlreadyExistsException(photo.date)
        }

        // 3. 問題なければリポジトリ経由で保存
        photoRepository.savePhoto(photo)
    }
}