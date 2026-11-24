package lab2.patterns.proxy

class PaymentServiceProxy(
    private val realService: PaymentService = RealPaymentService()
) : PaymentService {
    private val validTokens = mutableSetOf("AUTH-USER-001", "AUTH-VIP-777", "AUTH-STUDENT-42")
    private val auditLog = mutableListOf<String>()

    fun registerToken(token: String) {
        validTokens.add(token)
    }

    fun getAuditLog(): List<String> = auditLog.toList()

    override fun processPayment(customerId: String, amount: Double, authToken: String): PaymentResult {
        if (!validTokens.contains(authToken)) {
            val logEntry = "[SECURITY BLOCKED] Unauthorized attempt for customer '$customerId' with invalid token '$authToken'. Amount: $${"%.2f".format(amount)}"
            auditLog.add(logEntry)
            throw SecurityException("Access Denied: Invalid or revoked authentication token '$authToken'.")
        }

        if (amount <= 0.0) {
            val logEntry = "[INVALID AMOUNT] Payment rejected for customer '$customerId': amount must be positive ($amount)."
            auditLog.add(logEntry)
            throw IllegalArgumentException("Payment amount must be greater than zero.")
        }

        val logEntry = "[SECURITY AUTHORIZED] Token verified for customer '$customerId'. Forwarding payment of $${"%.2f".format(amount)} to RealPaymentService."
        auditLog.add(logEntry)
        return realService.processPayment(customerId, amount, authToken)
    }
}
