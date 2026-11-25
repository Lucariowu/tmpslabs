package lab3.patterns.command

import lab3.patterns.state.OrderContext
import lab3.patterns.state.OrderState
import lab3.patterns.strategy.DiscountStrategy

/**
 * Concrete Command: Processes payment for an order.
 * Receiver: OrderContext.
 */
class PayOrderCommand(
    private val order: OrderContext,
    val transactionId: String
) : OrderCommand {
    override val name: String = "PayOrderCommand"
    private var previousState: OrderState? = null

    override fun execute(): Boolean {
        previousState = order.state
        return try {
            order.pay()
            true
        } catch (e: Exception) {
            println("Failed to execute $name: ${e.message}")
            false
        }
    }

    override fun undo(): Boolean {
        val prevState = previousState ?: return false
        order.rollbackState(prevState, "Payment rolled back/refunded for txn $transactionId.")
        return true
    }

    override fun getDescription(): String = "Pay order '${order.orderId}' (Txn: $transactionId, Amount: $${"%.2f".format(order.getFinalCost())})"
}

/**
 * Concrete Command: Dispatches the order with a courier tracking number.
 * Receiver: OrderContext.
 */
class ShipOrderCommand(
    private val order: OrderContext,
    val trackingNumber: String
) : OrderCommand {
    override val name: String = "ShipOrderCommand"
    private var previousState: OrderState? = null
    private var previousTracking: String? = null

    override fun execute(): Boolean {
        previousState = order.state
        previousTracking = order.trackingNumber
        return try {
            order.ship(trackingNumber)
            true
        } catch (e: Exception) {
            println("Failed to execute $name: ${e.message}")
            false
        }
    }

    override fun undo(): Boolean {
        val prevState = previousState ?: return false
        order.trackingNumber = previousTracking
        order.rollbackState(prevState, "Shipment cancelled: Package recalled from courier.")
        return true
    }

    override fun getDescription(): String = "Ship order '${order.orderId}' (Tracking: $trackingNumber)"
}

/**
 * Concrete Command: Cancels the order.
 * Receiver: OrderContext.
 */
class CancelOrderCommand(
    private val order: OrderContext,
    val reason: String
) : OrderCommand {
    override val name: String = "CancelOrderCommand"
    private var previousState: OrderState? = null
    private var previousReason: String? = null

    override fun execute(): Boolean {
        previousState = order.state
        previousReason = order.cancellationReason
        return try {
            order.cancel(reason)
            true
        } catch (e: Exception) {
            println("Failed to execute $name: ${e.message}")
            false
        }
    }

    override fun undo(): Boolean {
        val prevState = previousState ?: return false
        order.cancellationReason = previousReason
        order.rollbackState(prevState, "Cancellation reversed: Order reinstated to $prevState.")
        return true
    }

    override fun getDescription(): String = "Cancel order '${order.orderId}' (Reason: $reason)"
}

/**
 * Concrete Command: Applies a new discount strategy to the order.
 * Receiver: OrderContext.
 */
class ApplyDiscountCommand(
    private val order: OrderContext,
    val newStrategy: DiscountStrategy
) : OrderCommand {
    override val name: String = "ApplyDiscountCommand"
    private var previousStrategy: DiscountStrategy? = null

    override fun execute(): Boolean {
        previousStrategy = order.discountStrategy
        order.discountStrategy = newStrategy
        return true
    }

    override fun undo(): Boolean {
        val prev = previousStrategy ?: return false
        order.discountStrategy = prev
        return true
    }

    override fun getDescription(): String = "Apply strategy '${newStrategy.name}' to order '${order.orderId}'"
}
