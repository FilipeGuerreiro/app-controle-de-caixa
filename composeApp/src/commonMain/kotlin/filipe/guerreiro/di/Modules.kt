package filipe.guerreiro.di

import filipe.guerreiro.data.local.AppDatabase
import filipe.guerreiro.data.local.getDatabaseBuilder
import filipe.guerreiro.ui.home.HomeViewModel
import filipe.guerreiro.ui.navigation.NavigationViewModel
import org.koin.dsl.module

val appModule = module {

    factory { NavigationViewModel() }
    factory { HomeViewModel(get()) }

    // 1. Definimos como criar o banco (recebendo o builder específico de cada plataforma)
    single<AppDatabase> { getDatabaseBuilder(get()) }

    // 2. Definimos como criar os DAOs (Injeção de dependência pura!)
    single { get<AppDatabase>().cashDao() }
    single { get<AppDatabase>().transactionDao() }
}