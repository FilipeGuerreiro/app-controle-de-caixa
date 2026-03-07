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

/**
 * Módulo com TODAS as dependências que derivam do AppDatabase.
 * Extraído do appModule para permitir unload/reload (Hot-Swap) durante
 * operações de backup/restore sem reiniciar o processo do app.
 *
 * Quando o arquivo .db é substituído em disco, este módulo é descarregado
 * e recarregado, forçando a recriação de todas as instâncias (Database → DAOs → Repos → SessionManager).
 */
val databaseDependenciesModule = module {

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
    single { get<AppDatabase>().auditLogDao() }

    // Repositories
    single<CashRepository> { CashRepositoryImpl(get(), get()) }
    single<UserRepository> { UserRepositoryImpl(get()) }
    single<PaymentMethodRepository> { PaymentMethodRepositoryImpl(get()) }
    single<CategoryRepository> { CategoryRepositoryImpl(get()) }
    single<TransactionRepository> { TransactionRepositoryImpl(get(), get()) }
    single<filipe.guerreiro.domain.repository.AuditLogRepository> { filipe.guerreiro.data.local.AuditLogRepositoryImpl(get()) }

    // Session Manager - depende de UserRepository (que depende do banco)
    single<SessionManager> { SessionManagerImpl(get(), get()) }
}

val appModule = module {

    factory<AppStartupResolver> { StartupResolver(get(), get()) }

    factory { NavigationViewModel() }

    // Use Cases
    factory { filipe.guerreiro.domain.usecase.UpdateTransactionUseCase(get(), get()) }
    factory { filipe.guerreiro.domain.usecase.DeleteTransactionUseCase(get(), get()) }
    factory { filipe.guerreiro.domain.usecase.GetSessionAuditLogsUseCase(get()) }
    factory { filipe.guerreiro.domain.usecase.GenerateDailyReportUseCase() }
    factory { filipe.guerreiro.domain.usecase.GenerateWeeklyReportUseCase() }
    factory { filipe.guerreiro.domain.usecase.GetWeeklyPeriodsUseCase(get()) }
    single { filipe.guerreiro.domain.service.ShareManager() }

    // Google Auth Service
    single { filipe.guerreiro.domain.service.oauth.GoogleAuthService() }

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
        (cashId: Long) -> CashDetailViewModel(cashId, get(), get(), get(), get(), get(), get(), get())
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

    viewModel {
        (cashId: Long?) -> filipe.guerreiro.ui.reports.ReportsDashboardViewModel(cashId, get(), get(), get(), get(), get(), get(), get(), get(), get())
    }
}
