package lab3.domain

abstract class Book(
    val id: String,
    val title: String,
    val author: String,
    val price: Double
) {
    abstract fun getDetails(): String
}

class PhysicalBook(
    id: String,
    title: String,
    author: String,
    price: Double,
    val weightKg: Double
) : Book(id, title, author, price) {
    override fun getDetails(): String = "Physical: $title by $author (${weightKg}kg) - $${"%.2f".format(price)}"
}

class EBook(
    id: String,
    title: String,
    author: String,
    price: Double,
    val downloadUrl: String
) : Book(id, title, author, price) {
    override fun getDetails(): String = "E-Book: $title by $author - $${"%.2f".format(price)}"
}
