package filipe.guerreiro.data.session

import filipe.guerreiro.data.SessionPreferences
import filipe.guerreiro.domain.model.User
import filipe.guerreiro.domain.repository.UserRepository
import filipe.guerreiro.domain.session.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val TAG = "[SessionManager]"

/**
 * Implementação do SessionManager.
 * Gerencia o estado de autenticação do usuário de forma centralizada.
 */
class SessionManagerImpl(
    private val sessionPreferences: SessionPreferences,
    private val userRepository: UserRepository
) : SessionManager {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _currentUser = MutableStateFlow<User?>(null)
    override val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    override val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    init {
        scope.launch {
            loadCurrentUser()
        }
    }

    private suspend fun loadCurrentUser() {
        val userId = sessionPreferences.getLoggedUserId()
        
        if (userId != null) {
            val user = userRepository.getUserById(userId)
            _currentUser.value = user
            _isLoggedIn.value = user != null
        } else {
            _currentUser.value = null
            _isLoggedIn.value = false
        }
    }

    override suspend fun login(userId: Long): Result<User> {
        return try {
            val user = userRepository.getUserById(userId)
            if (user != null) {
                sessionPreferences.setLoggedUserId(userId)
                _currentUser.value = user
                _isLoggedIn.value = true
                Result.success(user)
            } else {
                Result.failure(IllegalArgumentException("Usuário não encontrado"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout() {
        sessionPreferences.clearSession()
        _currentUser.value = null
        _isLoggedIn.value = false
    }

    override suspend fun refreshSession() {
        loadCurrentUser()
    }
}

