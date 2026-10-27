package lab1

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
