package lab3.patterns.strategy

/**
 * Strategy Pattern: Defines interchangeable pricing and discount algorithms.
 * The client or context can swap pricing algorithms dynamically at runtime.
 */
interface DiscountStrategy {
    val name: String
    val description: String
    fun applyDiscount(rawTotal: Double): Double
}

class RegularPricingStrategy : DiscountStrategy {
    override val name: String = "Standard Pricing"
    override val description: String = "Standard retail pricing (no discount applied)."

    override fun applyDiscount(rawTotal: Double): Double = rawTotal
}

class StudentDiscountStrategy : DiscountStrategy {
    override val name: String = "Student Discount (15%)"
    override val description: String = "15% off total order for verified academic accounts."

    override fun applyDiscount(rawTotal: Double): Double {
        val discount = rawTotal * 0.15
        return (rawTotal - discount).coerceAtLeast(0.0)
    }
}

class VipLoyaltyStrategy : DiscountStrategy {
    override val name: String = "VIP Loyalty Discount (25%)"
    override val description: String = "25% off total order for premium bookstore club members."

    override fun applyDiscount(rawTotal: Double): Double {
        val discount = rawTotal * 0.25
        return (rawTotal - discount).coerceAtLeast(0.0)
    }
}

class SeasonalCouponStrategy(
    val couponCode: String = "SAVE10",
    val discountAmount: Double = 10.0
) : DiscountStrategy {
    override val name: String = "Seasonal Coupon ($couponCode)"
    override val description: String = "Fixed $${"%.2f".format(discountAmount)} coupon discount."

    override fun applyDiscount(rawTotal: Double): Double {
        return (rawTotal - discountAmount).coerceAtLeast(0.0)
    }
}
