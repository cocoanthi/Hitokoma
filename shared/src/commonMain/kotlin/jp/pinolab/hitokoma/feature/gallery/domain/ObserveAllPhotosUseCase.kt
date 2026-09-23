package jp.pinolab.hitokoma.feature.gallery.domain

import jp.pinolab.hitokoma.domain.model.DailyPhoto
import jp.pinolab.hitokoma.domain.repository.PhotoRepository
import kotlinx.coroutines.flow.Flow

class ObserveAllPhotosUseCase(
    private val photoRepository: PhotoRepository
) {
    /**
     * 登録済みの写真を新しい日付順で監視する
     */
    operator fun invoke(): Flow<List<DailyPhoto>> = photoRepository.observeAllPhotos()
}
