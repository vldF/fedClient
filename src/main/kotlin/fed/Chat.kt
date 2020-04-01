package fed

import fed.api.Message
import java.lang.Integer.max
import kotlin.math.ceil

/**
 * This class stores text on user's screen and return part of it, that user need.
 */
class Chat {
    private val messages = mutableListOf<Message>()

    fun addAll(m: Collection<Message>) = messages.addAll(m)

    /**
     * This function return's text, that could be shown on user's screen.
     * @param width: chars on horizontal dim.
     * @param height: chars on vertical dim.
     * @param offset: lines, that user scroll.
     */
    fun getText(width: Int, height: Int, offset: Int): String {
        var linesCount = 0
        val result = StringBuilder()
        val messagesIter = messages.listIterator(messages.size)

        while (messagesIter.hasPrevious() && linesCount < height + offset) {
            val message = messagesIter.previous()
            val messageText = "[${message.senderNick}]: ${message.message}$newLine"
            val currentMessageHeight = max(1, ceil(messageText.length * 1.0 / width).toInt())

            if (linesCount > offset && linesCount + currentMessageHeight <= height + offset) {
                result.append(messageText)
            } else if (linesCount > offset && linesCount + currentMessageHeight >= height + offset) {
                val needToAdd = offset + height - linesCount
                result.append(messageText.substring(0, needToAdd*width))
                break
            }

            linesCount += currentMessageHeight
        }

        return result.toString()
    }
}