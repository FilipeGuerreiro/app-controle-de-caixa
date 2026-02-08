package filipe.guerreiro.core.startup

import filipe.guerreiro.data.SessionPreferences
import filipe.guerreiro.domain.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class StartupResolver(
    private val userRepository: UserRepository,
    private val sessionPreferences: SessionPreferences
) : AppStartupResolver {

    override suspend fun resolveStartState(): AppStartState = withContext(Dispatchers.IO) {
        println("StartupResolver: starting resolveStartState()")

        val users = userRepository.getAllUsers()
        println("StartupResolver: found ${users.size} users")

        if (users.isEmpty()) {
            println("StartupResolver: no users present -> NeedsUserCreation")
            return@withContext AppStartState.NeedsUserCreation
        }

        val loggedUserId = sessionPreferences.getLoggedUserId()
        println("StartupResolver: current loggedUserId = $loggedUserId")

        println("StartupResolver: performing startup delay")

        if (loggedUserId != null) {
            println("StartupResolver: user is logged -> Ready")
            AppStartState.Ready
        } else {
            println("StartupResolver: no logged user -> NeedsUserSelection")
            AppStartState.NeedsUserSelection
        }
    }
}