package lab2

class Order(
    val orderId: String,
    val customerName: String,
    val items: List<Book>,
    val discountPercent: Double,
    val isGiftWrapped: Boolean,
    val deliveryAddress: String?,
    val note: String?
) {
    fun calculateTotal(): Double {
        val subtotal = items.sumOf { it.price }
        val discounted = subtotal * (1.0 - discountPercent / 100.0)
        val giftCost = if (isGiftWrapped) 5.0 else 0.0
        val shippingCost = if (deliveryAddress != null) 10.0 else 0.0
        return discounted + giftCost + shippingCost
    }

    fun printSummary() {
        println("=== Order Summary (#$orderId) ===")
        println("Customer: $customerName")
        println("Items:")
        items.forEachIndexed { idx, book ->
            println("  ${idx + 1}. ${book.getDetails()}")
        }
        val subtotal = items.sumOf { it.price }
        println("Subtotal: $${"%.2f".format(subtotal)}")
        if (discountPercent > 0) {
            println("Discount: ${discountPercent.toInt()}%")
        }
        if (isGiftWrapped) {
            println("Gift Wrapping: Yes (+$5.00)")
        }
        if (deliveryAddress != null) {
            println("Delivery Address: $deliveryAddress (+$10.00)")
        } else {
            println("Delivery: Store Pickup (Free)")
        }
        note?.let { println("Note: $it") }
        println("Total Amount: $${"%.2f".format(calculateTotal())}")
        println("=================================")
    }
}

class OrderBuilder(private val orderId: String) {
    private var customerName: String = "Guest"
    private val items = mutableListOf<Book>()
    private var discountPercent: Double = 0.0
    private var isGiftWrapped: Boolean = false
    private var deliveryAddress: String? = null
    private var note: String? = null

    fun forCustomer(name: String): OrderBuilder {
        this.customerName = name
        return this
    }

    fun addItem(book: Book): OrderBuilder {
        this.items.add(book)
        return this
    }

    fun applyDiscount(percentage: Double): OrderBuilder {
        this.discountPercent = percentage
        return this
    }

    fun withGiftWrapping(enabled: Boolean): OrderBuilder {
        this.isGiftWrapped = enabled
        return this
    }

    fun withDeliveryAddress(address: String?): OrderBuilder {
        this.deliveryAddress = address
        return this
    }

    fun withNote(note: String?): OrderBuilder {
        this.note = note
        return this
    }

    fun build(): Order {
        require(items.isNotEmpty()) { "Order must contain at least one book." }
        return Order(
            orderId = orderId,
            customerName = customerName,
            items = items.toList(),
            discountPercent = discountPercent,
            isGiftWrapped = isGiftWrapped,
            deliveryAddress = deliveryAddress,
            note = note
        )
    }
}
