package lab3.domain

object LibraryCatalog {
    private val books = mutableMapOf<String, Book>()

    init {
        books["B1"] = PhysicalBook("B1", "Clean Architecture", "Robert C. Martin", 40.0, 0.6)
        books["B2"] = PhysicalBook("B2", "Design Patterns: Gang of Four", "Erich Gamma et al.", 55.0, 0.9)
        books["B3"] = EBook("B3", "Kotlin in Action", "Dmitry Jemerov", 30.0, "https://books.io/dl/kotlin")
        books["B4"] = EBook("B4", "Refactoring", "Martin Fowler", 35.0, "https://books.io/dl/refactor")
    }

    fun getBookById(id: String): Book? = books[id]

    fun getAllBooks(): List<Book> = books.values.toList()

    fun addBook(book: Book) {
        books[book.id] = book
    }
}
