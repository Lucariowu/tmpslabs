# Laboratory Work 3: Behavioral Design Patterns - Comprehensive Defense & Study Guide

This document contains full explanations of all behavioral design patterns, implementation walkthroughs, test suite breakdowns, and defense preparation notes for Laboratory Work 3.

---

## 1. Project Structure Overview

The code is located in [`lab3/`](file:///home/ionut/Programming/tmpslabs/lab3/):

```
lab3/
├── domain/
│   ├── Book.kt                     # Product hierarchy (PhysicalBook, EBook)
│   ├── LibraryCatalog.kt           # Singleton catalog inventory (Creational reuse)
│   └── OrderComponent.kt           # Order Decorator hierarchy (Structural reuse)
├── patterns/
│   ├── strategy/
│   │   └── DiscountStrategy.kt     # Strategy interface & concrete pricing strategies
│   ├── state/
│   │   ├── OrderState.kt           # State interface
│   │   ├── OrderStates.kt          # Concrete states (Pending, Processing, Shipped, Delivered, Cancelled)
│   │   └── OrderContext.kt         # Context managing lifecycle, pricing, and observer notifications
│   ├── observer/
│   │   ├── OrderObserver.kt        # Observer interface
│   │   ├── OrderSubject.kt         # Subject interface
│   │   └── ConcreteObservers.kt   # Customer email, Warehouse logistics, Analytics audit
│   └── command/
│       ├── OrderCommand.kt         # Command interface (execute, undo)
│       ├── ConcreteCommands.kt     # Pay, Ship, Cancel, ApplyDiscount commands
│       └── OrderCommandInvoker.kt  # Invoker with history, undo, and redo stacks
├── client/
│   └── Main.kt                     # Interactive CLI demonstration
├── test/
│   └── TestRunner.kt               # Automated test suite (11 test cases)
├── README.md                       # Quick-start build & run instructions
├── REPORT.md                       # Laboratory report matching lab2's style
└── DEFENSE_GUIDE.md                # This comprehensive defense and explanation guide
```

### Quick Commands

- **Run Unit Tests (11/11 passing):**
  ```bash
  kotlinc $(find lab3 -name "*.kt") -include-runtime -d lab3_app.jar && java -cp lab3_app.jar lab3.test.TestRunner
  ```
- **Run Interactive Application:**
  ```bash
  kotlinc $(find lab3 -name "*.kt") -include-runtime -d lab3_app.jar && java -cp lab3_app.jar lab3.client.MainKt
  ```

---

## 2. Concrete Explanations of Implemented Patterns

Four behavioral patterns were implemented to manage the order lifecycle, dynamic pricing, decoupled event dispatching, and reversible operations:

```
                  Client Action (Main / CLI)
                             │
                             ▼
               [Command Pattern] OrderCommandInvoker
                             │
            ┌────────────────┴────────────────┐
            ▼                                 ▼
   ApplyDiscountCommand                 PayOrderCommand
            │                                 │
            ▼                                 ▼
[Strategy Pattern]                     [Receiver / Context]
DiscountStrategy.applyDiscount()       OrderContext.pay()
(swaps algorithm at runtime)                  │
                                              ▼
                                    [State Pattern]
                                    PendingPaymentState.pay()
                                              │
                                              ▼ transitions to
                                    ProcessingState
                                              │
                                              ▼ triggers
                                    OrderContext.setState()
                                              │
                                              ▼ publishes event
                                    [Observer Pattern]
                        ┌─────────────────────┼─────────────────────┐
                        ▼                     ▼                     ▼
              CustomerEmailObserver   WarehouseObserver     AnalyticsObserver
              (dispatches receipt)    (generates pick-list) (logs audit trail)
```

---

### A. Strategy Pattern
* **Intent:** Define a family of algorithms, encapsulate each one, and make them interchangeable at runtime.
* **Problem Solved:** Prevents hardcoding promotional pricing rules, student discounts, and member coupons inside order classes with `if-else` cascades.
* **Where it is implemented:**
  - Interface: [`DiscountStrategy`](file:///home/ionut/Programming/tmpslabs/lab3/patterns/strategy/DiscountStrategy.kt#L7-L11)
  - Concrete Strategies:
    - [`RegularPricingStrategy`](file:///home/ionut/Programming/tmpslabs/lab3/patterns/strategy/DiscountStrategy.kt#L13-L18): No discount applied (100% price).
    - [`StudentDiscountStrategy`](file:///home/ionut/Programming/tmpslabs/lab3/patterns/strategy/DiscountStrategy.kt#L20-L28): 15% academic discount.
    - [`VipLoyaltyStrategy`](file:///home/ionut/Programming/tmpslabs/lab3/patterns/strategy/DiscountStrategy.kt#L30-L38): 25% bookstore VIP club discount.
    - [`SeasonalCouponStrategy`](file:///home/ionut/Programming/tmpslabs/lab3/patterns/strategy/DiscountStrategy.kt#L40-L49): Flat $10 coupon deduction with lower bound clamped at $0.00.
  - Context: [`OrderContext`](file:///home/ionut/Programming/tmpslabs/lab3/patterns/state/OrderContext.kt#L21-L85) delegating `getFinalCost()` to `discountStrategy.applyDiscount(rawCost)`.
* **Explanation for the Professor:**
  > *"Instead of using conditional branches inside our order calculation to check user types, we encapsulate each discount formula behind the `DiscountStrategy` interface. The `OrderContext` holds a reference to the strategy interface, allowing algorithms to be swapped on the fly without modifying existing order code (adhering to the Open-Closed Principle)."*

---

### B. State Pattern
* **Intent:** Allow an object to alter its behavior when its internal state changes, appearing as if the object changed its class.
* **Problem Solved:** Replaces state flags (e.g. `isPaid`, `isShipped`, `isCancelled`) and large switch-cases. Prevents illegal state transitions (e.g., shipping an unpaid order or cancelling a parcel already in transit).
* **Where it is implemented:**
  - State Interface: [`OrderState`](file:///home/ionut/Programming/tmpslabs/lab3/patterns/state/OrderState.kt#L7-L15)
  - Context: [`OrderContext`](file:///home/ionut/Programming/tmpslabs/lab3/patterns/state/OrderContext.kt#L15-L95)
  - Concrete States in [`OrderStates.kt`](file:///home/ionut/Programming/tmpslabs/lab3/patterns/state/OrderStates.kt):
    - [`PendingPaymentState`](file:///home/ionut/Programming/tmpslabs/lab3/patterns/state/OrderStates.kt#L6-L23): Valid operations: `pay()` -> `ProcessingState`, `cancel()` -> `CancelledState`. Forbidden operations: `ship()`, `deliver()` throw `IllegalStateException`.
    - [`ProcessingState`](file:///home/ionut/Programming/tmpslabs/lab3/patterns/state/OrderStates.kt#L28-L49): Valid: `ship()` -> `ShippedState`, `cancel()` -> `CancelledState` (with refund). Forbidden: `pay()`, `deliver()`.
    - [`ShippedState`](file:///home/ionut/Programming/tmpslabs/lab3/patterns/state/OrderStates.kt#L54-L73): Valid: `deliver()` -> `DeliveredState`. Forbidden: `cancel()` (cannot cancel parcel already with courier).
    - [`DeliveredState`](file:///home/ionut/Programming/tmpslabs/lab3/patterns/state/OrderStates.kt#L78-L96): Terminal state. All operations rejected.
    - [`CancelledState`](file:///home/ionut/Programming/tmpslabs/lab3/patterns/state/OrderStates.kt#L101-L121): Terminal state. All operations rejected.
* **Explanation for the Professor:**
  > *"Each lifecycle phase is represented by an independent state class rather than an enum with conditionals. The `OrderContext` delegates lifecycle methods directly to its current state object. Each state validates the transition rules and advances the context to the next state, enforcing domain invariants like disallowing shipment before payment."*

---

### C. Observer Pattern
* **Intent:** Define a one-to-many dependency between objects so that when one object changes state, all its dependents are notified and updated automatically.
* **Problem Solved:** Decouples order processing from notification systems, warehouse queues, and audit databases.
* **Where it is implemented:**
  - Subject Interface: [`OrderSubject`](file:///home/ionut/Programming/tmpslabs/lab3/patterns/observer/OrderSubject.kt#L7-L12)
  - Observer Interface: [`OrderObserver`](file:///home/ionut/Programming/tmpslabs/lab3/patterns/observer/OrderObserver.kt#L7-L11)
  - Concrete Subject: [`OrderContext`](file:///home/ionut/Programming/tmpslabs/lab3/patterns/state/OrderContext.kt#L57-L76) (`attach`, `detach`, `notifyObservers`)
  - Concrete Observers in [`ConcreteObservers.kt`](file:///home/ionut/Programming/tmpslabs/lab3/patterns/observer/ConcreteObservers.kt):
    - [`CustomerEmailNotificationObserver`](file:///home/ionut/Programming/tmpslabs/lab3/patterns/observer/ConcreteObservers.kt#L6-L19): Generates customer email updates for every transition.
    - [`WarehouseFulfillmentObserver`](file:///home/ionut/Programming/tmpslabs/lab3/patterns/observer/ConcreteObservers.kt#L24-L44): Reacts to `PROCESSING` by generating packaging slips and picking items; restocks on `CANCELLED`.
    - [`AnalyticsAuditObserver`](file:///home/ionut/Programming/tmpslabs/lab3/patterns/observer/ConcreteObservers.kt#L49-L62): Appends timestamped audit records for compliance.
* **Explanation for the Professor:**
  > *"When the order changes state inside `setState()`, it notifies all registered `OrderObserver` instances. The subject does not depend on concrete observer implementations. Observers can subscribe or unsubscribe at runtime, preventing tight coupling and memory leaks."*

---

### D. Command Pattern
* **Intent:** Encapsulate a request as an object, thereby letting you parameterize clients with different requests, queue or log requests, and support undoable operations.
* **Problem Solved:** Decouples the invoker (UI/manager) from the receiver (`OrderContext`), while maintaining a history stack to enable rollback/undo and redo.
* **Where it is implemented:**
  - Command Interface: [`OrderCommand`](file:///home/ionut/Programming/tmpslabs/lab3/patterns/command/OrderCommand.kt#L7-L12) (`execute()`, `undo()`, `getDescription()`)
  - Receiver: [`OrderContext`](file:///home/ionut/Programming/tmpslabs/lab3/patterns/state/OrderContext.kt)
  - Concrete Commands in [`ConcreteCommands.kt`](file:///home/ionut/Programming/tmpslabs/lab3/patterns/command/ConcreteCommands.kt):
    - [`PayOrderCommand`](file:///home/ionut/Programming/tmpslabs/lab3/patterns/command/ConcreteCommands.kt#L11-L36): Executes payment; `undo()` refunds and restores previous state.
    - [`ShipOrderCommand`](file:///home/ionut/Programming/tmpslabs/lab3/patterns/command/ConcreteCommands.kt#L41-L67): Executes shipping; `undo()` recalls shipment and restores processing state.
    - [`CancelOrderCommand`](file:///home/ionut/Programming/tmpslabs/lab3/patterns/command/ConcreteCommands.kt#L72-L98): Cancels order; `undo()` reinstates previous order state.
    - [`ApplyDiscountCommand`](file:///home/ionut/Programming/tmpslabs/lab3/patterns/command/ConcreteCommands.kt#L103-L126): Applies new discount strategy; `undo()` restores previous strategy.
  - Invoker: [`OrderCommandInvoker`](file:///home/ionut/Programming/tmpslabs/lab3/patterns/command/OrderCommandInvoker.kt#L9-L55) maintaining `history` and `redoStack`.
* **Explanation for the Professor:**
  > *"The command pattern packages operations as first-class objects containing their receiver and pre-execution state. The `OrderCommandInvoker` executes the command and pushes it onto an undo stack. Calling `undo()` restores the receiver to its exact pre-command state and alerts observers without violating encapsulation."*

---

## 3. Explanations & Examples for Unimplemented Patterns (Defense Reference)

Below are concrete explanations and domain examples for the remaining patterns from the syllabus:

### 1. Chain of Responsibility
* **Concept:** Passes a request along a sequence of handlers. Each handler decides either to process the request or pass it along to the next handler in the chain.
* **Bookstore Example:** An order validation pipeline before checkout:
  ```kotlin
  abstract class OrderValidationHandler {
      private var next: OrderValidationHandler? = null
      fun setNext(handler: OrderValidationHandler): OrderValidationHandler { next = handler; return handler }
      fun handle(order: OrderContext): Boolean {
          if (!validate(order)) return false
          return next?.handle(order) ?: true
      }
      abstract fun validate(order: OrderContext): Boolean
  }
  // Handlers: StockAvailabilityHandler -> FraudCheckHandler -> CreditLimitHandler
  ```
* **Mistake to Avoid:** Hardcoding the next handler inside the class, or allowing a request to fall off the end of the chain with undefined behavior.

---

### 2. Iterator
* **Concept:** Provides sequential access to elements in an aggregate collection without revealing its underlying structure (tree, hash map, list).
* **Bookstore Example:** Traversing books by different filtering criteria without exposing the `Map<String, Book>` internal storage:
  ```kotlin
  interface BookIterator { fun hasNext(): Boolean; fun next(): Book }
  class DiscountedBooksIterator(private val catalog: List<Book>) : BookIterator { ... }
  ```
* **Mistake to Avoid:** Simply returning the raw internal collection (e.g. `catalog.getBooks()`) or wrapping a standard `for` loop with no traversal logic.

---

### 3. Mediator
* **Concept:** Centralizes complex communications and interactions between multiple colleague objects so they don't reference each other directly.
* **Bookstore Example:** A `CheckoutDialogMediator`: when the user checks "Express Delivery", the mediator enables the courier dropdown, recalculates the shipping estimate, and refreshes the total price widget without the checkbox directly knowing about the dropdown.
* **Mistake to Avoid:** Turning the mediator into a "God Class" that contains all business logic instead of just orchestrating communications.

---

### 4. Memento
* **Concept:** Captures and externalizes an object's internal state so that it can be restored later, without violating encapsulation (private fields remain private).
* **Bookstore Example:** Saving a shopping cart checkpoint (`CartMemento`) before experimenting with coupon codes:
  ```kotlin
  class CartMemento(internal val itemsSnapshot: List<OrderItem>, internal val total: Double)
  class ShoppingCart {
      fun saveSnapshot(): CartMemento = CartMemento(ArrayList(items), calculateTotal())
      fun restore(memento: CartMemento) { this.items = ArrayList(memento.itemsSnapshot) }
  }
  ```
* **Mistake to Avoid:** Storing mutable references inside the snapshot, allowing external modifications to corrupt past state.

---

### 5. Template Method
* **Concept:** Defines the skeleton of an algorithm in a base class method, allowing subclasses to override specific steps without modifying the algorithm's structure.
* **Bookstore Example:** Generating invoices across different formats:
  ```kotlin
  abstract class InvoiceGenerator {
      // Template method marked final to preserve sequence
      fun generate(order: OrderContext) {
          fetchData(order)
          printHeader()
          printOrderLines(order) // Abstract step
          printFooter()          // Abstract step
      }
  }
  class PdfInvoiceGenerator : InvoiceGenerator { ... }
  class HtmlInvoiceGenerator : InvoiceGenerator { ... }
  ```
* **Mistake to Avoid:** Allowing subclasses to override the template method itself, thereby breaking the mandated sequence of execution.

---

### 6. Visitor
* **Concept:** Separates an algorithm from the object structure on which it operates, using double dispatch (`accept(visitor)` -> `visit(this)`).
* **Bookstore Example:** Performing operations (e.g. `TaxCalculationVisitor`, `CatalogExportVisitor`) across different items (`PhysicalBook`, `EBook`, `AudioBook`):
  ```kotlin
  interface CatalogVisitor {
      fun visit(book: PhysicalBook)
      fun visit(ebook: EBook)
  }
  ```
* **Mistake to Avoid:** Using `if (item is PhysicalBook)` type checks or reflection instead of standard double dispatch.

---

### 7. Interpreter (Good to Know)
* **Concept:** Given a language, defines a grammar along with an interpreter that parses and evaluates sentence expressions.
* **Bookstore Example:** An advanced catalog search syntax (e.g., `author = "Fowler" AND (price < 40 OR genre = "Tech")`).
* **Mistake to Avoid:** Using Interpreter for complex programming languages or grammars better handled by dedicated parser generators (e.g., ANTLR).

---

## 4. Test Suite Walkthrough: What and How Tests Are Implemented

The tests in [`lab3/test/TestRunner.kt`](file:///home/ionut/Programming/tmpslabs/lab3/test/TestRunner.kt) verify each behavioral invariant without external dependencies:

```
==================================================
  LAB 3: BEHAVIORAL DESIGN PATTERNS - TEST SUITE  
==================================================

--- 1. STRATEGY PATTERN TESTS ---
Running: testStrategyCalculatesCorrectDiscounts ... [PASSED]
Running: testStrategyCanBeSwappedAtRuntime ... [PASSED]

--- 2. STATE PATTERN TESTS ---
Running: testValidStateTransitions ... [PASSED]
Running: testForbiddenStateTransitionsThrowExceptions ... [PASSED]
Running: testCannotCancelShippedOrDeliveredOrder ... [PASSED]

--- 3. OBSERVER PATTERN TESTS ---
Running: testAllSubscribedObserversAreNotified ... [PASSED]
Running: testUnsubscribedObserversDoNotReceiveUpdates ... [PASSED]

--- 4. COMMAND PATTERN TESTS ---
Running: testCommandExecutionAndUndoRestoresState ... [PASSED]
Running: testCommandRedoAndApplyDiscountUndo ... [PASSED]

--- 5. PATTERN INTERACTION TEST ---
Running: testIntegratedWorkflowStrategyCommandStateObserver ... [PASSED]

--- 6. OPEN-CLOSED PRINCIPLE EXTENSION TEST ---
Running: testOpenForExtensionAllowsNewObserverAndStrategyWithoutModifyingExistingCode ... [PASSED]

==================================================
TEST SUMMARY: 11 passed, 0 failed out of 11 total.
==================================================
```

### Detailed Breakdown:

1. **Strategy Tests**:
   - `testStrategyCalculatesCorrectDiscounts`: Passes sample raw costs ($100.0) through Standard, Student (15%), VIP (25%), and Coupon strategies, asserting exact arithmetic values and lower-bound clamping at $0.00.
   - `testStrategyCanBeSwappedAtRuntime`: Mutates `order.discountStrategy` on an active `OrderContext` from Standard ($40) -> Student ($34) -> VIP ($30), verifying immediate recalculation without recreating the order.

2. **State Tests**:
   - `testValidStateTransitions`: Walks an order through `PENDING_PAYMENT` -> `PROCESSING` -> `SHIPPED` -> `DELIVERED`, asserting state names and tracking data at each stage.
   - `testForbiddenStateTransitionsThrowExceptions`: Uses `assertFailsWith(IllegalStateException::class.java)` to verify that shipping an unpaid order or paying an already paid order triggers an exception.
   - `testCannotCancelShippedOrDeliveredOrder`: Verifies that cancelling an order in transit or already delivered is rejected.

3. **Observer Tests**:
   - `testAllSubscribedObserversAreNotified`: Subscribes customer, warehouse, and audit observers, invokes `order.pay()`, and asserts that each observer receives the appropriate event log.
   - `testUnsubscribedObserversDoNotReceiveUpdates`: Detaches the customer observer, ships the order, and confirms that only the warehouse observer receives the new event.

4. **Command Tests**:
   - `testCommandExecutionAndUndoRestoresState`: Executes `PayOrderCommand`, verifies transition to `PROCESSING`, calls `invoker.undo()`, and confirms the state is rolled back to `PENDING_PAYMENT`.
   - `testCommandRedoAndApplyDiscountUndo`: Proves symmetry: applying a discount, undoing back to standard price, and redoing to restore the discount.

5. **Integrated Pattern Interaction Test**:
   - `testIntegratedWorkflowStrategyCommandStateObserver`: Decorates an order with add-on fees (structural reuse), applies a discount strategy via command, invokes lifecycle commands, verifies state transitions and observer alerts, and tests undo rollback.

6. **Open-Closed Principle Extension Test**:
   - `testOpenForExtensionAllowsNewObserverAndStrategyWithoutModifyingExistingCode`: Instantiates an anonymous `SmsNotificationObserver` and `BlackFriday50OffStrategy` and runs them against the context, demonstrating that the system can be extended without editing existing classes.
