package filipe.guerreiro.data.local

import filipe.guerreiro.data.local.dao.UserDao
import filipe.guerreiro.domain.model.User
import filipe.guerreiro.domain.repository.UserRepository

class UserRepositoryImpl(
    private val userDao: UserDao,
) : UserRepository {

    override suspend fun getAllUsers(): List<User> {
        return userDao.getAll()
    }

    override suspend fun getUserById(id: Long): User? {
        return userDao.getById(id)
    }

    override suspend fun createUser(name: String, businessName: String): User {
        val user = User(name = name, businessName = businessName)
        val id = userDao.insert(user)
        return user.copy(id = id)
    }
}