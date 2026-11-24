# Laboratory Work 2: Structural Design Patterns

This repository contains Laboratory Work 2 for the TMPS course, focusing on **Structural Design Patterns** in Kotlin.

## Domain
An **Online Bookstore & Library System** that allows customers to browse an inventory of books, customize orders with optional add-ons (gift wrapping, express delivery, shipping insurance), and process payments securely.

## Patterns Implemented
1. **Decorator** (`lab2/patterns/decorator/`): Dynamically attaches extra services/fees to an order component at runtime without modifying the base order.
2. **Proxy** (`lab2/patterns/proxy/`): Sits in front of the payment gateway to validate authentication tokens, enforce invariant checks, and maintain a security audit log.
3. **Facade** (`lab2/patterns/facade/`): Provides a simple, unified interface (`BookstoreFacade`) coordinating catalog retrieval, order decoration, and proxy-protected payment.

## Folder Structure
```
lab2/
├── domain/
│   ├── Book.kt               # Product hierarchy (PhysicalBook, EBook)
│   └── LibraryCatalog.kt     # Singleton repository of books (Creational reuse)
├── patterns/
│   ├── decorator/
│   │   ├── OrderComponent.kt # Component interface
│   │   ├── BaseOrder.kt      # Concrete component
│   │   └── OrderDecorators.kt# Base decorator & Concrete decorators
│   ├── proxy/
│   │   ├── PaymentService.kt # Subject interface & data models
│   │   ├── RealPaymentService.kt # Real subject
│   │   └── PaymentServiceProxy.kt# Proxy with authentication & audit log
│   └── facade/
│       └── BookstoreFacade.kt# Unified facade coordinating the subsystem
├── client/
│   └── Main.kt               # Interactive console demonstration
├── test/
│   └── TestRunner.kt         # Automated unit test suite
├── README.md                 # Build and run instructions
└── REPORT.md                 # Full laboratory report with UML and analysis
```

## Quick Start (Build & Run with One Command)

### 1. Run the Unit Test Suite (Automated Verification)
```bash
kotlinc $(find lab2 -name "*.kt") -include-runtime -d lab2_app.jar && java -cp lab2_app.jar lab2.test.TestRunner
```

### 2. Run the Interactive CLI Application
```bash
kotlinc $(find lab2 -name "*.kt") -include-runtime -d lab2_app.jar && java -cp lab2_app.jar lab2.client.MainKt
```
