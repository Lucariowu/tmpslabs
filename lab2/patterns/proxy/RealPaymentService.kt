package lab2.patterns.proxy

import java.util.UUID

class RealPaymentService : PaymentService {
    override fun processPayment(customerId: String, amount: Double, authToken: String): PaymentResult {
        val txnId = "TXN-${UUID.randomUUID().toString().take(8).uppercase()}"
        return PaymentResult(
            isSuccess = true,
            transactionId = txnId,
            amountCharged = amount,
            message = "Payment of $${"%.2f".format(amount)} successfully charged for customer $customerId."
        )
    }
}
