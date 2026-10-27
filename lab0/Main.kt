package lab1

import java.util.Scanner

fun main() {
    val scanner = Scanner(System.`in`)
    val library = Library()
    val printer = LibraryPrinter()

    // Pre-populate with initial books
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
            "1" -> {
                printer.printBooks(library.getAllBooks())
            }
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
            else -> {
                println("Invalid option. Please enter 1, 2, 3, or 4.")
            }
        }
    }
}
