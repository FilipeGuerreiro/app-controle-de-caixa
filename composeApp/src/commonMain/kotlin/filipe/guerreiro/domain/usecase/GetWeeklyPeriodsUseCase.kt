package filipe.guerreiro.domain.usecase

import filipe.guerreiro.domain.model.CashSession
import filipe.guerreiro.domain.repository.CashRepository
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.number
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

data class WeeklyPeriodUi(
    val label: String,           // Ex: "26/01 – 30/01"
    val startDate: LocalDate,
    val endDate: LocalDate,
    val sessionCount: Int,
    val sessions: List<CashSession>
)

class GetWeeklyPeriodsUseCase(
    private val cashRepository: CashRepository
) {
    suspend operator fun invoke(userId: Long): List<WeeklyPeriodUi> {
        val allSessions = cashRepository.getAllSessionsSuspend(userId)
        if (allSessions.isEmpty()) return emptyList()

        val tz = TimeZone.currentSystemDefault()

        // Group sessions by ISO week (Monday–Sunday)
        val byWeek = mutableMapOf<LocalDate, MutableList<CashSession>>()

        for (session in allSessions) {
            val localDate = session.openingTimeStamp.toLocalDateTime(tz).date
            val monday = getMonday(localDate)
            byWeek.getOrPut(monday) { mutableListOf() }.add(session)
        }

        return byWeek.entries
            .sortedByDescending { it.key }
            .map { (monday, sessions) ->
                val sunday = monday.plus(6, DateTimeUnit.DAY)

                // Last session day in this week
                val lastDay = sessions
                    .maxOf { it.openingTimeStamp.toLocalDateTime(tz).date }
                    .let { if (it > sunday) sunday else it }

                val startLabel = "${monday.day.toString().padStart(2, '0')}/${monday.month.number.toString().padStart(2, '0')}"
                val endLabel = "${lastDay.day.toString().padStart(2, '0')}/${lastDay.month.number.toString().padStart(2, '0')}"

                WeeklyPeriodUi(
                    label = "$startLabel – $endLabel",
                    startDate = monday,
                    endDate = lastDay,
                    sessionCount = sessions.size,
                    sessions = sessions
                )
            }
    }

    private fun getMonday(date: LocalDate): LocalDate {
        val dayOfWeek = date.dayOfWeek
        val daysFromMonday = (dayOfWeek.isoDayNumber - 1)
        return date.minus(daysFromMonday, DateTimeUnit.DAY)
    }
}
