package filipe.guerreiro.domain.service.oauth

import filipe.guerreiro.domain.model.oauth.AuthResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

actual class GoogleAuthService actual constructor() {

    private val _authState = MutableStateFlow<AuthResult?>(null)
    actual val authState: StateFlow<AuthResult?> = _authState.asStateFlow()

    actual suspend fun signIn(): AuthResult {
        return AuthResult.Error("Google Sign-In não é suportado na plataforma Desktop/JVM.")
    }

    actual suspend fun signOut() {
        _authState.value = AuthResult.SignedOut
    }

    actual suspend fun signInSilently(): AuthResult {
        return AuthResult.Cancelled
    }

    actual fun getAccessToken(): String? {
        return null
    }
}
