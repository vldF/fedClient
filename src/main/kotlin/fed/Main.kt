package fed

import com.google.gson.Gson
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.gui2.*
import com.googlecode.lanterna.screen.TerminalScreen
import com.googlecode.lanterna.terminal.DefaultTerminalFactory
import com.googlecode.lanterna.terminal.Terminal
import fed.api.Api
import fed.api.Message
import java.io.File

val terminal: Terminal = DefaultTerminalFactory().createTerminal()
val screen = TerminalScreen(terminal)
val window = BasicWindow()
val panel = Panel(LinearLayout(Direction.VERTICAL))

var maxColumns = terminal.terminalSize.columns
var maxRows = terminal.terminalSize.rows

lateinit var token: String
lateinit var nick: String
var id = -1
var chatWith = 2


fun main() {
    screen.startScreen()
    window.component = panel
    window.setHints(listOf(Window.Hint.FULL_SCREEN))
    val input = TextBox(TerminalSize(terminal.terminalSize.rows, 1))
    panel.addComponent(input)

    val textGUI = MultiWindowTextGUI(screen)
    messageCheckerDaemon()
    textGUI.addWindowAndWait(window)
}

private fun messageCheckerDaemon() {
    val configFile = File("fedConfig").readLines()
    token = configFile[0]
    nick = configFile[1]

    val api = Api(nick, token)
    val messagePanel = Panel()
    panel.addComponent(messagePanel)
    Thread(Runnable {
        var oldMessages: Array<Message>? = null
        while(true) {
            Thread.sleep(100)
            val newMessages = Gson().fromJson(api.getMessages(chatWith), Array<Message>::class.java)

            if (oldMessages != null && oldMessages.contentEquals(newMessages)) continue
            messagePanel.removeAllComponents()
            for (m in newMessages) {
                messagePanel.addComponent(Label(m.message))
            }
            oldMessages = newMessages
        }
    }).start()
}
