package jp.pinolab.hitokoma

import android.app.Application
import jp.pinolab.hitokoma.di.initKoin
import org.koin.android.ext.koin.androidContext

class HitokomaApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidContext(this@HitokomaApplication)
        }
    }
}
