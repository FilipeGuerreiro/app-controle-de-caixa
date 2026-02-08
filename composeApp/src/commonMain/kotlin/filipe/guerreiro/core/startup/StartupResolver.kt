package filipe.guerreiro.core.startup

import filipe.guerreiro.domain.repository.UserRepository
import filipe.guerreiro.domain.session.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class StartupResolver(
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager
) : AppStartupResolver {

    override suspend fun resolveStartState(): AppStartState = withContext(Dispatchers.IO) {
        
        val users = userRepository.getAllUsers()

        if (users.isEmpty()) {
            return@withContext AppStartState.NeedsUserCreation
        }

        val isLoggedIn = sessionManager.isLoggedIn.first()

        if (isLoggedIn) {
            AppStartState.Ready
        } else {
            AppStartState.NeedsUserSelection
        }
    }
}