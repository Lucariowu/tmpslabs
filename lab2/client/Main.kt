package lab2.client

import lab2.domain.LibraryCatalog
import lab2.patterns.facade.BookstoreFacade
import lab2.patterns.facade.CheckoutOptions
import lab2.patterns.proxy.PaymentServiceProxy
import lab2.patterns.proxy.RealPaymentService
import lab2.test.TestRunner
import java.util.Scanner

fun main() {
    val scanner = Scanner(System.`in`)
    val facade = BookstoreFacade()
    val paymentProxy = PaymentServiceProxy(RealPaymentService())

    println("=========================================================")
    println("  BOOKSTORE SYSTEM - LAB 2: STRUCTURAL DESIGN PATTERNS   ")
    println("=========================================================")

    while (true) {
        println("\n--- MAIN MENU ---")
        println("1. View Library Catalog")
        println("2. Checkout with Custom Add-ons (Facade + Decorator + Proxy)")
        println("3. Test Payment Security Proxy (Valid vs Invalid Token)")
        println("4. Run Automated Unit Test Suite")
        println("5. Exit")
        print("Select an option (1-5): ")

        when (scanner.nextLine().trim()) {
            "1" -> {
                val books = LibraryCatalog.getAllBooks()
                println("\n=== AVAILABLE BOOKS IN CATALOG ===")
                books.forEach { book ->
                    println(" [${book.id}] ${book.getDetails()}")
                }
            }

            "2" -> {
                val books = LibraryCatalog.getAllBooks()
                println("\n=== AVAILABLE BOOKS ===")
                books.forEach { book ->
                    println(" [${book.id}] ${book.title} ($${"%.2f".format(book.price)})")
                }

                print("\nEnter Book ID to purchase: ")
                val bookId = scanner.nextLine().trim()

                print("Enter your Customer ID (e.g. CUST-42): ")
                val customerId = scanner.nextLine().trim().ifBlank { "CUST-DEFAULT" }

                println("\nAuthentication Tokens:")
                println(" - Valid Tokens: AUTH-USER-001, AUTH-VIP-777, AUTH-STUDENT-42")
                println(" - (Or enter any other string to test security rejection)")
                print("Enter Auth Token [Default: AUTH-USER-001]: ")
                val inputToken = scanner.nextLine().trim()
                val authToken = if (inputToken.isBlank()) "AUTH-USER-001" else inputToken

                println("\n--- SELECT DECORATORS (ADD-ONS) ---")
                print("1. Include Gift Wrapping (+$5.00)? (y/N): ")
                val giftWrap = scanner.nextLine().trim().equals("y", ignoreCase = true)

                print("2. Include Express Delivery (+$15.00)? (y/N): ")
                val express = scanner.nextLine().trim().equals("y", ignoreCase = true)

                print("3. Include Shipping Insurance (+$3.50)? (y/N): ")
                val insurance = scanner.nextLine().trim().equals("y", ignoreCase = true)

                val options = CheckoutOptions(
                    includeGiftWrapping = giftWrap,
                    includeExpressDelivery = express,
                    includeInsurance = insurance
                )

                println("\n... Processing Checkout via BookstoreFacade ...")
                val receipt = facade.checkoutBook(bookId, customerId, authToken, options)

                println("\n================ ORDER RECEIPT ================")
                println("Status:       ${if (receipt.isSuccess) "SUCCESS" else "FAILED"}")
                println("Book:         ${receipt.bookTitle}")
                println("Total Cost:   $${"%.2f".format(receipt.finalCost)}")
                println("Details:      ${receipt.itemizedDescription}")
                if (receipt.transactionId != null) {
                    println("Txn ID:       ${receipt.transactionId}")
                }
                println("Message:      ${receipt.message}")
                println("===============================================")
            }

            "3" -> {
                println("\n--- DIRECT PROXY TEST ---")
                print("Enter Auth Token (try 'AUTH-USER-001' for success or 'HACKER-123' for rejection): ")
                val token = scanner.nextLine().trim().ifBlank { "AUTH-USER-001" }

                print("Enter Amount: ")
                val amount = scanner.nextLine().trim().toDoubleOrNull() ?: 50.0

                try {
                    val result = paymentProxy.processPayment("CUST-DEMO", amount, token)
                    println("\n[PROXY SUCCESS]: ${result.message} (Txn: ${result.transactionId})")
                } catch (e: SecurityException) {
                    println("\n[PROXY REJECTED]: ${e.message}")
                } catch (e: Exception) {
                    println("\n[ERROR]: ${e.message}")
                }

                println("\nProxy Audit Log:")
                paymentProxy.getAuditLog().takeLast(3).forEach { println("  $it") }
            }

            "4" -> {
                TestRunner.main(emptyArray())
            }

            "5" -> {
                println("Exiting Bookstore System. Goodbye!")
                break
            }

            else -> {
                println("Invalid option. Please enter a number between 1 and 5.")
            }
        }
    }
}
