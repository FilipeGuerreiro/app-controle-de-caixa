package filipe.guerreiro.di

import filipe.guerreiro.core.startup.AppStartupResolver
import filipe.guerreiro.core.startup.AppViewModel
import filipe.guerreiro.core.startup.StartupResolver
import filipe.guerreiro.data.local.AppDatabase
import filipe.guerreiro.data.local.CashRepositoryImpl
import filipe.guerreiro.data.local.UserRepositoryImpl
import filipe.guerreiro.data.local.getDatabaseBuilder
import filipe.guerreiro.data.session.SessionManagerImpl
import filipe.guerreiro.domain.repository.CashRepository
import filipe.guerreiro.domain.repository.UserRepository
import filipe.guerreiro.domain.session.SessionManager
import filipe.guerreiro.ui.cash.CashViewModel
import filipe.guerreiro.ui.home.HomeViewModel
import filipe.guerreiro.ui.navigation.NavigationViewModel
import filipe.guerreiro.ui.opening.OpeningViewModel
import filipe.guerreiro.ui.register.RegisterViewModel
import filipe.guerreiro.ui.userselection.UserSelectionViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

    factory<AppStartupResolver> { StartupResolver(get(), get()) }

    factory { NavigationViewModel() }

    // Database com inicialização lazy - só construído quando primeiro DAO for acessado
    single<AppDatabase> { 
        getDatabaseBuilder(get()) 
    }

    // DAOs - resolvidos sob demanda (lazy por padrão no Koin)
    single { get<AppDatabase>().cashDao() }
    single { get<AppDatabase>().transactionDao() }
    single { get<AppDatabase>().userDao() }

    // Repositorys
    single<CashRepository> { CashRepositoryImpl(get(), get()) }
    single<UserRepository> { UserRepositoryImpl(get()) }

    // Session Manager - singleton para gerenciar estado de autenticação
    single<SessionManager> { SessionManagerImpl(get(), get()) }

    viewModel {
        RegisterViewModel(get(), get(), get())
    }

    viewModel {
        AppViewModel(get())
    }

    viewModel {
        HomeViewModel(get())
    }

    viewModel {
        CashViewModel(get())
    }

    viewModel {
        OpeningViewModel(get())
    }

    viewModel {
        UserSelectionViewModel(get(), get())
    }
}

