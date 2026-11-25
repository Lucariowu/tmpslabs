package lab3.patterns.state

import lab3.domain.OrderComponent
import lab3.patterns.observer.OrderObserver
import lab3.patterns.observer.OrderSubject
import lab3.patterns.strategy.DiscountStrategy
import lab3.patterns.strategy.RegularPricingStrategy

/**
 * State Pattern: Context class.
 * Also acts as the Subject in the Observer pattern, and Receiver in the Command pattern.
 */
class OrderContext(
    val orderId: String,
    val customerId: String,
    val customerEmail: String,
    val item: OrderComponent,
    initialStrategy: DiscountStrategy = RegularPricingStrategy()
) : OrderSubject {

    var discountStrategy: DiscountStrategy = initialStrategy
    var state: OrderState = PendingPaymentState()
        private set

    var trackingNumber: String? = null
    var cancellationReason: String? = null

    private val observers = mutableListOf<OrderObserver>()

    // --- State Pattern Delegations ---

    fun setState(newState: OrderState, details: String = "") {
        val previousStateName = state.stateName
        state = newState
        notifyObservers(previousStateName, newState.stateName, details)
    }

    /**
     * Internal transition helper, useful when undoing commands or setting exact state.
     */
    fun rollbackState(targetState: OrderState, details: String) {
        setState(targetState, details)
    }

    fun pay() {
        state.pay(this)
    }

    fun ship(trackingNumber: String) {
        state.ship(this, trackingNumber)
    }

    fun deliver() {
        state.deliver(this)
    }

    fun cancel(reason: String) {
        state.cancel(this, reason)
    }

    // --- Observer Pattern Implementation ---

    override fun attach(observer: OrderObserver) {
        if (!observers.contains(observer)) {
            observers.add(observer)
        }
    }

    override fun detach(observer: OrderObserver) {
        observers.remove(observer)
    }

    override fun notifyObservers(previousState: String, newState: String, details: String) {
        // Create defensive copy to prevent ConcurrentModificationException if an observer detaches during notification
        val observersCopy = ArrayList(observers)
        for (obs in observersCopy) {
            obs.onOrderStateChanged(orderId, previousState, newState, details)
        }
    }

    fun getAttachedObservers(): List<OrderObserver> = observers.toList()

    // --- Pricing & Summary ---

    fun getRawCost(): Double = item.getCost()

    fun getFinalCost(): Double = discountStrategy.applyDiscount(getRawCost())

    fun getSummary(): String {
        val sb = StringBuilder()
        sb.append("Order [$orderId] Summary:\n")
        sb.append(" - Customer: $customerId ($customerEmail)\n")
        sb.append(" - Items: ${item.getDescription()}\n")
        sb.append(" - Raw Cost: $${"%.2f".format(getRawCost())}\n")
        sb.append(" - Applied Strategy: ${discountStrategy.name} (${discountStrategy.description})\n")
        sb.append(" - Final Cost: $${"%.2f".format(getFinalCost())}\n")
        sb.append(" - Current State: ${state.stateName}\n")
        if (trackingNumber != null) {
            sb.append(" - Tracking Number: $trackingNumber\n")
        }
        if (cancellationReason != null) {
            sb.append(" - Cancellation Reason: $cancellationReason\n")
        }
        sb.append(" - Active Observers: ${observers.joinToString { it.observerName }}")
        return sb.toString()
    }
}
