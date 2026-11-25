package lab3.patterns.command

import java.util.ArrayDeque

/**
 * Command Pattern: Invoker.
 * Decouples the sender from the receiver, tracks command history, and supports undo/redo operations.
 */
class OrderCommandInvoker {
    private val history = ArrayDeque<OrderCommand>()
    private val redoStack = ArrayDeque<OrderCommand>()

    fun executeCommand(command: OrderCommand): Boolean {
        val success = command.execute()
        if (success) {
            history.push(command)
            redoStack.clear() // Clear redo stack on new command execution
        }
        return success
    }

    fun undo(): Boolean {
        if (history.isEmpty()) {
            println("Nothing to undo.")
            return false
        }
        val command = history.pop()
        val success = command.undo()
        if (success) {
            redoStack.push(command)
        }
        return success
    }

    fun redo(): Boolean {
        if (redoStack.isEmpty()) {
            println("Nothing to redo.")
            return false
        }
        val command = redoStack.pop()
        val success = command.execute()
        if (success) {
            history.push(command)
        }
        return success
    }

    fun canUndo(): Boolean = history.isNotEmpty()

    fun canRedo(): Boolean = redoStack.isNotEmpty()

    fun getHistory(): List<OrderCommand> = history.toList()

    fun getRedoStack(): List<OrderCommand> = redoStack.toList()

    fun clear() {
        history.clear()
        redoStack.clear()
    }
}
