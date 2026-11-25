# Laboratory Work 3: Behavioral Design Patterns

This repository contains Laboratory Work 3 for the TMPS course, focusing on **Behavioral Design Patterns** in Kotlin.

## Domain
An **Online Bookstore & Library System** (extending the domain from Lab 1 and Lab 2) that manages catalog items, applies dynamic pricing strategies, tracks the complete order fulfillment lifecycle through finite states, dispatches reactive observer events across subsystems, and encapsulates customer and manager operations into undoable command objects.

## Patterns Implemented
1. **Strategy** (`lab3/patterns/strategy/`): Defines a family of interchangeable discount and pricing algorithms (`RegularPricingStrategy`, `StudentDiscountStrategy`, `VipLoyaltyStrategy`, `SeasonalCouponStrategy`) swappable dynamically at runtime.
2. **State** (`lab3/patterns/state/`): Models the order lifecycle using explicit state classes (`PendingPaymentState`, `ProcessingState`, `ShippedState`, `DeliveredState`, `CancelledState`), managing valid transitions and rejecting illegal state mutations.
3. **Observer** (`lab3/patterns/observer/`): Decouples lifecycle status changes from auxiliary services by publishing events to subscribed observers (`CustomerEmailNotificationObserver`, `WarehouseFulfillmentObserver`, `AnalyticsAuditObserver`) with runtime attach/detach capability.
4. **Command** (`lab3/patterns/command/`): Encapsulates lifecycle transitions and pricing modifications into standalone command objects (`PayOrderCommand`, `ShipOrderCommand`, `CancelOrderCommand`, `ApplyDiscountCommand`) managed by an `OrderCommandInvoker` with execution history and undo/redo support.

## Creational & Structural Reuse from Previous Labs
- **Creational (Singleton)**: `LibraryCatalog` manages the centralized catalog inventory.
- **Structural (Decorator)**: `BaseOrder` decorated with `GiftWrappingDecorator`, `ExpressDeliveryDecorator`, and `ShippingInsuranceDecorator` dynamically stacks add-on fees and descriptions before passing to behavioral handlers.

## Folder Structure
```
lab3/
├── domain/
│   ├── Book.kt               # Product hierarchy (PhysicalBook, EBook)
│   ├── LibraryCatalog.kt     # Singleton book inventory (Creational reuse)
│   └── OrderComponent.kt     # Order decorator hierarchy (Structural reuse)
├── patterns/
│   ├── strategy/
│   │   └── DiscountStrategy.kt # Strategy interface & Concrete pricing strategies
│   ├── state/
│   │   ├── OrderState.kt     # State interface
│   │   ├── OrderStates.kt    # Concrete states (Pending, Processing, Shipped, Delivered, Cancelled)
│   │   └── OrderContext.kt   # Context managing state, pricing, and observer publishing
│   ├── observer/
│   │   ├── OrderObserver.kt  # Observer interface
│   │   ├── OrderSubject.kt   # Subject interface
│   │   └── ConcreteObservers.kt # Customer email, Warehouse logistics, Analytics audit
│   └── command/
│       ├── OrderCommand.kt   # Command interface (execute, undo)
│       ├── ConcreteCommands.kt # Pay, Ship, Cancel, and ApplyDiscount commands
│       └── OrderCommandInvoker.kt # Invoker with history, undo, and redo stacks
├── client/
│   └── Main.kt               # Interactive console demonstration
├── test/
│   └── TestRunner.kt         # Automated unit test suite (11 test cases)
├── README.md                 # Build and run instructions
└── REPORT.md                 # Full laboratory report with UML diagrams, logs, and defense study guide
```

## Quick Start (Build & Run with One Command)

### 1. Run the Unit Test Suite (Automated Verification)
```bash
kotlinc $(find lab3 -name "*.kt") -include-runtime -d lab3_app.jar && java -cp lab3_app.jar lab3.test.TestRunner
```

### 2. Run the Interactive CLI Application
```bash
kotlinc $(find lab3 -name "*.kt") -include-runtime -d lab3_app.jar && java -cp lab3_app.jar lab3.client.MainKt
```
