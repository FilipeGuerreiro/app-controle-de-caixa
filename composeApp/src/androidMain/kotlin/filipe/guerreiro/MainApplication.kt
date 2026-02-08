package filipe.guerreiro

import android.app.Application
import filipe.guerreiro.data.local.AppDatabase
import filipe.guerreiro.di.appModule
import filipe.guerreiro.domain.session.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.get
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

        // Pre-warming em background (não bloqueia a Main Thread)
        CoroutineScope(Dispatchers.IO).launch {
            get<AppDatabase>()
            get<SessionManager>()
        }
    }
}

