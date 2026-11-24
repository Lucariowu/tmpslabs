package lab2.test

import lab2.domain.Book
import lab2.domain.PhysicalBook
import lab2.patterns.decorator.*
import lab2.patterns.facade.BookstoreFacade
import lab2.patterns.facade.CheckoutOptions
import lab2.patterns.proxy.PaymentServiceProxy
import lab2.patterns.proxy.RealPaymentService

object TestRunner {
    private var passed = 0
    private var failed = 0

    private fun test(name: String, block: () -> Unit) {
        print("Running: $name ... ")
        try {
            block()
            println("[PASSED]")
            passed++
        } catch (e: Throwable) {
            println("[FAILED]: ${e.message}")
            e.printStackTrace()
            failed++
        }
    }

    private fun assertEquals(expected: Any?, actual: Any?, message: String = "") {
        if (expected != actual) {
            throw AssertionError("$message | Expected: <$expected>, Actual: <$actual>")
        }
    }

    private fun assertTrue(condition: Boolean, message: String = "") {
        if (!condition) {
            throw AssertionError("Assertion failed: $message")
        }
    }

    @JvmStatic
    fun main(args: Array<String>) {
        println("==================================================")
        println("  LAB 2: STRUCTURAL DESIGN PATTERNS - TEST SUITE  ")
        println("==================================================\n")

        // ---------------- DECORATOR TESTS ----------------
        println("--- 1. DECORATOR PATTERN TESTS ---")

        test("testDecoratorsStackInAnyOrderWithIdenticalCost") {
            val book: Book = PhysicalBook("T1", "Test Book", "Author", 35.0, 0.5)

            // Order A: Base -> GiftWrap -> Express
            val orderA: OrderComponent = ExpressDeliveryDecorator(GiftWrappingDecorator(BaseOrder(book)))
            // Order B: Base -> Express -> GiftWrap
            val orderB: OrderComponent = GiftWrappingDecorator(ExpressDeliveryDecorator(BaseOrder(book)))

            val expectedCost = 35.0 + 5.0 + 15.0 // 55.0
            assertEquals(expectedCost, orderA.getCost(), "Order A cost mismatch")
            assertEquals(expectedCost, orderB.getCost(), "Order B cost mismatch")
            assertEquals(orderA.getCost(), orderB.getCost(), "Stacked order cost must be commutative")

            assertTrue(orderA.getDescription().contains("Gift Wrapping"), "Order A must include Gift Wrapping")
            assertTrue(orderA.getDescription().contains("Express Delivery"), "Order A must include Express Delivery")
        }

        test("testAllDecoratorsStackedTogether") {
            val book: Book = PhysicalBook("T2", "Kotlin Guide", "Author", 28.0, 0.4)
            val fullOrder: OrderComponent = ShippingInsuranceDecorator(
                ExpressDeliveryDecorator(
                    GiftWrappingDecorator(BaseOrder(book))
                )
            )

            val expectedCost = 28.0 + 5.0 + 15.0 + 3.5 // 51.50
            assertEquals(expectedCost, fullOrder.getCost(), "Full decorators cost mismatch")
            assertTrue(fullOrder.getDescription().contains("Shipping Insurance"), "Must contain Insurance")
            assertTrue(fullOrder.getDescription().contains("Gift Wrapping"), "Must contain Gift Wrapping")
            assertTrue(fullOrder.getDescription().contains("Express Delivery"), "Must contain Express Delivery")
        }

        // ---------------- PROXY TESTS ----------------
        println("\n--- 2. PROXY PATTERN TESTS ---")

        test("testProxyBlocksUnauthorizedAccess") {
            val proxy = PaymentServiceProxy(RealPaymentService())
            var exceptionThrown = false

            try {
                proxy.processPayment("CUST-99", 50.0, "INVALID-TOKEN-XYZ")
            } catch (e: SecurityException) {
                exceptionThrown = true
                assertTrue(e.message?.contains("Access Denied") == true, "Exception must indicate access denied")
            }

            assertTrue(exceptionThrown, "SecurityException must be thrown for invalid token")
            val auditLog = proxy.getAuditLog()
            assertTrue(auditLog.any { it.contains("[SECURITY BLOCKED]") && it.contains("INVALID-TOKEN-XYZ") },
                "Audit log must record the unauthorized attempt")
        }

        test("testProxyAllowsAuthorizedCallsAndPassesThrough") {
            val proxy = PaymentServiceProxy(RealPaymentService())
            val validToken = "AUTH-USER-001"

            val result = proxy.processPayment("CUST-01", 75.0, validToken)

            assertTrue(result.isSuccess, "Authorized payment must succeed")
            assertEquals(75.0, result.amountCharged, "Charged amount must match")
            assertTrue(result.transactionId.startsWith("TXN-"), "Transaction ID must be generated")

            val auditLog = proxy.getAuditLog()
            assertTrue(auditLog.any { it.contains("[SECURITY AUTHORIZED]") && it.contains("CUST-01") },
                "Audit log must record authorized delegation")
        }

        // ---------------- FACADE TESTS ----------------
        println("\n--- 3. FACADE PATTERN TESTS ---")

        test("testFacadeExecutesCoordinatedCheckoutSuccessfully") {
            val facade = BookstoreFacade()
            val receipt = facade.checkoutBook(
                bookId = "B101", // Clean Code: $35.0
                customerId = "ALICE-01",
                authToken = "AUTH-USER-001",
                options = CheckoutOptions(includeGiftWrapping = true, includeExpressDelivery = true)
            )

            assertTrue(receipt.isSuccess, "Checkout must succeed")
            assertEquals("Clean Code", receipt.bookTitle, "Book title must match catalog")
            assertEquals(55.0, receipt.finalCost, "Total cost must include $35 base + $5 gift + $15 express")
            assertTrue(receipt.itemizedDescription.contains("Gift Wrapping"), "Receipt must itemize Gift Wrapping")
            assertTrue(receipt.transactionId != null, "Transaction ID must be present")
        }

        test("testFacadeGracefullyHandlesSecurityProxyBlock") {
            val facade = BookstoreFacade()
            val receipt = facade.checkoutBook(
                bookId = "B101",
                customerId = "BOB-02",
                authToken = "MALICIOUS-TOKEN",
                options = CheckoutOptions(includeGiftWrapping = true)
            )

            assertTrue(!receipt.isSuccess, "Checkout must fail for invalid token")
            assertTrue(receipt.message.contains("Security Proxy Blocked"), "Message must explain security block")
            assertEquals(null, receipt.transactionId, "No transaction ID should exist on blocked order")
        }

        test("testFacadeHandlesNonExistentBook") {
            val facade = BookstoreFacade()
            val receipt = facade.checkoutBook(
                bookId = "NON-EXISTENT-ID",
                customerId = "ALICE-01",
                authToken = "AUTH-USER-001"
            )

            assertTrue(!receipt.isSuccess, "Checkout must fail for missing book")
            assertTrue(receipt.message.contains("not found in library catalog"), "Message must mention catalog lookup failure")
        }

        // ---------------- SUMMARY ----------------
        println("\n==================================================")
        println("TEST SUMMARY: $passed passed, $failed failed out of ${passed + failed} total.")
        println("==================================================")

        if (failed > 0) {
            System.exit(1)
        }
    }
}
