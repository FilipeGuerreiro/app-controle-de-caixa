package filipe.guerreiro.di

import filipe.guerreiro.core.startup.AppStartupResolver
import filipe.guerreiro.core.startup.AppViewModel
import filipe.guerreiro.core.startup.StartupResolver
import filipe.guerreiro.data.local.AppDatabase
import filipe.guerreiro.data.local.CashRepositoryImpl
import filipe.guerreiro.data.local.UserRepositoryImpl
import filipe.guerreiro.data.local.getDatabaseBuilder
import filipe.guerreiro.domain.repository.CashRepository
import filipe.guerreiro.domain.repository.UserRepository
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

    single<AppDatabase> { getDatabaseBuilder(get()) }

    // Daos
    single { get<AppDatabase>().cashDao() }
    single { get<AppDatabase>().transactionDao() }
    single { get<AppDatabase>().userDao() }

    // Repositorys
    single<CashRepository> { CashRepositoryImpl(get(), get()) }
    single<UserRepository> { UserRepositoryImpl(get()) }

    viewModel {
        RegisterViewModel(get(), get(), get())
    }

    viewModel {
        AppViewModel(get())
    }

    viewModel {
        HomeViewModel(get(), get())
    }

    viewModel {
        OpeningViewModel(get())
    }

    viewModel {
        UserSelectionViewModel(get(), get(), get())
    }
}
