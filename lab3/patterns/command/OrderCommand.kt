package lab3.patterns.command

/**
 * Command Pattern: Command interface.
 * Encapsulates an order action as an object, providing execute and undo capabilities.
 */
interface OrderCommand {
    val name: String
    fun execute(): Boolean
    fun undo(): Boolean
    fun getDescription(): String
}
