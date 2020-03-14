package fed

import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.gui2.*
import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.input.KeyType
import com.googlecode.lanterna.screen.TerminalScreen
import com.googlecode.lanterna.terminal.DefaultTerminalFactory
import com.googlecode.lanterna.terminal.Terminal
import fed.api.Api
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean


class Main {
    private val terminal: Terminal = DefaultTerminalFactory().createTerminal()
    private val screen = TerminalScreen(terminal)
    private val window = BasicWindow()
    private val panel = Panel(LinearLayout(Direction.VERTICAL))
    private val input = TextBox(TerminalSize(terminal.terminalSize.rows, 1))

    private var maxColumns = terminal.terminalSize.columns
    private var maxRows = terminal.terminalSize.rows
    private var chatOffset = 0
    private lateinit var api: Api
    private var lastAddedChatTextLen = 0

    private lateinit var token: String
    private lateinit var nick: String
    private var chatWith = 2

    fun main() {
        screen.startScreen()
        window.component = panel
        window.setHints(listOf(Window.Hint.FULL_SCREEN))
        panel.addComponent(input)

        val textGUI = MultiWindowTextGUI(screen)
        input.takeFocus()
        messageCheckerDaemon()
        window.addWindowListener(keyListener)
        textGUI.addWindowAndWait(window)
    }

    private fun messageCheckerDaemon() {
        val chat = Chat()
        val configFile = File("fedConfig").readLines()
        var oldOffset = chatOffset
        var oldMaxColumns = maxColumns
        var oldMaxRows = maxRows

        token = configFile[0]
        nick = configFile[1]

        api = Api(nick, token)

        val messagePanel = Panel()
        panel.addComponent(messagePanel)

        val messages = api.getAllMessages(chatWith)
        for (m in messages) {
            chat.add(m)
        }
        var chatText = chat.getText(maxColumns, maxRows, chatOffset - 1)
        lastAddedChatTextLen = chatText.lines().size
        messagePanel.addComponent(Label(chatText))

        Thread(Runnable {
            while (true) {
                Thread.sleep(100)
                val newMessages = api.getLastMessages(
                    chatWith,
                    if (chat.size != 0)
                        chat.lastTime
                    else
                        0
                )

                // checking for messages count (empty or not) and window size and chat offset didn't changed
                if (
                    newMessages.isEmpty() &&
                    oldOffset == chatOffset &&
                    oldMaxColumns == maxColumns &&
                    oldMaxRows == oldMaxRows
                ) continue

                oldOffset = chatOffset
                oldMaxColumns = maxColumns
                oldMaxRows = maxRows

                chat.addAll(newMessages)
                chatText = chat.getText(maxColumns, maxRows, chatOffset - 1)
                lastAddedChatTextLen = chatText.lines().size
                messagePanel.removeAllComponents()
                messagePanel.addComponent(Label(chatText))
            }
        }).start()
    }

    private val keyListener = object : WindowListener {
        override fun onInput(basePane: Window?, keyStroke: KeyStroke?, deliverEvent: AtomicBoolean?) {
            when (keyStroke?.keyType ?: return) {
                KeyType.Enter -> {
                    api.messageSend(chatWith, input.text)
                    input.text = ""
                }
                KeyType.ArrowUp -> if (chatOffset > 0) chatOffset--
                KeyType.ArrowDown -> {
                    if (lastAddedChatTextLen > 10)
                        chatOffset++
                }
                else -> {
                }
            }
        }

        override fun onMoved(window: Window?, oldPosition: TerminalPosition?, newPosition: TerminalPosition?) {
        }

        override fun onResized(window: Window, oldSize: TerminalSize?, newSize: TerminalSize) {
            maxColumns = newSize.columns
            maxRows = newSize.rows
        }

        override fun onUnhandledInput(basePane: Window?, keyStroke: KeyStroke?, hasBeenHandled: AtomicBoolean?) {
            return
        }

    }
}


fun main() {
    val m = Main()
    m.main()
}