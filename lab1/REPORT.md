# Laboratory Work 1: SOLID Principles

## 1. Introduction

For this laboratory work and upcoming labs, the chosen topic is an **Online Bookstore & Library System**. The application is designed to manage a catalog of physical and electronic books, support customer purchases with various discounts, and provide an interactive terminal menu for users to insert, view, and buy books.

The goal of this lab is to demonstrate three SOLID principles in Kotlin in a clean and direct way:
1. **Single Responsibility Principle (SRP)**
2. **Open/Closed Principle (OCP)**
3. **Liskov Substitution Principle (LSP)**

Each principle is implemented in a separate file, and all are aggregated into a `Main.kt` file featuring an interactive menu.

---

## 2. Implementation of SOLID Principles

### 2.1 Single Responsibility Principle (SRP)
**Concept:** A class should have only one reason to change, meaning each class has a single, focused responsibility.

In `SingleResponsibility.kt`:
- `Book`: Model representing book attributes (`title`, `price`).
- `Library`: Holds the collection of books and manages adding/retrieving items.
- `LibraryPrinter`: Dedicated exclusively to formatting and presenting output (printing the catalog and purchase receipts) to the console.

If the output formatting or receipt style changes in the future, neither `Book` nor `Library` needs to be modified.

```kotlin
open class Book(
    val title: String,
    val price: Double
) {
    open fun getDetails(): String = "$title - $${"%.2f".format(price)}"
}

class Library {
    val books = mutableListOf<Book>()

    fun addBook(book: Book) {
        books.add(book)
    }

    fun getAllBooks(): List<Book> = books
}

class LibraryPrinter {
    fun printBooks(books: List<Book>) {
        if (books.isEmpty()) {
            println("The library is currently empty.")
            return
        }
        println("=== Available Books ===")
        books.forEachIndexed { index, book ->
            println("${index + 1}. ${book.getDetails()}")
        }
        println("========================")
    }

    fun printInvoice(book: Book, discount: Discount) {
        val finalPrice = discount.apply(book.price)
        println("=== Purchase Receipt ===")
        println("Book: ${book.title}")
        println("Original Price: $${"%.2f".format(book.price)}")
        println("Final Price: $${"%.2f".format(finalPrice)}")
        println("========================")
    }
}
```

---

### 2.2 Open/Closed Principle (OCP)
**Concept:** Software entities should be open for extension, but closed for modification.

In `OpenClosed.kt`:
- The base `Discount` class defines the default behavior (no discount).
- Specialized subclasses such as `StudentDiscount` (15% off) and `VipDiscount` (20% off) extend this behavior.

Adding new discount policies (e.g. seasonal sales or promo codes) can be done by creating a new subclass without modifying existing discount or checkout logic.

```kotlin
open class Discount {
    open fun apply(price: Double): Double = price
}

class StudentDiscount : Discount() {
    override fun apply(price: Double): Double = price * 0.85
}

class VipDiscount : Discount() {
    override fun apply(price: Double): Double = price * 0.80
}
```

---

### 2.3 Liskov Substitution Principle (LSP)
**Concept:** Subtypes must be substitutable for their base types without altering program correctness.

In `LiskovSubstitution.kt`:
- `PhysicalBook` (with shipping weight) and `EBook` (with download URL) inherit from `Book`.
- Both subtypes override `getDetails()` while maintaining the contract of the base class.
- The `Library` stores and treats both as `Book` instances, and `LibraryPrinter.printBooks()` displays both seamlessly without type checks (`is`) or casting.

```kotlin
class PhysicalBook(
    title: String,
    price: Double,
    val weightKg: Double
) : Book(title, price) {
    override fun getDetails(): String = "$title (Physical, ${weightKg}kg) - $${"%.2f".format(price)}"
}

class EBook(
    title: String,
    price: Double,
    val downloadUrl: String
) : Book(title, price) {
    override fun getDetails(): String = "$title (E-Book) - $${"%.2f".format(price)}"
}
```

---

### 2.4 Aggregating Everything in `Main.kt`

`Main.kt` provides an interactive console menu allowing users to:
1. View all books currently in the library catalog.
2. Insert a new book (either Physical or E-Book, demonstrating LSP).
3. Purchase a book with an interchangeable discount (demonstrating OCP).
4. Exit the application.

