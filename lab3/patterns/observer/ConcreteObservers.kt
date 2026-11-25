package lab3.patterns.observer

/**
 * Concrete Observer 1: Sends email status notifications to the customer.
 */
class CustomerEmailNotificationObserver(
    val customerEmail: String
) : OrderObserver {
    override val observerName: String = "Customer Email Notification ($customerEmail)"
    private val receivedMessages = mutableListOf<String>()

    fun getReceivedMessages(): List<String> = receivedMessages.toList()
    fun clearMessages() = receivedMessages.clear()

    override fun onOrderStateChanged(orderId: String, previousState: String, newState: String, details: String) {
        val message = "[EMAIL to $customerEmail] Order '$orderId' changed: $previousState -> $newState. Notes: $details"
        receivedMessages.add(message)
    }
}

/**
 * Concrete Observer 2: Triggers warehouse logistics and inventory packing actions.
 */
class WarehouseFulfillmentObserver : OrderObserver {
    override val observerName: String = "Warehouse Logistics & Fulfillment"
    private val packingLogs = mutableListOf<String>()

    fun getPackingLogs(): List<String> = packingLogs.toList()
    fun clearLogs() = packingLogs.clear()

    override fun onOrderStateChanged(orderId: String, previousState: String, newState: String, details: String) {
        val action = when (newState) {
            "PROCESSING" -> "[WAREHOUSE ACTION] Order '$orderId' paid. Picking items from shelves and printing shipping label."
            "SHIPPED" -> "[WAREHOUSE ACTION] Order '$orderId' picked up by courier. Tracking details: $details."
            "DELIVERED" -> "[WAREHOUSE ACTION] Order '$orderId' confirmed delivered to customer doorstep."
            "CANCELLED" -> "[WAREHOUSE ACTION] Order '$orderId' cancelled. Restocking items back to library warehouse."
            else -> "[WAREHOUSE LOG] Order '$orderId' status updated to $newState."
        }
        packingLogs.add(action)
    }
}

/**
 * Concrete Observer 3: Records persistent audit records for business intelligence and SLA compliance.
 */
class AnalyticsAuditObserver : OrderObserver {
    override val observerName: String = "Analytics & Audit Monitor"
    private val auditRecords = mutableListOf<String>()

    fun getAuditRecords(): List<String> = auditRecords.toList()
    fun clearRecords() = auditRecords.clear()

    override fun onOrderStateChanged(orderId: String, previousState: String, newState: String, details: String) {
        val timestamp = System.currentTimeMillis()
        val record = "[AUDIT @ $timestamp] OrderId: $orderId | Transition: $previousState -> $newState | Info: $details"
        auditRecords.add(record)
    }
}
