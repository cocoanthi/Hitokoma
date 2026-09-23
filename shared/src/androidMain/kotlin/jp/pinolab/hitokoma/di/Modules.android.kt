package jp.pinolab.hitokoma.di

import jp.pinolab.hitokoma.core.file.LocalImageStorage
import jp.pinolab.hitokoma.data.local.db.getDatabaseBuilder
import jp.pinolab.hitokoma.data.local.db.getRoomDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single { getRoomDatabase(getDatabaseBuilder(androidContext())) }
    single { LocalImageStorage(androidContext()) }
}
