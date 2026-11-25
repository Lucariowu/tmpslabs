package lab3.patterns.state

/**
 * State Pattern: State interface.
 * Defines the contract for all state-dependent lifecycle operations on an order.
 */
interface OrderState {
    val stateName: String

    fun pay(context: OrderContext)
    fun ship(context: OrderContext, trackingNumber: String)
    fun deliver(context: OrderContext)
    fun cancel(context: OrderContext, reason: String)
}
