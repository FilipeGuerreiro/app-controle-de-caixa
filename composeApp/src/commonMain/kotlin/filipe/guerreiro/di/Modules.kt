package filipe.guerreiro.di

import filipe.guerreiro.core.startup.AppStartupResolver
import filipe.guerreiro.core.startup.AppViewModel
import filipe.guerreiro.core.startup.StartupResolver
import filipe.guerreiro.data.local.AppDatabase
import filipe.guerreiro.data.local.CashRepositoryImpl
import filipe.guerreiro.data.local.getDatabaseBuilder
import filipe.guerreiro.domain.repository.CashRepository
import filipe.guerreiro.ui.home.HomeViewModel
import filipe.guerreiro.ui.navigation.NavigationViewModel
import filipe.guerreiro.ui.register.RegisterViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

    factory<AppStartupResolver> {
        StartupResolver(get())
    }

    factory { NavigationViewModel() }

    // 1. Definimos como criar o banco (recebendo o builder específico de cada plataforma)
    single<AppDatabase> { getDatabaseBuilder(get()) }

    // 2. Definimos como criar os DAOs (Injeção de dependência pura!)
    single { get<AppDatabase>().cashDao() }
    single { get<AppDatabase>().transactionDao() }

    // 3. Repositório
    single<CashRepository> { CashRepositoryImpl(get(), get()) }

    viewModel {
        RegisterViewModel(get())
    }

    viewModel {
        AppViewModel(get())
    }

    viewModel {
        HomeViewModel(get())
    }
}