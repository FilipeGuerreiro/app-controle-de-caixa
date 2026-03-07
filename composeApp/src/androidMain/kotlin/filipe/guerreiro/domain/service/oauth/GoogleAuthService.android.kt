package filipe.guerreiro.domain.service.oauth

import android.content.Context
import android.content.Intent
import filipe.guerreiro.domain.model.oauth.AuthResult
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.koin.java.KoinJavaComponent.getKoin

actual class GoogleAuthService {

    private val context: Context get() = getKoin().get()
    
    private val _authState = MutableStateFlow<AuthResult?>(null)
    actual val authState: StateFlow<AuthResult?> = _authState.asStateFlow()
    
    private var cachedAccessToken: String? = null

    actual suspend fun signIn(): AuthResult {
        _authState.value = AuthResult.Loading
        val deferred = CompletableDeferred<AuthResult>()
        GoogleAuthActivity.authDeferred = deferred
        GoogleAuthActivity.isSignOut = false
        
        val intent = Intent(context, GoogleAuthActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
        
        val result = deferred.await()
        if (result is AuthResult.Success) {
            cachedAccessToken = result.accessToken
        }
        _authState.value = result
        return result
    }

    actual suspend fun signOut() {
        _authState.value = AuthResult.Loading
        val deferred = CompletableDeferred<AuthResult>()
        GoogleAuthActivity.authDeferred = deferred
        GoogleAuthActivity.isSignOut = true
        
        val intent = Intent(context, GoogleAuthActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
        
        deferred.await()
        cachedAccessToken = null
        _authState.value = AuthResult.SignedOut
    }

    actual suspend fun signInSilently(): AuthResult {
        return AuthResult.Cancelled
    }

    actual fun getAccessToken(): String? {
        return cachedAccessToken
    }
}
