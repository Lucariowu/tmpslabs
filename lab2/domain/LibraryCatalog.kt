package lab2.domain

object LibraryCatalog {
    private val books = mutableMapOf<String, Book>()

    init {
        addBook(PhysicalBook("B101", "Clean Code", "Robert C. Martin", 35.0, 0.6))
        addBook(PhysicalBook("B102", "Refactoring", "Martin Fowler", 42.0, 0.8))
        addBook(EBook("B103", "Kotlin in Action", "Dmitry Jemerov", 28.0, "https://books.store/kotlin"))
        addBook(EBook("B104", "Design Patterns", "Gang of Four", 45.0, "https://books.store/gof"))
    }

    fun addBook(book: Book) {
        books[book.id] = book
    }

    fun getBookById(id: String): Book? = books[id]

    fun getAllBooks(): List<Book> = books.values.toList()
}
