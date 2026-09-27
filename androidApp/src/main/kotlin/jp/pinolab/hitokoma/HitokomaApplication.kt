package jp.pinolab.hitokoma

import android.app.Application
import jp.pinolab.hitokoma.di.initKoin
import jp.pinolab.hitokoma.feature.monthlyvideo.MonthlyVideoScheduler
import org.koin.android.ext.koin.androidContext

class HitokomaApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidContext(this@HitokomaApplication)
        }
        // 月初に先月のストーリー動画を生成する定期実行を登録
        MonthlyVideoScheduler.schedule(this)
    }
}
