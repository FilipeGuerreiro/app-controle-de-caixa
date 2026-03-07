package filipe.guerreiro.domain.service.oauth

import filipe.guerreiro.domain.model.oauth.AuthResult
import kotlinx.coroutines.flow.StateFlow

/**
 * Classe multiplataforma para gerenciar o login pelo Google.
 * Cada plataforma (Android/iOS) terá a sua própria implementação nativa (`actual`).
 */
expect class GoogleAuthService() {
    
    /**
     * Estado atual da autenticação, permitindo que a UI (Compose) reaja
     * instantaneamente a mudanças de login/logout no app.
     */
    val authState: StateFlow<AuthResult?>

    /**
     * Inicia o fluxo interativo de Sign-In do Google.
     * Deve retornar o resultado (Success, Cancelled, Error) após o usuário interagir.
     */
    suspend fun signIn(): AuthResult

    /**
     * Desloga o usuário e limpa as credenciais ativas do sistema.
     */
    suspend fun signOut()

    /**
     * Tenta recuperar o usuário já logado silenciosamente (sem exibir tela de login).
     * Útil quando o app é aberto para verificar se a sessão ainda é válida.
     */
    suspend fun signInSilently(): AuthResult

    /**
     * Retorna o token de acesso (OAuth) atual caso exista uma sessão ativa.
     * Esse token precisará do escopo do Google Drive App Data Folder.
     */
    fun getAccessToken(): String?
}
