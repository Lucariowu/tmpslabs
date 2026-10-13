package lab1

open class Book(
    val title: String,
    val price: Double
) {
    open fun getDetails(): String = "$title - $${"%.2f".format(price)}"
}

class Library {
    private val books = mutableListOf<Book>()

    fun addBook(book: Book) {
        books.add(book)
    }

    fun getAllBooks(): List<Book> = books.toList()
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
