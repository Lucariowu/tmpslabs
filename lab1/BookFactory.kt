package lab2

abstract class Book(
    val title: String,
    val price: Double
) {
    abstract fun getDetails(): String
}

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

class AudioBook(
    title: String,
    price: Double,
    val durationMinutes: Int
) : Book(title, price) {
    override fun getDetails(): String = "$title (Audiobook, ${durationMinutes} mins) - $${"%.2f".format(price)}"
}

abstract class BookCreator {
    abstract fun createBook(title: String, price: Double, extra: String): Book
}

class PhysicalBookCreator : BookCreator() {
    override fun createBook(title: String, price: Double, extra: String): Book {
        val weight = extra.toDoubleOrNull() ?: 0.5
        return PhysicalBook(title, price, weight)
    }
}

class EBookCreator : BookCreator() {
    override fun createBook(title: String, price: Double, extra: String): Book {
        val url = if (extra.isBlank()) "https://books.example/download" else extra
        return EBook(title, price, url)
    }
}

class AudioBookCreator : BookCreator() {
    override fun createBook(title: String, price: Double, extra: String): Book {
        val duration = extra.toIntOrNull() ?: 60
        return AudioBook(title, price, duration)
    }
}
