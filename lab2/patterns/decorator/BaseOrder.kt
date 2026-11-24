package lab2.patterns.decorator

import lab2.domain.Book

class BaseOrder(val book: Book) : OrderComponent {
    override fun getCost(): Double = book.price
    override fun getDescription(): String = "${book.title} ($${"%.2f".format(book.price)})"
}
