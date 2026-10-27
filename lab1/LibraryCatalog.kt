package lab2

object LibraryCatalog {
    private val books = mutableListOf<Book>()

    init {
        books.add(PhysicalBook("Clean Code", 35.0, 0.6))
        books.add(EBook("Kotlin in Action", 30.0, "https://books.example/kotlin"))
        books.add(AudioBook("The Pragmatic Programmer", 45.0, 360))
    }

    fun addBook(book: Book) {
        books.add(book)
    }

    fun getAllBooks(): List<Book> = books.toList()

    fun getBookByIndex(index: Int): Book? {
        return if (index in books.indices) books[index] else null
    }

    fun totalBooksCount(): Int = books.size
}
