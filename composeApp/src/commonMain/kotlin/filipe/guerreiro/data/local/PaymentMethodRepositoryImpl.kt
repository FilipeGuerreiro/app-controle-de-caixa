package filipe.guerreiro.data.local

import filipe.guerreiro.data.local.dao.PaymentMethodDao
import filipe.guerreiro.domain.model.PaymentMethod
import filipe.guerreiro.domain.repository.PaymentMethodRepository
import kotlinx.coroutines.flow.Flow

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

class PaymentMethodRepositoryImpl(
    private val dao: PaymentMethodDao
) : PaymentMethodRepository {
    override fun getPaymentMethods(userId: Long): Flow<List<PaymentMethod>> {
        val payments = dao.getAll(userId)
        return payments
    }

    override suspend fun createPaymentMethod(userId: Long, name: String) {
        withContext(Dispatchers.IO) {
            val foundPaymentMethod = dao.getByName(userId, name)

            if (foundPaymentMethod != null) {
                throw IllegalStateException("Já existe um método de pagamento com esse nome.")
            }
            val newPayment = PaymentMethod(
                name = name,
                userId = userId
            )
            dao.insert(newPayment)
        }
    }

    override suspend fun updatePaymentMethod(userId: Long, paymentId: Long, name: String) {
        withContext(Dispatchers.IO) {
            val foundPaymentMethod = dao.getByName(userId, name)

            if (foundPaymentMethod != null && foundPaymentMethod.id != paymentId) {
                throw IllegalStateException("Já existe um método de pagamento com esse nome.")
            }
            dao.update(paymentId, name)
        }
    }

    override suspend fun deletePaymentMethod(userId: Long, paymentId: Long) {
        withContext(Dispatchers.IO) {
            dao.deleteById(paymentId)
        }
    }
}