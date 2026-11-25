package lab3.patterns.observer

/**
 * Observer Pattern: Observer interface.
 * Implemented by any subscriber that needs to react to order lifecycle state changes.
 */
interface OrderObserver {
    val observerName: String
    fun onOrderStateChanged(orderId: String, previousState: String, newState: String, details: String)
}
