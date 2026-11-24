package lab2.patterns.facade

import lab2.domain.LibraryCatalog
import lab2.patterns.decorator.*
import lab2.patterns.proxy.PaymentService
import lab2.patterns.proxy.PaymentServiceProxy

data class CheckoutOptions(
    val includeGiftWrapping: Boolean = false,
    val includeExpressDelivery: Boolean = false,
    val includeInsurance: Boolean = false
)

data class OrderReceipt(
    val isSuccess: Boolean,
    val bookTitle: String,
    val finalCost: Double,
    val itemizedDescription: String,
    val transactionId: String?,
    val message: String
)

class BookstoreFacade(
    private val paymentService: PaymentService = PaymentServiceProxy()
) {
    fun checkoutBook(
        bookId: String,
        customerId: String,
        authToken: String,
        options: CheckoutOptions = CheckoutOptions()
    ): OrderReceipt {
        val book = LibraryCatalog.getBookById(bookId)
            ?: return OrderReceipt(
                isSuccess = false,
                bookTitle = "Unknown",
                finalCost = 0.0,
                itemizedDescription = "N/A",
                transactionId = null,
                message = "Book with ID '$bookId' not found in library catalog."
            )

        // 1. Structural Pattern: Base Component
        var order: OrderComponent = BaseOrder(book)

        // 2. Structural Pattern: Decorator wrapping (can be applied in any combination)
        if (options.includeGiftWrapping) {
            order = GiftWrappingDecorator(order)
        }
        if (options.includeExpressDelivery) {
            order = ExpressDeliveryDecorator(order)
        }
        if (options.includeInsurance) {
            order = ShippingInsuranceDecorator(order)
        }

        val totalCost = order.getCost()
        val description = order.getDescription()

        // 3. Structural Pattern: Proxy delegation (Security, Validation, Audit)
        return try {
            val paymentResult = paymentService.processPayment(customerId, totalCost, authToken)
            OrderReceipt(
                isSuccess = true,
                bookTitle = book.title,
                finalCost = totalCost,
                itemizedDescription = description,
                transactionId = paymentResult.transactionId,
                message = paymentResult.message
            )
        } catch (e: SecurityException) {
            OrderReceipt(
                isSuccess = false,
                bookTitle = book.title,
                finalCost = totalCost,
                itemizedDescription = description,
                transactionId = null,
                message = "Security Proxy Blocked: ${e.message}"
            )
        } catch (e: Exception) {
            OrderReceipt(
                isSuccess = false,
                bookTitle = book.title,
                finalCost = totalCost,
                itemizedDescription = description,
                transactionId = null,
                message = "Payment failed: ${e.message}"
            )
        }
    }
}
