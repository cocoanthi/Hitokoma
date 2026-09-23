package jp.pinolab.hitokoma.feature.gallery.domain

import jp.pinolab.hitokoma.core.file.LocalImageStorage
import jp.pinolab.hitokoma.domain.model.DailyPhoto
import jp.pinolab.hitokoma.domain.repository.PhotoRepository

class DeleteDailyPhotoUseCase(
    private val photoRepository: PhotoRepository,
    private val imageStorage: LocalImageStorage
) {
    /**
     * 登録済みの写真を削除する（DB の行と保存済みの画像ファイルの両方）
     * @return 成功時は Result.success(Unit)
     */
    suspend operator fun invoke(photo: DailyPhoto): Result<Unit> = runCatching {
        // 1. 先に DB の行を消す（ファイル削除に失敗しても一覧に壊れたカードが残らないようにする）
        photoRepository.deletePhoto(photo.date)

        // 2. 保存済みの画像ファイルを消す
        imageStorage.deleteImage(photo.imagePath)
    }
}
