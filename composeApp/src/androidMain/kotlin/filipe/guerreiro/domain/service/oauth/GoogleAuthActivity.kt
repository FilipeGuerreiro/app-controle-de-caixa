package filipe.guerreiro.domain.service.oauth

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import filipe.guerreiro.domain.model.oauth.AuthResult
import filipe.guerreiro.domain.model.oauth.GoogleUser
import filipe.guerreiro.BuildConfig
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GoogleAuthActivity : ComponentActivity() {

    companion object {
        var authDeferred: CompletableDeferred<AuthResult>? = null
        var isSignOut: Boolean = false
        
        private val WEB_CLIENT_ID = BuildConfig.WEB_CLIENT_ID
        private const val DRIVE_APP_DATA_SCOPE = "https://www.googleapis.com/auth/drive.appdata"
    }

    private val signInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.result
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val androidAccount = account.account
                        if (androidAccount != null) {
                            // Obtem o Token de Acesso (OAuth2 Access Token) especifico para Ktor chamadas
                            val token = GoogleAuthUtil.getToken(
                                this@GoogleAuthActivity,
                                androidAccount,
                                "oauth2:$DRIVE_APP_DATA_SCOPE"
                            )
                            val user = GoogleUser(
                                id = account.id ?: "",
                                email = account.email ?: "",
                                displayName = account.displayName,
                                photoUrl = account.photoUrl?.toString()
                            )
                            authDeferred?.complete(AuthResult.Success(user, token))
                        } else {
                            authDeferred?.complete(AuthResult.Error("Conta Android não encontrada no dispositivo."))
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        authDeferred?.complete(AuthResult.Error("Falha ao obter token de acesso do Google", e))
                    } finally {
                        finish()
                    }
                }
            } catch (e: Exception) {
                authDeferred?.complete(AuthResult.Error("Falha no login nativo do Google", e))
                finish()
            }
        } else {
            authDeferred?.complete(AuthResult.Cancelled)
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(WEB_CLIENT_ID)
            .requestEmail()
            .requestScopes(Scope(DRIVE_APP_DATA_SCOPE))
            .build()
            
        val signInClient = GoogleSignIn.getClient(this, gso)

        if (isSignOut) {
            signInClient.signOut().addOnCompleteListener {
                authDeferred?.complete(AuthResult.SignedOut)
                isSignOut = false
                finish()
            }
        } else {
            signInLauncher.launch(signInClient.signInIntent)
        }
    }
}
