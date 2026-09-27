package jp.pinolab.hitokoma.di

import jp.pinolab.hitokoma.data.local.db.AppDatabase
import jp.pinolab.hitokoma.data.repository.MonthlyVideoRepositoryImpl
import jp.pinolab.hitokoma.data.repository.PhotoRepositoryImpl
import jp.pinolab.hitokoma.domain.repository.MonthlyVideoRepository
import jp.pinolab.hitokoma.domain.repository.PhotoRepository
import jp.pinolab.hitokoma.feature.gallery.domain.DeleteDailyPhotoUseCase
import jp.pinolab.hitokoma.feature.gallery.domain.ObserveAllPhotosUseCase
import jp.pinolab.hitokoma.feature.gallery.presentation.PhotoCalendarViewModel
import jp.pinolab.hitokoma.feature.monthlyvideo.domain.GenerateMonthlyVideoUseCase
import jp.pinolab.hitokoma.feature.selector.domain.ObserveTodayPhotoUseCase
import jp.pinolab.hitokoma.feature.selector.domain.SaveDailyPhotoUseCase
import jp.pinolab.hitokoma.feature.selector.presentation.PhotoSelectorViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

/**
 * プラットフォーム固有の依存（AppDatabase・LocalImageStorage・VideoEncoder・VideoStorage）を提供するモジュール
 */
expect val platformModule: Module

val dataModule = module {
    single { get<AppDatabase>().photoDao() }
    singleOf(::PhotoRepositoryImpl) bind PhotoRepository::class
    singleOf(::MonthlyVideoRepositoryImpl) bind MonthlyVideoRepository::class
}

val selectorModule = module {
    factoryOf(::SaveDailyPhotoUseCase)
    factoryOf(::ObserveTodayPhotoUseCase)
    viewModelOf(::PhotoSelectorViewModel)
}

val galleryModule = module {
    factoryOf(::ObserveAllPhotosUseCase)
    factoryOf(::DeleteDailyPhotoUseCase)
    viewModelOf(::PhotoCalendarViewModel)
}

val monthlyVideoModule = module {
    factoryOf(::GenerateMonthlyVideoUseCase)
}

val sharedModules: List<Module>
    get() = listOf(platformModule, dataModule, selectorModule, galleryModule, monthlyVideoModule)
