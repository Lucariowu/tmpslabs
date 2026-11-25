package lab3.patterns.state

/**
 * Concrete State 1: Order created, awaiting payment.
 */
class PendingPaymentState : OrderState {
    override val stateName: String = "PENDING_PAYMENT"

    override fun pay(context: OrderContext) {
        context.setState(ProcessingState(), "Payment authorized and verified.")
    }

    override fun ship(context: OrderContext, trackingNumber: String) {
        throw IllegalStateException("Cannot ship order '${context.orderId}': Payment has not been received yet.")
    }

    override fun deliver(context: OrderContext) {
        throw IllegalStateException("Cannot deliver order '${context.orderId}': Order has not been paid or shipped.")
    }

    override fun cancel(context: OrderContext, reason: String) {
        context.cancellationReason = reason
        context.setState(CancelledState(), "Cancelled prior to payment: $reason")
    }
}

/**
 * Concrete State 2: Order paid, in warehouse fulfillment.
 */
class ProcessingState : OrderState {
    override val stateName: String = "PROCESSING"

    override fun pay(context: OrderContext) {
        throw IllegalStateException("Cannot pay for order '${context.orderId}': Payment has already been completed.")
    }

    override fun ship(context: OrderContext, trackingNumber: String) {
        if (trackingNumber.isBlank()) {
            throw IllegalArgumentException("Tracking number cannot be blank.")
        }
        context.trackingNumber = trackingNumber
        context.setState(ShippedState(trackingNumber), "Handed over to carrier. Tracking: $trackingNumber")
    }

    override fun deliver(context: OrderContext) {
        throw IllegalStateException("Cannot deliver order '${context.orderId}': Package is still being processed in the warehouse.")
    }

    override fun cancel(context: OrderContext, reason: String) {
        context.cancellationReason = reason
        context.setState(CancelledState(), "Cancelled during warehouse processing. Refund initiated: $reason")
    }
}

/**
 * Concrete State 3: Order shipped, in transit.
 */
class ShippedState(val trackingNumber: String) : OrderState {
    override val stateName: String = "SHIPPED"

    override fun pay(context: OrderContext) {
        throw IllegalStateException("Cannot pay for order '${context.orderId}': Order is already paid and in transit.")
    }

    override fun ship(context: OrderContext, trackingNumber: String) {
        throw IllegalStateException("Cannot ship order '${context.orderId}': Order is already shipped with tracking #${this.trackingNumber}.")
    }

    override fun deliver(context: OrderContext) {
        context.setState(DeliveredState(), "Package successfully delivered to customer doorstep.")
    }

    override fun cancel(context: OrderContext, reason: String) {
        throw IllegalStateException("Cannot cancel order '${context.orderId}': Package is already in transit with courier ($trackingNumber). Return process must be initiated after delivery.")
    }
}

/**
 * Concrete State 4: Order delivered (Terminal state).
 */
class DeliveredState : OrderState {
    override val stateName: String = "DELIVERED"

    override fun pay(context: OrderContext) {
        throw IllegalStateException("Order '${context.orderId}' is already delivered and closed.")
    }

    override fun ship(context: OrderContext, trackingNumber: String) {
        throw IllegalStateException("Order '${context.orderId}' is already delivered.")
    }

    override fun deliver(context: OrderContext) {
        throw IllegalStateException("Order '${context.orderId}' has already been marked as delivered.")
    }

    override fun cancel(context: OrderContext, reason: String) {
        throw IllegalStateException("Order '${context.orderId}' is already delivered. Must open a standard return request.")
    }
}

/**
 * Concrete State 5: Order cancelled (Terminal state).
 */
class CancelledState : OrderState {
    override val stateName: String = "CANCELLED"

    override fun pay(context: OrderContext) {
        throw IllegalStateException("Cannot pay for cancelled order '${context.orderId}'.")
    }

    override fun ship(context: OrderContext, trackingNumber: String) {
        throw IllegalStateException("Cannot ship cancelled order '${context.orderId}'.")
    }

    override fun deliver(context: OrderContext) {
        throw IllegalStateException("Cannot deliver cancelled order '${context.orderId}'.")
    }

    override fun cancel(context: OrderContext, reason: String) {
        throw IllegalStateException("Order '${context.orderId}' is already cancelled.")
    }
}
