package lab2

import java.util.Scanner

fun main() {
    val scanner = Scanner(System.`in`)
    var orderCounter = 1

    println("=== Bookstore & Library Management (Lab 2: Creational Patterns) ===")

    while (true) {
        println("\nMenu:")
        println("1. View all books in catalog (Singleton)")
        println("2. Add a new book (Factory Method)")
        println("3. Create a custom order (Builder)")
        println("4. Exit")
        print("Choose an option: ")

        when (scanner.nextLine().trim()) {
            "1" -> {
                val books = LibraryCatalog.getAllBooks()
                if (books.isEmpty()) {
                    println("The catalog is currently empty.")
                } else {
                    println("\n=== Catalog Inventory (${LibraryCatalog.totalBooksCount()} books) ===")
                    books.forEachIndexed { idx, book ->
                        println("${idx + 1}. ${book.getDetails()}")
                    }
                    println("==========================================")
                }
            }

            "2" -> {
                print("Enter title: ")
                val title = scanner.nextLine().trim()

                print("Enter price: ")
                val price = scanner.nextLine().trim().toDoubleOrNull() ?: 0.0

                println("Select book format:")
                println("1. Physical Book")
                println("2. E-Book")
                println("3. Audiobook")
                print("Choice: ")

                val (creator: BookCreator, extraPrompt: String) = when (scanner.nextLine().trim()) {
                    "2" -> Pair(EBookCreator(), "Enter download URL: ")
                    "3" -> Pair(AudioBookCreator(), "Enter duration in minutes: ")
                    else -> Pair(PhysicalBookCreator(), "Enter weight in kg: ")
                }

                print(extraPrompt)
                val extra = scanner.nextLine().trim()

                val createdBook = creator.createBook(title, price, extra)
                LibraryCatalog.addBook(createdBook)
                println("Created and added: ${createdBook.getDetails()}")
            }

            "3" -> {
                val books = LibraryCatalog.getAllBooks()
                if (books.isEmpty()) {
                    println("Cannot create order: the catalog is empty.")
                    continue
                }

                val orderId = "ORD-${"%03d".format(orderCounter++)}"
                val builder = OrderBuilder(orderId)

                print("Enter customer name: ")
                val name = scanner.nextLine().trim()
                if (name.isNotBlank()) builder.forCustomer(name)

                println("\nAvailable books:")
                books.forEachIndexed { idx, book ->
                    println("${idx + 1}. ${book.getDetails()}")
                }

                print("Enter book numbers to add (comma-separated, e.g. 1, 2): ")
                val selections = scanner.nextLine().trim()
                val selectedIndices = selections.split(",")
                    .mapNotNull { it.trim().toIntOrNull() }
                    .map { it - 1 }

                var addedCount = 0
                for (idx in selectedIndices) {
                    val book = LibraryCatalog.getBookByIndex(idx)
                    if (book != null) {
                        builder.addItem(book)
                        addedCount++
                    }
                }

                if (addedCount == 0) {
                    println("No valid books selected. Cancelling order.")
                    continue
                }

                print("Enter discount percentage (0-50, or hit Enter for 0): ")
                val discount = scanner.nextLine().trim().toDoubleOrNull() ?: 0.0
                builder.applyDiscount(discount)

                print("Include gift wrapping? (y/N): ")
                val isGift = scanner.nextLine().trim().equals("y", ignoreCase = true)
                builder.withGiftWrapping(isGift)

                print("Require home delivery? (y/N): ")
                val isDelivery = scanner.nextLine().trim().equals("y", ignoreCase = true)
                if (isDelivery) {
                    print("Enter delivery address: ")
                    val address = scanner.nextLine().trim()
                    builder.withDeliveryAddress(if (address.isNotBlank()) address else "Home Address")
                }

                print("Add a custom note (optional, hit Enter to skip): ")
                val note = scanner.nextLine().trim()
                if (note.isNotBlank()) builder.withNote(note)

                val order = builder.build()
                println()
                order.printSummary()
            }

            "4" -> {
                println("Exiting system. Goodbye!")
                break
            }

            else -> {
                println("Invalid option. Please choose 1, 2, 3, or 4.")
            }
        }
    }
}
