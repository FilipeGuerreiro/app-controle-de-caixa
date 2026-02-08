package filipe.guerreiro.domain.session

import filipe.guerreiro.domain.model.User
import kotlinx.coroutines.flow.StateFlow

/**
 * Gerenciador centralizado de sessão do usuário.
 * Expõe o estado de autenticação de forma reativa via StateFlow.
 */
interface SessionManager {
    /**
     * Usuário atualmente logado, ou null se não houver sessão ativa.
     */
    val currentUser: StateFlow<User?>

    /**
     * Indica se há um usuário logado.
     */
    val isLoggedIn: StateFlow<Boolean>

    /**
     * Realiza login com o userId especificado.
     * @param userId ID do usuário a logar
     * @return Result com o User em caso de sucesso, ou falha
     */
    suspend fun login(userId: Long): Result<User>

    /**
     * Encerra a sessão do usuário atual.
     */
    suspend fun logout()

    /**
     * Recarrega os dados do usuário logado do banco de dados.
     * Útil quando os dados do usuário podem ter sido atualizados.
     */
    suspend fun refreshSession()
}
