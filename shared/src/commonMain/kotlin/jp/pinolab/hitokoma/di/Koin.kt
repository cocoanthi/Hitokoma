package jp.pinolab.hitokoma.di

import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

/**
 * 各プラットフォームの起動時に一度だけ呼び出す
 */
fun initKoin(appDeclaration: KoinAppDeclaration = {}) {
    startKoin {
        appDeclaration()
        modules(sharedModules)
    }
}
