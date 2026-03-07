package filipe.guerreiro.ui.reports

import filipe.guerreiro.domain.usecase.WeeklyPeriodUi

data class ReportsDashboardUiState(
    val selectedSegment: ReportSegment = ReportSegment.WEEKLY,
    val isLoading: Boolean = true,

    // Weekly Data
    val weeklyPeriods: List<WeeklyPeriodUi> = emptyList(),
    val selectedWeeklyPeriod: WeeklyPeriodUi? = null,
    val weeklySummary: PeriodSummaryUi = PeriodSummaryUi(),
    val weeklyDailyAggregates: List<DailyAggregateUi> = emptyList(),
    val weeklyPaymentMethods: List<PaymentBreakdownUi> = emptyList(),
    val weeklyExpensesByCategory: List<CategoryBreakdownUi> = emptyList(),
    val previousWeekSummary: PeriodSummaryUi? = null,

    // Daily Data
    val dailySessions: List<DailySessionUi> = emptyList(),
    val selectedDailySession: DailySessionUi? = null,
    val dailyDateLabel: String = "",
    val dailySummary: PeriodSummaryUi = PeriodSummaryUi(),
    val dailyTransactions: List<TransactionUi> = emptyList(),
    val dailyPaymentMethods: List<PaymentBreakdownUi> = emptyList(),
    val dailyExpensesByCategory: List<CategoryBreakdownUi> = emptyList(),
    val dailyGoalProgress: GoalProgressUi? = null,
    
    val preSelectedCashId: Long? = null,

    // Export State
    val isExporting: Boolean = false,
    val showExportSheet: Boolean = false
)

data class DailySessionUi(
    val cashId: Long,
    val label: String
)

enum class ReportSegment { DAILY, WEEKLY }

data class PeriodSummaryUi(
    val netBalance: Long = 0L,
    val totalInflow: Long = 0L,
    val totalOutflow: Long = 0L,
    val initialAmount: Long = 0L
)

data class PaymentBreakdownUi(
    val name: String,
    val amount: Long,
    val percentage: Int
)

data class DailyAggregateUi(
    val dateLabel: String,
    val inflow: Long,
    val outflow: Long,
    val netBalance: Long
)

data class TransactionUi(
    val id: String,
    val title: String,
    val amount: Long,
    val isIncome: Boolean,
    val categoryName: String,
    val paymentMethodName: String,
    val time: String
)

data class CategoryBreakdownUi(
    val name: String,
    val amount: Long,
    val percentage: Int
)

data class GoalProgressUi(
    val currentInflow: Long,
    val goalAmount: Long,
    val percentage: Int
)
