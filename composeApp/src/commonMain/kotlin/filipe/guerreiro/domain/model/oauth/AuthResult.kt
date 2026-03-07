package filipe.guerreiro.domain.model.oauth

data class GoogleUser(
    val id: String,
    val email: String,
    val displayName: String?,
    val photoUrl: String?
)

sealed class AuthResult {
    data object Loading : AuthResult()
    data class Success(val user: GoogleUser, val accessToken: String) : AuthResult()
    data class Error(val message: String, val exception: Throwable? = null) : AuthResult()
    data object Cancelled : AuthResult()
    data object SignedOut : AuthResult()
}
