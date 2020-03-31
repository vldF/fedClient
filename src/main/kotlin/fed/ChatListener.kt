package fed

import fed.api.Message

interface ChatListener {
    fun onMessages(messages: Collection<Message>)
    fun onScroll(lines: Int)
}
