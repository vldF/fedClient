package fed

import fed.api.Message
import java.lang.Integer.max
import kotlin.math.ceil


class Chat {
    private val messages = mutableListOf<Message>()

    val size
        get() = messages.size
    val lastTime
        get() = messages.last().time

    fun add(m: Message) = messages.add(m)

    fun addAll(m: List<Message>) = messages.addAll(m)

    fun getText(width: Int, height: Int, offset: Int): String {
        var linesCount = 0
        val result = StringBuilder()
        val messagesIter = messages.listIterator(messages.size)
        while (messagesIter.hasPrevious() && linesCount < height + offset) {
            val message = messagesIter.previous()
            val messageText = "[${message.senderNick}]: ${message.message}\n"
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