package filipe.guerreiro

import android.app.Application
import filipe.guerreiro.di.appModule
import org.koin.android.ext.koin.androidContext

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        initKoin {
            androidContext(this@MainApplication)
            modules(
                appModule, // CommonModule
                androidDatabaseModule,
                androidModule
            )
        }
    }
}