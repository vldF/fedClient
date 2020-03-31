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
import fed.api.Message
import fed.exceptions.AccountErrorException
import fed.exceptions.ChatBaseException
import fed.exceptions.InternetConnectionException
import fed.exceptions.WrongArgumentException
import org.kohsuke.args4j.CmdLineException
import org.kohsuke.args4j.CmdLineParser
import org.kohsuke.args4j.Option
import java.io.File
import java.net.ConnectException
import java.nio.charset.Charset
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.system.exitProcess

/**
 * Main class of chat.
 * @param args: args from command line.
 */
class Main(vararg args: String) {
    private val terminal: Terminal = DefaultTerminalFactory(System.out, System.`in`, Charset.forName("UTF-8"))
        .createTerminal()
    private val screen = TerminalScreen(terminal)
    private val window = BasicWindow()
    private val panel = Panel(LinearLayout(Direction.VERTICAL))
    private val messagePanel = Panel()
    private lateinit var input: TextBox

    private var maxColumns = terminal.terminalSize.columns
    private var maxRows = terminal.terminalSize.rows
    private val minLinesOnChat = 10 // if in chat windows less, than it lines, chat can't scroll down
    private var api: Api

    private var isClosed = false

    @Option(name = "--with", aliases = ["-w"], usage = withParameterDescriptor, required=true, metaVar = "userName")
    private var withParameter: String = ""
    private var userInChatId = -1

    @Option(name="--user", aliases = ["-u"], usage = userParameterDescriptor, metaVar = "userName")
    private var userName = ""

    @Option(name="--server", aliases = ["-s"], usage = serverParameterDescription, required=true, metaVar = "server")
    private var serverAddress = ""

    @Option(name="--help", aliases = ["-h"])
    private var help: Boolean = false

    private val chatEventsListener = object : ChatListener {
        private val chat = Chat()
        private var chatOffset = 0
        private var lastAddedChatTextLen = 0

        override fun onMessages(messages: Collection<Message>) {
            chat.addAll(messages)
            redrawChat()
        }

        override fun onScroll(lines: Int) {
            if (-lines + lastAddedChatTextLen > minLinesOnChat && chatOffset + lines >= 0)
                chatOffset += lines
            else
                return

            redrawChat()
        }

        private fun redrawChat() {
            val chatText = chat.getText(maxColumns, maxRows, chatOffset - 1)
            lastAddedChatTextLen = chatText.lines().size
            messagePanel.removeAllComponents()
            messagePanel.addComponent(Label(chatText))
        }

    }

    init {
        val parser = CmdLineParser(this)
        try {
            parser.parseArgument(*args)
        } catch (e: CmdLineException) {
            when {
                help -> {
                    parser.printUsage(System.err)
                }
                withParameter.isEmpty() -> System.err.println("--with parameter is empty")
                serverAddress.isEmpty() -> System.err.println("--server parameter is empty")
            }
            throw WrongArgumentException()
        }

        // loading config
        if(userName.isEmpty()) {
            // trying to get first name from saved files
            userName = File("users").listFiles()?.first()?.nameWithoutExtension ?: ""
            if (userName.isEmpty()) {
                System.err.println("You haven't any accounts. Create new using --user NAME flag")
                throw AccountErrorException()
            }
        }

        try {
            api = Api(userName, serverAddress)
        }catch (_: ConnectException) {
            System.err.println("Connection error. Please, check your internet connection")
            throw InternetConnectionException()
        }
        userInChatId = api.getUserId(withParameter)
    }


    fun main() {
        try {
            // preparing terminal screen
            screen.startScreen()
            window.component = panel
            window.setHints(listOf(Window.Hint.FULL_SCREEN))

            input = TextBox(TerminalSize(screen.terminalSize.columns, 1))

            panel.addComponent(input)
            panel.addComponent(messagePanel)

            val textGUI = MultiWindowTextGUI(screen)
            input.takeFocus()

            // starting background thread, that will check messages for updates and attaching listener
            messageCheckerDaemon(chatEventsListener)
            keyListener.setChatListener(chatEventsListener)

            window.addWindowListener(keyListener)
            textGUI.addWindowAndWait(window)
        } finally {
            // set flag, that alerts threads about stopping program
            isClosed = true
        }
    }


    private fun messageCheckerDaemon(chatListener: ChatListener) {
        var lastTime = 0L

        Thread(Runnable {
            while (!isClosed) {
                Thread.sleep(300)
                val newMessages = try{
                    api.getLastMessages(
                        userInChatId,
                        lastTime
                    )
                } catch (_: ConnectException) {
                    System.err.println("Connection trouble")
                    exitProcess(1)
                }

                // checking messages count (empty or not) and window size, and chat offset didn't change
                if (newMessages.isEmpty()) continue

                lastTime = newMessages.last().time
                chatListener.onMessages(newMessages)
            }
        }).start()
    }


    private val keyListener = object : WindowListener {
        private lateinit var chatListener: ChatListener

        override fun onInput(basePane: Window?, keyStroke: KeyStroke?, deliverEvent: AtomicBoolean?) {
            when (keyStroke?.keyType ?: return) {
                KeyType.Enter -> {
                    try {
                        api.messageSend(userInChatId, input.text)
                    } catch (_: ConnectException) {
                        System.err.println("Error on sending message")
                        throw InternetConnectionException()
                    }
                    input.text = ""
                }
                KeyType.ArrowUp -> chatListener.onScroll(-1)
                KeyType.ArrowDown -> chatListener.onScroll(1)
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

        fun setChatListener(chatListener: ChatListener) { this.chatListener = chatListener }
    }

    // this method used in tests
    internal fun checkDataIsCorrect(): Boolean = userName.isNotEmpty() && userName.isNotEmpty() && userInChatId != -1
}


fun main(args: Array<out String>) {
    try{
        Main(*args).main()
    } catch (_: ChatBaseException) {
        exitProcess(1)
    }
}