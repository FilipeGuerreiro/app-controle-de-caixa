package filipe.guerreiro.di

import filipe.guerreiro.core.startup.AppStartupResolver
import filipe.guerreiro.core.startup.AppViewModel
import filipe.guerreiro.core.startup.StartupResolver
import filipe.guerreiro.data.local.AppDatabase
import filipe.guerreiro.data.local.CashRepositoryImpl
import filipe.guerreiro.data.local.CategoryRepositoryImpl
import filipe.guerreiro.data.local.PaymentMethodRepositoryImpl
import filipe.guerreiro.data.local.TransactionRepositoryImpl
import filipe.guerreiro.data.local.UserRepositoryImpl
import filipe.guerreiro.data.local.getDatabaseBuilder
import filipe.guerreiro.data.session.SessionManagerImpl
import filipe.guerreiro.domain.repository.CashRepository
import filipe.guerreiro.domain.repository.CategoryRepository
import filipe.guerreiro.domain.repository.PaymentMethodRepository
import filipe.guerreiro.domain.repository.TransactionRepository
import filipe.guerreiro.domain.repository.UserRepository
import filipe.guerreiro.domain.session.SessionManager
import filipe.guerreiro.ui.cash.detail.CashDetailViewModel
import filipe.guerreiro.ui.cash.listing.CashListViewModel
import filipe.guerreiro.ui.category.CategoryViewModel
import filipe.guerreiro.ui.closing.ClosingViewModel
import filipe.guerreiro.ui.home.HomeViewModel
import filipe.guerreiro.ui.navigation.NavigationViewModel
import filipe.guerreiro.ui.opening.OpeningViewModel
import filipe.guerreiro.ui.onboarding.OnboardingViewModel
import filipe.guerreiro.ui.paymentmethod.PaymentMethodViewModel
import filipe.guerreiro.ui.register.RegisterViewModel
import filipe.guerreiro.ui.transaction.TransactionViewModel
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
    single { get<AppDatabase>().paymentMethodDao() }
    single { get<AppDatabase>().categoryDao() }

    // Repositorys
    single<CashRepository> { CashRepositoryImpl(get(), get()) }
    single<UserRepository> { UserRepositoryImpl(get()) }
    single<PaymentMethodRepository> { PaymentMethodRepositoryImpl(get()) }
    single<CategoryRepository> { CategoryRepositoryImpl(get()) }
    single<TransactionRepository> { TransactionRepositoryImpl(get(), get()) }


    // Session Manager - singleton para gerenciar estado de autenticação
    single<SessionManager> { SessionManagerImpl(get(), get()) }

    viewModel {
        RegisterViewModel(get(), get(), get())
    }

    viewModel {
        AppViewModel(get())
    }

    viewModel {
        HomeViewModel(get(), get(), get(), get(), get())
    }

    viewModel {
        CashListViewModel(get(), get(), get())
    }

    viewModel {
        (cashId: Long) -> CashDetailViewModel(cashId, get(), get(), get(), get())
    }

    viewModel {
        OpeningViewModel(get(), get(), get())
    }

    viewModel {
        ClosingViewModel(get(), get(), get())
    }

    viewModel {
        UserSelectionViewModel(get(), get())
    }

    viewModel {
        PaymentMethodViewModel(get(), get())
    }

    viewModel {
        CategoryViewModel(get(), get())
    }

    viewModel {
        TransactionViewModel(get(), get(), get(), get())
    }

    viewModel {
        OnboardingViewModel(get(), get(), get())
    }
}
