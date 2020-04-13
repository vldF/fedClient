package fed

import ru.vldf.fed.Message

interface ChatListener {
    fun onMessages(messages: Collection<Message>)
    fun onScroll(lines: Int)
}
