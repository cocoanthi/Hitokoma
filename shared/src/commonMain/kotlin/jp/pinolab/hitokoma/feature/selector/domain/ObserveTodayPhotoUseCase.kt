package jp.pinolab.hitokoma.feature.selector.domain

import jp.pinolab.hitokoma.core.time.todayFlow
import jp.pinolab.hitokoma.domain.model.DailyPhoto
import jp.pinolab.hitokoma.domain.repository.PhotoRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate

class ObserveTodayPhotoUseCase(
    private val photoRepository: PhotoRepository
) {
    /**
     * 今日の日付と、その日に登録済みの写真（未登録なら null）を監視する。
     * 日付が変わると新しい日付の監視に切り替わる
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<Pair<LocalDate, DailyPhoto?>> =
        todayFlow().flatMapLatest { date ->
            photoRepository.observePhotoByDate(date).map { photo -> date to photo }
        }
}
