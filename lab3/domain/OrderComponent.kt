package lab3.domain

/**
 * Reused Structural Pattern (Decorator from Lab 2):
 * Allows dynamic composition of additional order services without modifying Book or BaseOrder.
 */
interface OrderComponent {
    fun getCost(): Double
    fun getDescription(): String
}

class BaseOrder(val book: Book) : OrderComponent {
    override fun getCost(): Double = book.price
    override fun getDescription(): String = "${book.title} ($${"%.2f".format(book.price)})"
}

abstract class OrderDecorator(protected val decoratedOrder: OrderComponent) : OrderComponent {
    override fun getCost(): Double = decoratedOrder.getCost()
    override fun getDescription(): String = decoratedOrder.getDescription()
}

class GiftWrappingDecorator(decoratedOrder: OrderComponent) : OrderDecorator(decoratedOrder) {
    private val fee = 5.0
    override fun getCost(): Double = decoratedOrder.getCost() + fee
    override fun getDescription(): String = "${decoratedOrder.getDescription()} + Gift Wrapping ($${"%.2f".format(fee)})"
}

class ExpressDeliveryDecorator(decoratedOrder: OrderComponent) : OrderDecorator(decoratedOrder) {
    private val fee = 15.0
    override fun getCost(): Double = decoratedOrder.getCost() + fee
    override fun getDescription(): String = "${decoratedOrder.getDescription()} + Express Delivery ($${"%.2f".format(fee)})"
}

class ShippingInsuranceDecorator(decoratedOrder: OrderComponent) : OrderDecorator(decoratedOrder) {
    private val fee = 3.5
    override fun getCost(): Double = decoratedOrder.getCost() + fee
    override fun getDescription(): String = "${decoratedOrder.getDescription()} + Shipping Insurance ($${"%.2f".format(fee)})"
}