```kotlin
fun main() {
    val scanner = Scanner(System.`in`)
    val library = Library()
    val printer = LibraryPrinter()

    library.addBook(PhysicalBook("Clean Code", 35.0, 0.6))
    library.addBook(EBook("Kotlin in Action", 30.0, "https://books.example/kotlin"))

    println("=== Bookstore & Library Management ===")

    while (true) {
        println("\nMenu:")
        println("1. View all books")
        println("2. Add a new book")
        println("3. Purchase a book with discount")
        println("4. Exit")
        print("Choose an option: ")

        when (scanner.nextLine().trim()) {
            "1" -> printer.printBooks(library.getAllBooks())
            "2" -> {
                print("Enter title: ")
                val title = scanner.nextLine().trim()
                print("Enter price: ")
                val price = scanner.nextLine().trim().toDoubleOrNull() ?: 0.0

                println("Select book type:")
                println("1. Physical Book")
                println("2. E-Book")
                print("Choice: ")

                val book: Book = when (scanner.nextLine().trim()) {
                    "2" -> {
                        print("Enter download URL: ")
                        val url = scanner.nextLine().trim()
                        EBook(title, price, url)
                    }
                    else -> {
                        print("Enter weight in kg: ")
                        val weight = scanner.nextLine().trim().toDoubleOrNull() ?: 0.5
                        PhysicalBook(title, price, weight)
                    }
                }
                library.addBook(book)
                println("Book \"$title\" added successfully.")
            }
            "3" -> {
                val books = library.getAllBooks()
                if (books.isEmpty()) {
                    println("No books available in the library.")
                    continue
                }
                printer.printBooks(books)
                print("Select book number: ")
                val index = (scanner.nextLine().trim().toIntOrNull() ?: 0) - 1
                if (index !in books.indices) {
                    println("Invalid selection.")
                    continue
                }

                val selectedBook = books[index]
                println("Select discount:")
                println("1. Standard (No discount)")
                println("2. Student (15% off)")
                println("3. VIP (20% off)")
                print("Choice: ")

                val discount: Discount = when (scanner.nextLine().trim()) {
                    "2" -> StudentDiscount()
                    "3" -> VipDiscount()
                    else -> Discount()
                }
                printer.printInvoice(selectedBook, discount)
            }
            "4" -> {
                println("Goodbye!")
                break
            }
            else -> println("Invalid option. Please enter 1, 2, 3, or 4.")
        }
    }
}
```

#### Example Output:
```text
=== Bookstore & Library Management ===

Menu:
1. View all books
2. Add a new book
3. Purchase a book with discount
4. Exit
Choose an option: 1
=== Available Books ===
1. Clean Code (Physical, 0.6kg) - $35.00
2. Kotlin in Action (E-Book) - $30.00
========================

Menu:
1. View all books
2. Add a new book
3. Purchase a book with discount
4. Exit
Choose an option: 2
Enter title: The Pragmatic Programmer
Enter price: 40.0
Select book type:
1. Physical Book
2. E-Book
Choice: 1
Enter weight in kg: 0.8
Book "The Pragmatic Programmer" added successfully.

Menu:
1. View all books
2. Add a new book
3. Purchase a book with discount
4. Exit
Choose an option: 3
=== Available Books ===
1. Clean Code (Physical, 0.6kg) - $35.00
2. Kotlin in Action (E-Book) - $30.00
3. The Pragmatic Programmer (Physical, 0.8kg) - $40.00
========================
Select book number: 3
Select discount:
1. Standard (No discount)
2. Student (15% off)
3. VIP (20% off)
Choice: 2
=== Purchase Receipt ===
Book: The Pragmatic Programmer
Original Price: $40.00
Final Price: $34.00
========================
```

---

## 3. Conclusion

In this lab, three core SOLID principles were implemented and showcased in a simple interactive terminal application:
- **Single Responsibility Principle**: `Library` manages collection storage, while `LibraryPrinter` is solely responsible for rendering output.
- **Open/Closed Principle**: The `Discount` hierarchy is open for extension without changing existing purchase logic.
- **Liskov Substitution Principle**: `PhysicalBook` and `EBook` seamlessly substitute for `Book` in the library collection and printer without special handling or type checking.

This interactive setup gives a clean baseline that will be extended with design patterns in the upcoming labs.
