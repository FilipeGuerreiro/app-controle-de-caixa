package filipe.guerreiro.domain.repository

import filipe.guerreiro.domain.model.PaymentMethod
import kotlinx.coroutines.flow.Flow

interface PaymentMethodRepository {

    fun getPaymentMethods(userId: Long): Flow<List<PaymentMethod>>
    suspend fun createPaymentMethod(userId: Long, name: String)
    suspend fun updatePaymentMethod(userId: Long, paymentId: Long, name: String)
    suspend fun deletePaymentMethod(userId: Long, paymentId: Long)
}