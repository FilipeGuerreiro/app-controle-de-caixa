package filipe.guerreiro.domain.repository

import filipe.guerreiro.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun getAllUsers(): List<User>

    suspend fun getUserById(id: Long): User?

    suspend fun createUser(name: String, businessName: String): User
}