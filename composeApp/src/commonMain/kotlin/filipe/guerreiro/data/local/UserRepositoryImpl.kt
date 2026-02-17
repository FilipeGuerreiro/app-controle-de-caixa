package filipe.guerreiro.data.local

import filipe.guerreiro.data.local.dao.UserDao
import filipe.guerreiro.domain.model.User
import filipe.guerreiro.domain.repository.UserRepository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

class UserRepositoryImpl(
    private val userDao: UserDao,
) : UserRepository {

    override suspend fun getAllUsers(): List<User> {
        return withContext(Dispatchers.IO) {
            userDao.getAll()
        }
    }

    override suspend fun getUserById(id: Long): User? {
        return withContext(Dispatchers.IO) {
            userDao.getById(id)
        }
    }

    override suspend fun createUser(name: String, businessName: String): User {
        return withContext(Dispatchers.IO) {
            val user = User(name = name, businessName = businessName)
            val id = userDao.insert(user)
            user.copy(id = id)
        }
    }
}