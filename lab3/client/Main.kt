package lab3.client

import lab3.domain.*
import lab3.patterns.command.*
import lab3.patterns.observer.AnalyticsAuditObserver
import lab3.patterns.observer.CustomerEmailNotificationObserver
import lab3.patterns.observer.WarehouseFulfillmentObserver
import lab3.patterns.state.OrderContext
import lab3.patterns.strategy.*
import lab3.test.TestRunner
import java.util.Scanner

fun main() {
    val scanner = Scanner(System.`in`)
    val invoker = OrderCommandInvoker()

    // Default active observers
    val customerObserver = CustomerEmailNotificationObserver("alice@reader.io")
    val warehouseObserver = WarehouseFulfillmentObserver()
    val auditObserver = AnalyticsAuditObserver()

    // Create an initial sample order to play with immediately
    var currentBook: Book = LibraryCatalog.getAllBooks().first()
    var currentOrder: OrderComponent = BaseOrder(currentBook)
    var currentOrderContext = OrderContext(
        orderId = "ORD-2026-001",
        customerId = "CUST-ALICE-42",
        customerEmail = "alice@reader.io",
        item = currentOrder,
        initialStrategy = RegularPricingStrategy()
    )
    currentOrderContext.attach(customerObserver)
    currentOrderContext.attach(warehouseObserver)
    currentOrderContext.attach(auditObserver)

    println("==================================================================")
    println("      BOOKSTORE SYSTEM - LAB 3: BEHAVIORAL DESIGN PATTERNS        ")
    println("==================================================================")

    while (true) {
        println("\n--------------------------- MAIN MENU ----------------------------")
        println("1. Create New Order (Browse Catalog & Select Decorator Add-ons)")
        println("2. Swap Discount Strategy at Runtime (Strategy Pattern)")
        println("3. Manage Observers [Attach / Detach] (Observer Pattern)")
        println("4. Execute Lifecycle Command [Pay / Ship / Deliver / Cancel] (Command & State)")
        println("5. Undo Last Command (Command Pattern Undo)")
        println("6. Redo Last Command (Command Pattern Redo)")
        println("7. View Current Order Details, Audit Log & Observer Notifications")
        println("8. Run Automated Unit Test Suite")
        println("9. Exit")
        print("Select an option (1-9): ")

        when (scanner.nextLine().trim()) {
            "1" -> {
                println("\n=== AVAILABLE BOOKS IN CATALOG ===")
                val books = LibraryCatalog.getAllBooks()
                books.forEachIndexed { idx, b ->
                    println(" [${idx + 1}] ID: ${b.id} | ${b.getDetails()}")
                }
                print("Select book number (1-${books.size}) [Default 1]: ")
                val selection = scanner.nextLine().trim().toIntOrNull() ?: 1
                currentBook = books.getOrElse(selection - 1) { books.first() }

                var component: OrderComponent = BaseOrder(currentBook)

                println("\n--- OPTIONAL ADD-ONS (DECORATOR PATTERN REUSE) ---")
                print("1. Add Gift Wrapping (+$5.00)? (y/N): ")
                if (scanner.nextLine().trim().equals("y", ignoreCase = true)) {
                    component = GiftWrappingDecorator(component)
                }

                print("2. Add Express Delivery (+$15.00)? (y/N): ")
                if (scanner.nextLine().trim().equals("y", ignoreCase = true)) {
                    component = ExpressDeliveryDecorator(component)
                }

                print("3. Add Shipping Insurance (+$3.50)? (y/N): ")
                if (scanner.nextLine().trim().equals("y", ignoreCase = true)) {
                    component = ShippingInsuranceDecorator(component)
                }

                print("\nEnter customer email [Default: alice@reader.io]: ")
                val emailInput = scanner.nextLine().trim()
                val email = if (emailInput.isBlank()) "alice@reader.io" else emailInput

                val newOrderId = "ORD-2026-" + (100..999).random()
                currentOrderContext = OrderContext(
                    orderId = newOrderId,
                    customerId = "CUST-${(1000..9999).random()}",
                    customerEmail = email,
                    item = component,
                    initialStrategy = RegularPricingStrategy()
                )

                // Clear previous observer logs and re-attach
                customerObserver.clearMessages()
                warehouseObserver.clearLogs()
                auditObserver.clearRecords()
                invoker.clear()

                currentOrderContext.attach(customerObserver)
                currentOrderContext.attach(warehouseObserver)
                currentOrderContext.attach(auditObserver)

                println("\n>> Successfully initialized new order context:")
                println(currentOrderContext.getSummary())
            }

            "2" -> {
                println("\n=== SWAP DISCOUNT STRATEGY AT RUNTIME ===")
                println("Current Strategy: ${currentOrderContext.discountStrategy.name}")
                println("1. Standard Retail Pricing (0% discount)")
                println("2. Student Discount (15% discount)")
                println("3. VIP Loyalty Club (25% discount)")
                println("4. Seasonal Coupon 'FALL2026' ($10 off)")
                print("Choose strategy (1-4): ")

                val newStrategy: DiscountStrategy = when (scanner.nextLine().trim()) {
                    "2" -> StudentDiscountStrategy()
                    "3" -> VipLoyaltyStrategy()
                    "4" -> SeasonalCouponStrategy("FALL2026", 10.0)
                    else -> RegularPricingStrategy()
                }

                val cmd = ApplyDiscountCommand(currentOrderContext, newStrategy)
                invoker.executeCommand(cmd)

                println("\n>> Strategy updated to: ${newStrategy.name}")
                println("   Raw Cost:   $${"%.2f".format(currentOrderContext.getRawCost())}")
                println("   Final Cost: $${"%.2f".format(currentOrderContext.getFinalCost())}")
            }

            "3" -> {
                println("\n=== MANAGE OBSERVERS (RUNTIME ATTACH / DETACH) ===")
                val attached = currentOrderContext.getAttachedObservers()
                println("Active Observers:")
                println(" 1. Customer Email (${customerObserver.customerEmail}): " +
                        if (attached.contains(customerObserver)) "[ATTACHED]" else "[DETACHED]")
                println(" 2. Warehouse Fulfillment: " +
                        if (attached.contains(warehouseObserver)) "[ATTACHED]" else "[DETACHED]")
                println(" 3. Analytics Audit Monitor: " +
                        if (attached.contains(auditObserver)) "[ATTACHED]" else "[DETACHED]")

                println("\nToggle observer status:")
                println("1. Toggle Customer Email Observer")
                println("2. Toggle Warehouse Fulfillment Observer")
                println("3. Toggle Analytics Audit Observer")
                print("Select option (1-3): ")

                when (scanner.nextLine().trim()) {
                    "1" -> {
                        if (attached.contains(customerObserver)) {
                            currentOrderContext.detach(customerObserver)
                            println(">> Detached Customer Email Observer.")
                        } else {
                            currentOrderContext.attach(customerObserver)
                            println(">> Attached Customer Email Observer.")
                        }
                    }
                    "2" -> {
                        if (attached.contains(warehouseObserver)) {
                            currentOrderContext.detach(warehouseObserver)
                            println(">> Detached Warehouse Fulfillment Observer.")
                        } else {
                            currentOrderContext.attach(warehouseObserver)
                            println(">> Attached Warehouse Fulfillment Observer.")
                        }
                    }
                    "3" -> {
                        if (attached.contains(auditObserver)) {
                            currentOrderContext.detach(auditObserver)
                            println(">> Detached Analytics Audit Observer.")
                        } else {
                            currentOrderContext.attach(auditObserver)
                            println(">> Attached Analytics Audit Observer.")
                        }
                    }
                }
            }

            "4" -> {
                println("\n=== EXECUTE LIFECYCLE COMMAND (STATE & COMMAND PATTERNS) ===")
                println("Current State: [${currentOrderContext.state.stateName}]")
                println("1. Pay for Order (triggers transition PENDING -> PROCESSING)")
                println("2. Ship Order with Courier (triggers transition PROCESSING -> SHIPPED)")
                println("3. Deliver Order (triggers transition SHIPPED -> DELIVERED)")
                println("4. Cancel Order (triggers transition to CANCELLED)")
                print("Select command to execute (1-4): ")

                when (scanner.nextLine().trim()) {
                    "1" -> {
                        val txnId = "TXN-${(10000..99999).random()}"
                        val cmd = PayOrderCommand(currentOrderContext, txnId)
                        val ok = invoker.executeCommand(cmd)
                        if (ok) {
                            println(">> Executed ${cmd.getDescription()} successfully! New State: ${currentOrderContext.state.stateName}")
                        } else {
                            println(">> Execution failed.")
                        }
                    }
                    "2" -> {
                        print("Enter tracking number [Default: DHL-EXP-777]: ")
                        val input = scanner.nextLine().trim()
                        val tracking = if (input.isBlank()) "DHL-EXP-777" else input
                        val cmd = ShipOrderCommand(currentOrderContext, tracking)
                        val ok = invoker.executeCommand(cmd)
                        if (ok) {
                            println(">> Executed ${cmd.getDescription()} successfully! New State: ${currentOrderContext.state.stateName}")
                        } else {
                            println(">> Execution failed.")
                        }
                    }
                    "3" -> {
                        try {
                            currentOrderContext.deliver()
                            println(">> Order successfully delivered! New State: ${currentOrderContext.state.stateName}")
                        } catch (e: Exception) {
                            println(">> Action rejected by State: ${e.message}")
                        }
                    }
                    "4" -> {
                        print("Enter cancellation reason [Default: Customer requested refund]: ")
                        val input = scanner.nextLine().trim()
                        val reason = if (input.isBlank()) "Customer requested refund" else input
                        val cmd = CancelOrderCommand(currentOrderContext, reason)
                        val ok = invoker.executeCommand(cmd)
                        if (ok) {
                            println(">> Executed ${cmd.getDescription()} successfully! New State: ${currentOrderContext.state.stateName}")
                        } else {
                            println(">> Execution failed.")
                        }
                    }
                }
            }

            "5" -> {
                println("\n=== UNDO LAST COMMAND ===")
                if (invoker.canUndo()) {
                    val lastCmd = invoker.getHistory().first()
                    println("Undoing: ${lastCmd.getDescription()} ...")
                    val ok = invoker.undo()
                    if (ok) {
                        println(">> Undo successful! Current State: ${currentOrderContext.state.stateName}")
                    } else {
                        println(">> Undo failed.")
                    }
                } else {
                    println(">> No commands in history to undo.")
                }
            }

            "6" -> {
                println("\n=== REDO COMMAND ===")
                if (invoker.canRedo()) {
                    val nextCmd = invoker.getRedoStack().first()
                    println("Redoing: ${nextCmd.getDescription()} ...")
                    val ok = invoker.redo()
                    if (ok) {
                        println(">> Redo successful! Current State: ${currentOrderContext.state.stateName}")
                    } else {
                        println(">> Redo failed.")
                    }
                } else {
                    println(">> No commands in redo stack.")
                }
            }

            "7" -> {
                println("\n================ CURRENT ORDER STATUS ================")
                println(currentOrderContext.getSummary())

                println("\n--- COMMAND HISTORY (Invoker Stack) ---")
                val hist = invoker.getHistory()
                if (hist.isEmpty()) {
                    println(" (No commands executed yet)")
                } else {
                    hist.forEachIndexed { i, c ->
                        println(" [${i + 1}] ${c.getDescription()}")
                    }
                }

                println("\n--- CUSTOMER EMAIL OBSERVER LOGS ---")
                val emails = customerObserver.getReceivedMessages()
                if (emails.isEmpty()) println(" (No emails sent yet)") else emails.forEach { println(" - $it") }

                println("\n--- WAREHOUSE FULFILLMENT LOGS ---")
                val wh = warehouseObserver.getPackingLogs()
                if (wh.isEmpty()) println(" (No warehouse events yet)") else wh.forEach { println(" - $it") }

                println("\n--- ANALYTICS AUDIT RECORDS ---")
                val audit = auditObserver.getAuditRecords()
                if (audit.isEmpty()) println(" (No audit records yet)") else audit.forEach { println(" - $it") }
                println("======================================================")
            }

            "8" -> {
                println("\nRunning Automated Test Suite...\n")
                TestRunner.main(emptyArray())
            }

            "9" -> {
                println("\nExiting Bookstore System. Goodbye!")
                break
            }

            else -> println("Invalid option. Please choose 1-9.")
        }
    }
}
