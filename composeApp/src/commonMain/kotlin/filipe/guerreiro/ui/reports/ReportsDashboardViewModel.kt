package filipe.guerreiro.ui.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import filipe.guerreiro.domain.model.TransactionType
import filipe.guerreiro.domain.model.toCurrencyString
import filipe.guerreiro.domain.repository.CashRepository
import filipe.guerreiro.domain.repository.CategoryRepository
import filipe.guerreiro.domain.repository.PaymentMethodRepository
import filipe.guerreiro.domain.repository.TransactionRepository
import filipe.guerreiro.domain.session.SessionManager
import filipe.guerreiro.domain.usecase.DailyAggregate
import filipe.guerreiro.domain.usecase.GenerateDailyReportUseCase
import filipe.guerreiro.domain.usecase.GenerateWeeklyReportUseCase
import filipe.guerreiro.domain.usecase.GetWeeklyPeriodsUseCase
import filipe.guerreiro.domain.usecase.WeeklyPeriodUi
import filipe.guerreiro.domain.service.ShareManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import kotlin.math.roundToInt

class ReportsDashboardViewModel(
    private val initialCashId: Long?,
    private val cashRepository: CashRepository,
    private val transactionRepository: TransactionRepository,
    private val paymentMethodRepository: PaymentMethodRepository,
    private val categoryRepository: CategoryRepository,
    private val getWeeklyPeriodsUseCase: GetWeeklyPeriodsUseCase,
    private val sessionManager: SessionManager,
    private val generateDailyReportUseCase: GenerateDailyReportUseCase,
    private val generateWeeklyReportUseCase: GenerateWeeklyReportUseCase,
    private val shareManager: ShareManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportsDashboardUiState(preSelectedCashId = initialCashId))
    val uiState: StateFlow<ReportsDashboardUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            val userId = sessionManager.currentUser.value?.id ?: return@launch
            val periods = getWeeklyPeriodsUseCase(userId)
            
            val allSessions = cashRepository.getAllSessionsSuspend(userId)
            val dailySessionsUi = allSessions.sortedByDescending { it.openingTimeStamp }.map { s ->
                val date = s.openingTimeStamp.toLocalDateTime(TimeZone.currentSystemDefault())
                val label = "${date.day.toString().padStart(2, '0')}/${date.month.number.toString().padStart(2, '0')}/${date.year} - ${s.initialAmount.toCurrencyString()} Inicial"
                DailySessionUi(cashId = s.id, label = label)
            }
            
            _uiState.value = _uiState.value.copy(
                weeklyPeriods = periods,
                dailySessions = dailySessionsUi
            )

            if (initialCashId != null) {
                // Link direto do Detalhe do Caixa
                val selectedDaily = dailySessionsUi.find { it.cashId == initialCashId }
                _uiState.value = _uiState.value.copy(
                    selectedSegment = ReportSegment.DAILY,
                    selectedDailySession = selectedDaily
                )
                loadDailyData(initialCashId)
            } else {
                // Abriu pela Navbar
                val latestPeriod = periods.firstOrNull()
                _uiState.value = _uiState.value.copy(
                    selectedSegment = ReportSegment.WEEKLY, 
                    selectedWeeklyPeriod = latestPeriod,
                    selectedDailySession = dailySessionsUi.firstOrNull()
                )
                if (latestPeriod != null) {
                    loadWeeklyData(latestPeriod, userId)
                }
            }
            
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    fun onSegmentChanged(segment: ReportSegment) {
        if (_uiState.value.selectedSegment == segment) return
        _uiState.value = _uiState.value.copy(selectedSegment = segment)
        
        viewModelScope.launch {
            if (segment == ReportSegment.DAILY) {
                // Se o usuário trocou para diário, e tem um cashId, recarrega. Se não tem, ele precisa escolher (ou carregamos o último).
                val idToLoad = _uiState.value.preSelectedCashId ?: _uiState.value.selectedDailySession?.cashId
                if (idToLoad != null) loadDailyData(idToLoad)
            } else {
                val period = _uiState.value.selectedWeeklyPeriod ?: _uiState.value.weeklyPeriods.firstOrNull()
                if (period != null) loadWeeklyData(period, sessionManager.currentUser.value?.id!!)
            }
        }
    }

    fun onWeeklyPeriodSelected(period: WeeklyPeriodUi) {
        _uiState.value = _uiState.value.copy(selectedWeeklyPeriod = period)
        viewModelScope.launch {
            val userId = sessionManager.currentUser.value?.id ?: return@launch
            loadWeeklyData(period, userId)
        }
    }

    fun onDailySessionSelected(session: DailySessionUi) {
        _uiState.value = _uiState.value.copy(selectedDailySession = session)
        viewModelScope.launch {
            loadDailyData(session.cashId)
        }
    }

    private suspend fun loadDailyData(cashId: Long) {
        val session = cashRepository.getSessionById(cashId).firstOrNull() ?: return
        val transactions = transactionRepository.getAllTransactions(cashId).firstOrNull() ?: emptyList()
        val userId = sessionManager.currentUser.value?.id ?: return
        val paymentMethodsDb = paymentMethodRepository.getPaymentMethods(userId).firstOrNull() ?: emptyList()
        val categoriesDb = categoryRepository.getCategories(userId).firstOrNull() ?: emptyList()

        val incomes = transactions.filter { it.type == TransactionType.INCOME }
        val expenses = transactions.filter { it.type == TransactionType.EXPENSE }

        val totalInflow = incomes.sumOf { it.amount }
        val totalOutflow = expenses.sumOf { it.amount }
        val netBalance = session.initialAmount + totalInflow - totalOutflow

        val dateLabel = session.openingTimeStamp.toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()

        val pms = incomes.groupBy { tx -> 
            paymentMethodsDb.find { it.id == tx.paymentMethodId }?.name ?: "Outros" 
        }.map { (name, txs) ->
            val sum = txs.sumOf { it.amount }
            val perc = if (totalInflow > 0) ((sum.toDouble() / totalInflow) * 100).roundToInt() else 0
            PaymentBreakdownUi(name, sum, perc)
        }.sortedByDescending { it.percentage }

        // Saídas por categoria
        val expensesByCategory = expenses.groupBy { tx ->
            categoriesDb.find { it.id == tx.categoryId }?.name ?: "Outros"
        }.map { (name, txs) ->
            val sum = txs.sumOf { it.amount }
            val perc = if (totalOutflow > 0) ((sum.toDouble() / totalOutflow) * 100).roundToInt() else 0
            CategoryBreakdownUi(name, sum, perc)
        }.sortedByDescending { it.percentage }

        // Progresso da meta diária
        val goalProgress = session.dailyGoalAmount?.let { goal ->
            if (goal > 0) {
                val perc = ((totalInflow.toDouble() / goal) * 100).roundToInt()
                GoalProgressUi(currentInflow = totalInflow, goalAmount = goal, percentage = perc)
            } else null
        }

        _uiState.value = _uiState.value.copy(
            dailyDateLabel = dateLabel,
            dailySummary = PeriodSummaryUi(netBalance, totalInflow, totalOutflow, session.initialAmount),
            dailyTransactions = transactions.map { tx ->
                val pmName = paymentMethodsDb.find { it.id == tx.paymentMethodId }?.name ?: "Outros"
                val catName = categoriesDb.find { it.id == tx.categoryId }?.name ?: "Outros"
                
                val local = tx.timestamp.toLocalDateTime(TimeZone.currentSystemDefault())
                val formattedTime = "${local.hour.toString().padStart(2, '0')}:${local.minute.toString().padStart(2, '0')}"
                
                filipe.guerreiro.ui.reports.TransactionUi(
                    id = tx.id.toString(),
                    title = tx.description,
                    amount = tx.amount,
                    isIncome = tx.type == TransactionType.INCOME,
                    categoryName = catName,
                    paymentMethodName = pmName,
                    time = formattedTime
                )
            },
            dailyPaymentMethods = pms,
            dailyExpensesByCategory = expensesByCategory,
            dailyGoalProgress = goalProgress
        )
    }

    private suspend fun loadWeeklyData(period: WeeklyPeriodUi, userId: Long) {
        val paymentMethodsDb = paymentMethodRepository.getPaymentMethods(userId).firstOrNull() ?: emptyList()
        val categoriesDb = categoryRepository.getCategories(userId).firstOrNull() ?: emptyList()
        
        var totalInflow = 0L
        var totalOutflow = 0L
        var totalInitialAmount = 0L
        val txsByMethod = mutableMapOf<String, Long>()
        val txsByExpenseCategory = mutableMapOf<String, Long>()
        val dailyAggregates = mutableListOf<DailyAggregateUi>()

        val sessionsByDate = period.sessions.groupBy { s ->
            s.openingTimeStamp.toLocalDateTime(TimeZone.currentSystemDefault()).date
        }.entries.sortedBy { it.key }

        for ((date, sessions) in sessionsByDate) {
            var dayInflow = 0L
            var dayOutflow = 0L
            var dayInitial = 0L
            
            for (s in sessions) {
                dayInitial += s.initialAmount
                val txs = transactionRepository.getAllTransactions(s.id).firstOrNull() ?: emptyList()
                val incomes = txs.filter { it.type == TransactionType.INCOME }
                val expenses = txs.filter { it.type == TransactionType.EXPENSE }
                
                dayInflow += incomes.sumOf { it.amount }
                dayOutflow += expenses.sumOf { it.amount }

                incomes.forEach { tx ->
                    val name = paymentMethodsDb.find { it.id == tx.paymentMethodId }?.name ?: "Outros"
                    txsByMethod[name] = (txsByMethod[name] ?: 0L) + tx.amount
                }

                expenses.forEach { tx ->
                    val catName = categoriesDb.find { it.id == tx.categoryId }?.name ?: "Outros"
                    txsByExpenseCategory[catName] = (txsByExpenseCategory[catName] ?: 0L) + tx.amount
                }
            }

            totalInflow += dayInflow
            totalOutflow += dayOutflow
            totalInitialAmount += dayInitial
            
            val dayNet = dayInitial + dayInflow - dayOutflow
            dailyAggregates.add(
                DailyAggregateUi(
                    dateLabel = date.toString(),
                    inflow = dayInflow,
                    outflow = dayOutflow,
                    netBalance = dayNet
                )
            )
        }

        val netBalance = totalInitialAmount + totalInflow - totalOutflow
        val pms = txsByMethod.map { (name, sum) ->
            val perc = if (totalInflow > 0) ((sum.toDouble() / totalInflow) * 100).roundToInt() else 0
            PaymentBreakdownUi(name, sum, perc)
        }.sortedByDescending { it.percentage }

        val weeklyExpCategories = txsByExpenseCategory.map { (name, sum) ->
            val perc = if (totalOutflow > 0) ((sum.toDouble() / totalOutflow) * 100).roundToInt() else 0
            CategoryBreakdownUi(name, sum, perc)
        }.sortedByDescending { it.percentage }

        // Comparação com semana anterior
        val previousWeekSummary = loadPreviousWeekSummary(period, userId)

        _uiState.value = _uiState.value.copy(
            weeklySummary = PeriodSummaryUi(netBalance, totalInflow, totalOutflow, totalInitialAmount),
            weeklyDailyAggregates = dailyAggregates,
            weeklyPaymentMethods = pms,
            weeklyExpensesByCategory = weeklyExpCategories,
            previousWeekSummary = previousWeekSummary
        )
    }

    private suspend fun loadPreviousWeekSummary(currentPeriod: WeeklyPeriodUi, userId: Long): PeriodSummaryUi? {
        val periods = _uiState.value.weeklyPeriods
        val currentIndex = periods.indexOfFirst { it.startDate == currentPeriod.startDate }
        if (currentIndex < 0 || currentIndex >= periods.size - 1) return null

        val previousPeriod = periods[currentIndex + 1]
        var prevInflow = 0L
        var prevOutflow = 0L
        var prevInitial = 0L

        for (s in previousPeriod.sessions) {
            prevInitial += s.initialAmount
            val txs = transactionRepository.getAllTransactions(s.id).firstOrNull() ?: emptyList()
            prevInflow += txs.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
            prevOutflow += txs.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        }

        val prevNet = prevInitial + prevInflow - prevOutflow
        return PeriodSummaryUi(prevNet, prevInflow, prevOutflow, prevInitial)
    }

    // ── Export Methods ──────────────────────────────────────────────────

    fun showExportSheet() {
        _uiState.value = _uiState.value.copy(showExportSheet = true)
    }

    fun dismissExportSheet() {
        _uiState.value = _uiState.value.copy(showExportSheet = false)
    }

    fun exportCurrentView() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isExporting = true)
            try {
                val userId = sessionManager.currentUser.value?.id ?: return@launch
                val cats = categoryRepository.getCategories(userId).firstOrNull() ?: emptyList()
                val pms = paymentMethodRepository.getPaymentMethods(userId).firstOrNull() ?: emptyList()

                if (_uiState.value.selectedSegment == ReportSegment.DAILY) {
                    exportDailyReport(cats, pms)
                } else {
                    exportWeeklyReport(cats, pms)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _uiState.value = _uiState.value.copy(isExporting = false, showExportSheet = false)
            }
        }
    }

    private suspend fun exportDailyReport(
        cats: List<filipe.guerreiro.domain.model.Category>,
        pms: List<filipe.guerreiro.domain.model.PaymentMethod>
    ) {
        val cashId = _uiState.value.preSelectedCashId
            ?: _uiState.value.selectedDailySession?.cashId
            ?: return

        val session = cashRepository.getSessionById(cashId).firstOrNull() ?: return
        val transactions = transactionRepository.getAllTransactions(cashId).firstOrNull() ?: emptyList()

        val totalInflow = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val totalOutflow = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        val currentBalance = session.initialAmount + totalInflow - totalOutflow

        val xlsxBytes = generateDailyReportUseCase(
            session = session,
            transactions = transactions,
            categories = cats,
            paymentMethods = pms,
            totalInflow = totalInflow,
            totalOutflow = totalOutflow,
            currentBalance = currentBalance
        )

        val date = session.openingTimeStamp.toLocalDateTime(TimeZone.currentSystemDefault())
        val filename = "fechamento_diario_${date.date}.xlsx"
        shareManager.shareXlsxFile(filename, xlsxBytes)
    }

    private suspend fun exportWeeklyReport(
        cats: List<filipe.guerreiro.domain.model.Category>,
        pms: List<filipe.guerreiro.domain.model.PaymentMethod>
    ) {
        val period = _uiState.value.selectedWeeklyPeriod ?: return

        val allTransMap = mutableMapOf<Long, List<filipe.guerreiro.domain.model.Transaction>>()
        val dailyAggs = period.sessions.groupBy { s ->
            s.openingTimeStamp.toLocalDateTime(TimeZone.currentSystemDefault()).date
        }.map { (date, sessions) ->
            var inflow = 0L; var outflow = 0L
            for (s in sessions) {
                val txs = transactionRepository.getAllTransactions(s.id).firstOrNull() ?: emptyList()
                allTransMap[s.id] = txs
                inflow += txs.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
                outflow += txs.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
            }
            DailyAggregate(date = date, totalInflow = inflow, totalOutflow = outflow, sessions = sessions)
        }.sortedBy { it.date }

        val filename = "fechamento_semanal_${period.startDate}_${period.endDate}.xlsx"
        val xlsxBytes = generateWeeklyReportUseCase(period.label, dailyAggs, cats, pms, allTransMap)
        shareManager.shareXlsxFile(filename, xlsxBytes)
    }
}
