package lab1

open class Discount {
    open fun apply(price: Double): Double = price
}

class StudentDiscount : Discount() {
    override fun apply(price: Double): Double = price * 0.85
}

class VipDiscount : Discount() {
    override fun apply(price: Double): Double = price * 0.80
}
