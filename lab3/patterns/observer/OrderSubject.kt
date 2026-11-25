package lab3.patterns.observer

/**
 * Observer Pattern: Subject interface.
 * Allows dynamic runtime attachment, detachment, and event dispatching to observers.
 */
interface OrderSubject {
    fun attach(observer: OrderObserver)
    fun detach(observer: OrderObserver)
    fun notifyObservers(previousState: String, newState: String, details: String)
}
