package lab3.test

import lab3.domain.BaseOrder
import lab3.domain.Book
import lab3.domain.ExpressDeliveryDecorator
import lab3.domain.GiftWrappingDecorator
import lab3.domain.PhysicalBook
import lab3.patterns.command.*
import lab3.patterns.observer.AnalyticsAuditObserver
import lab3.patterns.observer.CustomerEmailNotificationObserver
import lab3.patterns.observer.OrderObserver
import lab3.patterns.observer.WarehouseFulfillmentObserver
import lab3.patterns.state.*
import lab3.patterns.strategy.*

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

    private fun assertFailsWith(expectedType: Class<out Throwable>, message: String = "", block: () -> Unit) {
        try {
            block()
            throw AssertionError("Expected exception ${expectedType.name} was not thrown. $message")
        } catch (t: Throwable) {
            if (!expectedType.isInstance(t)) {
                throw AssertionError("Expected exception ${expectedType.name}, but caught ${t.javaClass.name}: ${t.message}")
            }
        }
    }

    @JvmStatic
    fun main(args: Array<String>) {
        println("==================================================")
        println("  LAB 3: BEHAVIORAL DESIGN PATTERNS - TEST SUITE  ")
        println("==================================================\n")

        // ---------------- 1. STRATEGY PATTERN TESTS ----------------
        println("--- 1. STRATEGY PATTERN TESTS ---")

        test("testStrategyCalculatesCorrectDiscounts") {
            val rawCost = 100.0

            val regular = RegularPricingStrategy()
            val student = StudentDiscountStrategy()
            val vip = VipLoyaltyStrategy()
            val coupon = SeasonalCouponStrategy("SAVE10", 10.0)

            assertEquals(100.0, regular.applyDiscount(rawCost), "Regular pricing should not discount")
            assertEquals(85.0, student.applyDiscount(rawCost), "Student pricing should be 15% off")
            assertEquals(75.0, vip.applyDiscount(rawCost), "VIP pricing should be 25% off")
            assertEquals(90.0, coupon.applyDiscount(rawCost), "Coupon should subtract $10")

            // Test non-negative boundary
            val bigCoupon = SeasonalCouponStrategy("BIG50", 50.0)
            assertEquals(0.0, bigCoupon.applyDiscount(30.0), "Coupon should not reduce price below 0.0")
        }

        test("testStrategyCanBeSwappedAtRuntime") {
            val book = PhysicalBook("B1", "Clean Architecture", "Robert C. Martin", 40.0, 0.6)
            val order = OrderContext("ORD-STRAT", "CUST-1", "user@test.com", BaseOrder(book), RegularPricingStrategy())

            assertEquals(40.0, order.getFinalCost(), "Initial regular price mismatch")

            // Swap to Student Strategy at runtime
            order.discountStrategy = StudentDiscountStrategy()
            assertEquals(34.0, order.getFinalCost(), "Price after runtime swap to Student strategy mismatch")

            // Swap to VIP Strategy at runtime
            order.discountStrategy = VipLoyaltyStrategy()
            assertEquals(30.0, order.getFinalCost(), "Price after runtime swap to VIP strategy mismatch")
        }

        // ---------------- 2. STATE PATTERN TESTS ----------------
        println("\n--- 2. STATE PATTERN TESTS ---")

        test("testValidStateTransitions") {
            val book = PhysicalBook("B2", "Design Patterns", "GoF", 50.0, 0.8)
            val order = OrderContext("ORD-STATE-1", "CUST-2", "gof@test.com", BaseOrder(book))

            assertEquals("PENDING_PAYMENT", order.state.stateName)

            // Pay
            order.pay()
            assertEquals("PROCESSING", order.state.stateName)

            // Ship
            order.ship("TRACK-999")
            assertEquals("SHIPPED", order.state.stateName)
            assertEquals("TRACK-999", order.trackingNumber)

            // Deliver
            order.deliver()
            assertEquals("DELIVERED", order.state.stateName)
        }

        test("testForbiddenStateTransitionsThrowExceptions") {
            val book = PhysicalBook("B2", "Design Patterns", "GoF", 50.0, 0.8)
            val order = OrderContext("ORD-STATE-2", "CUST-3", "gof@test.com", BaseOrder(book))

            // In PENDING_PAYMENT, cannot ship or deliver
            assertFailsWith(IllegalStateException::class.java, "Cannot ship an unpaid order") {
                order.ship("TRACK-FAIL")
            }
            assertFailsWith(IllegalStateException::class.java, "Cannot deliver an unpaid order") {
                order.deliver()
            }

            // Move to PROCESSING
            order.pay()

            // In PROCESSING, cannot pay again or deliver directly
            assertFailsWith(IllegalStateException::class.java, "Cannot pay an already paid order") {
                order.pay()
            }
            assertFailsWith(IllegalStateException::class.java, "Cannot deliver unshipped order") {
                order.deliver()
            }
        }

        test("testCannotCancelShippedOrDeliveredOrder") {
            val book = PhysicalBook("B3", "Kotlin", "Author", 30.0, 0.5)
            val order = OrderContext("ORD-STATE-3", "CUST-4", "test@test.com", BaseOrder(book))

            order.pay()
            order.ship("TRACK-EXP-1")

            // Cannot cancel once shipped
            assertFailsWith(IllegalStateException::class.java, "Cannot cancel shipped order") {
                order.cancel("Customer changed mind")
            }

            order.deliver()

            // Cannot cancel once delivered
            assertFailsWith(IllegalStateException::class.java, "Cannot cancel delivered order") {
                order.cancel("Return request")
            }
        }

        // ---------------- 3. OBSERVER PATTERN TESTS ----------------
        println("\n--- 3. OBSERVER PATTERN TESTS ---")

        test("testAllSubscribedObserversAreNotified") {
            val book = PhysicalBook("B4", "Refactoring", "Fowler", 35.0, 0.5)
            val order = OrderContext("ORD-OBS-1", "CUST-5", "fowler@test.com", BaseOrder(book))

            val customerObserver = CustomerEmailNotificationObserver("fowler@test.com")
            val warehouseObserver = WarehouseFulfillmentObserver()
            val auditObserver = AnalyticsAuditObserver()

            order.attach(customerObserver)
            order.attach(warehouseObserver)
            order.attach(auditObserver)

            order.pay()

            assertEquals(1, customerObserver.getReceivedMessages().size, "Customer should receive 1 notification")
            assertTrue(customerObserver.getReceivedMessages().first().contains("PROCESSING"), "Notification mentions PROCESSING")

            assertEquals(1, warehouseObserver.getPackingLogs().size, "Warehouse should receive 1 packing log")
            assertTrue(warehouseObserver.getPackingLogs().first().contains("Picking items"), "Warehouse initiates picking")

            assertEquals(1, auditObserver.getAuditRecords().size, "Audit log should have 1 entry")
            assertTrue(auditObserver.getAuditRecords().first().contains("PENDING_PAYMENT -> PROCESSING"), "Audit records transition")
        }

        test("testUnsubscribedObserversDoNotReceiveUpdates") {
            val book = PhysicalBook("B4", "Refactoring", "Fowler", 35.0, 0.5)
            val order = OrderContext("ORD-OBS-2", "CUST-6", "user@test.com", BaseOrder(book))

            val customerObserver = CustomerEmailNotificationObserver("user@test.com")
            val warehouseObserver = WarehouseFulfillmentObserver()

            order.attach(customerObserver)
            order.attach(warehouseObserver)

            order.pay()
            assertEquals(1, customerObserver.getReceivedMessages().size)
            assertEquals(1, warehouseObserver.getPackingLogs().size)

            // Detach customer observer
            order.detach(customerObserver)

            // Ship the order
            order.ship("TRACK-777")

            // Customer should NOT receive second update
            assertEquals(1, customerObserver.getReceivedMessages().size, "Detached observer should not receive updates")

            // Warehouse remains attached and receives second update
            assertEquals(2, warehouseObserver.getPackingLogs().size, "Attached observer continues to receive updates")
            assertTrue(warehouseObserver.getPackingLogs().last().contains("courier"), "Warehouse logs courier pickup")
        }

        // ---------------- 4. COMMAND PATTERN TESTS ----------------
        println("\n--- 4. COMMAND PATTERN TESTS ---")

        test("testCommandExecutionAndUndoRestoresState") {
            val book = PhysicalBook("B1", "Clean Architecture", "Robert C. Martin", 40.0, 0.6)
            val order = OrderContext("ORD-CMD-1", "CUST-7", "bob@test.com", BaseOrder(book))
            val invoker = OrderCommandInvoker()

            val payCmd = PayOrderCommand(order, "TXN-8888")
            val success = invoker.executeCommand(payCmd)

            assertTrue(success, "Command execution must succeed")
            assertEquals("PROCESSING", order.state.stateName, "Order must transition to PROCESSING")
            assertEquals(1, invoker.getHistory().size, "Invoker history must contain executed command")

            // Undo payment
            val undoSuccess = invoker.undo()
            assertTrue(undoSuccess, "Undo must succeed")
            assertEquals("PENDING_PAYMENT", order.state.stateName, "State must be safely rolled back to PENDING_PAYMENT")
        }

        test("testCommandRedoAndApplyDiscountUndo") {
            val book = PhysicalBook("B1", "Clean Architecture", "Robert C. Martin", 40.0, 0.6)
            val order = OrderContext("ORD-CMD-2", "CUST-8", "bob@test.com", BaseOrder(book))
            val invoker = OrderCommandInvoker()

            val discountCmd = ApplyDiscountCommand(order, VipLoyaltyStrategy())
            invoker.executeCommand(discountCmd)

            assertEquals(30.0, order.getFinalCost(), "VIP discount should reduce price to 30.0")

            // Undo discount command
            invoker.undo()
            assertEquals(40.0, order.getFinalCost(), "Undoing discount command must restore regular price")

            // Redo discount command
            invoker.redo()
            assertEquals(30.0, order.getFinalCost(), "Redoing discount command must reapply VIP discount")
        }

        // ---------------- 5. PATTERN INTERACTION TEST ----------------
        println("\n--- 5. PATTERN INTERACTION TEST ---")

        test("testIntegratedWorkflowStrategyCommandStateObserver") {
            // Reusing Creational (Catalog) and Structural (Decorators) from Lab 2
            val book = PhysicalBook("B2", "Design Patterns", "GoF", 50.0, 0.8)
            val decoratedOrder = ExpressDeliveryDecorator(GiftWrappingDecorator(BaseOrder(book)))
            // Raw price: 50.0 + 5.0 (gift) + 15.0 (express) = 70.0

            val order = OrderContext("ORD-INTEG-1", "CUST-VIP", "vip@test.com", decoratedOrder)
            val invoker = OrderCommandInvoker()

            val customerObserver = CustomerEmailNotificationObserver("vip@test.com")
            val warehouseObserver = WarehouseFulfillmentObserver()
            val auditObserver = AnalyticsAuditObserver()

            order.attach(customerObserver)
            order.attach(warehouseObserver)
            order.attach(auditObserver)

            // Step 1: Strategy applied via Command
            invoker.executeCommand(ApplyDiscountCommand(order, StudentDiscountStrategy()))
            // 70.0 * 0.85 = 59.50
            assertEquals(59.50, order.getFinalCost(), "Strategy discount calculation mismatch")

            // Step 2: Pay command alters State and triggers Observers
            invoker.executeCommand(PayOrderCommand(order, "TXN-INTEG-99"))
            assertEquals("PROCESSING", order.state.stateName)
            assertEquals(1, customerObserver.getReceivedMessages().size)
            assertEquals(1, warehouseObserver.getPackingLogs().size)
            assertEquals(1, auditObserver.getAuditRecords().size)

            // Step 3: Ship command alters State to Shipped and notifies Observers
            invoker.executeCommand(ShipOrderCommand(order, "DHL-EXPRESS-12345"))
            assertEquals("SHIPPED", order.state.stateName)
            assertEquals(2, customerObserver.getReceivedMessages().size)
            assertEquals(2, warehouseObserver.getPackingLogs().size)

            // Step 4: Undo Ship command rolls back state to Processing and notifies Observers
            invoker.undo()
            assertEquals("PROCESSING", order.state.stateName)
            assertEquals(3, customerObserver.getReceivedMessages().size)
            assertTrue(customerObserver.getReceivedMessages().last().contains("SHIPPED -> PROCESSING"))

            // Step 5: Deliver directly through context
            order.ship("DHL-RE-DISPATCH")
            order.deliver()
            assertEquals("DELIVERED", order.state.stateName)
        }

        // ---------------- 6. OPEN-CLOSED PRINCIPLE EXTENSION TEST ----------------
        println("\n--- 6. OPEN-CLOSED PRINCIPLE EXTENSION TEST ---")

        test("testOpenForExtensionAllowsNewObserverAndStrategyWithoutModifyingExistingCode") {
            // Define a brand new observer without altering any existing class
            val customSmsLogs = mutableListOf<String>()
            val smsObserver = object : OrderObserver {
                override val observerName: String = "SMS Notification Service"
                override fun onOrderStateChanged(orderId: String, previousState: String, newState: String, details: String) {
                    customSmsLogs.add("SMS to +1-555-0199: Order $orderId is now $newState")
                }
            }

            // Define a brand new strategy without altering any existing class
            val blackFridayStrategy = object : DiscountStrategy {
                override val name: String = "Black Friday 50% Off"
                override val description: String = "Half-price blowout sale"
                override fun applyDiscount(rawTotal: Double): Double = rawTotal * 0.50
            }

            val book = PhysicalBook("B1", "Clean Architecture", "Robert C. Martin", 40.0, 0.6)
            val order = OrderContext("ORD-EXT-1", "CUST-OCP", "ocp@test.com", BaseOrder(book), blackFridayStrategy)
            order.attach(smsObserver)

            assertEquals(20.0, order.getFinalCost(), "New strategy calculates 50% correctly")

            order.pay()
            assertEquals(1, customSmsLogs.size, "New observer receives update smoothly")
            assertTrue(customSmsLogs.first().contains("SMS to +1-555-0199: Order ORD-EXT-1 is now PROCESSING"))
        }

        println("\n==================================================")
        println("TEST SUMMARY: $passed passed, $failed failed out of ${passed + failed} total.")
        println("==================================================")

        if (failed > 0) {
            throw RuntimeException("$failed tests failed!")
        }
    }
}
