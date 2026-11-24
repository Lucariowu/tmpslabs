package lab2.patterns.proxy

data class PaymentResult(
    val isSuccess: Boolean,
    val transactionId: String,
    val amountCharged: Double,
    val message: String
)

interface PaymentService {
    fun processPayment(customerId: String, amount: Double, authToken: String): PaymentResult
}
