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
import fed.exceptions.AccountErrorException
import fed.exceptions.ChatBaseException
import fed.exceptions.InternetConnectionException
import fed.exceptions.WrongArgumentException
import org.kohsuke.args4j.CmdLineException
import org.kohsuke.args4j.CmdLineParser
import org.kohsuke.args4j.Option
import java.io.File
import java.io.FileNotFoundException
import java.net.ConnectException
import java.nio.charset.Charset
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.system.exitProcess


class Main(vararg args: String) {
    private val terminal: Terminal = DefaultTerminalFactory(System.out, System.`in`, Charset.forName("UTF-8"))
        //.setForceTextTerminal(true)
        .createTerminal()
    private val screen = TerminalScreen(terminal)
    private val window = BasicWindow()
    private val panel = Panel(LinearLayout(Direction.VERTICAL))
    private lateinit var input: TextBox

    private var maxColumns = terminal.terminalSize.columns
    private var maxRows = terminal.terminalSize.rows
    private var chatOffset = 0
    private var api: Api
    private var lastAddedChatTextLen = 0
    private var nick: String
    private var token: String

    private var isClosed = false

    @Option(name = "--with", aliases = ["-w"], usage = withParameterDescriptor, required=true)
    private var withParameter: String = ""
    private var userInChatId = -1

    @Option(name="--user", aliases = ["-u"], usage = userParameterDescriptor)
    private var userName = ""

    init {
        val parser = CmdLineParser(this)
        try {
            parser.parseArgument(*args)
        } catch (e: CmdLineException) {
            System.err.println("No argument passed:\n--with parameter is empty")
            throw WrongArgumentException()
        }

        // loading config
        if(userName.isEmpty()) {
            userName = File("users").listFiles()?.first()?.nameWithoutExtension ?: ""
            if (userName.isEmpty()) {
                System.err.println("You haven't any accounts. Create new using --user NAME flag")
                throw AccountErrorException()
            }
        }

        val config: List<String> =
        try {
            val configFile = File("users/$userName")
            configFile.readLines()
        } catch (_: FileNotFoundException) {
            // user set username, but account doesn't exist. Trying to register new
            val localApi = try {
                 Api(userName, "")
            } catch (_: ConnectException) {
                System.err.println("Please, check your internet connection")
                throw InternetConnectionException()
            }
            val resp = localApi.register()
            if (resp["status"].asString == "error") {
                System.err.println("Error. May be this username already in use")
                throw AccountErrorException()
            } else {
                val token = resp["token"].asString
                File("users/$userName").createNewFile()
                val configFile = File("users/$userName")
                configFile.writeText("$token\n$userName")

                configFile.readLines()
            }
        }

        token = config[0]
        nick = config[1]

        try {
            api = Api(nick, token)
        }catch (_: ConnectException) {
            System.err.println("Connection error. Please, check your internet connection")
            throw InternetConnectionException()
        }
        userInChatId = api.getUserId(withParameter)
    }

    fun main() {
        try {
            screen.startScreen()
            window.component = panel
            window.setHints(listOf(Window.Hint.FULL_SCREEN))

            input = TextBox(TerminalSize(screen.terminalSize.columns, 1))

            panel.addComponent(input)

            val textGUI = MultiWindowTextGUI(screen)
            input.takeFocus()
            messageCheckerDaemon()
            window.addWindowListener(keyListener)
            textGUI.addWindowAndWait(window)
        } finally {
            isClosed = true
        }
    }

    private fun messageCheckerDaemon() {
        val chat = Chat()
        var chatText: String
        var oldOffset = chatOffset
        var oldMaxColumns = maxColumns
        var oldMaxRows = maxRows
        val messagePanel = Panel()

        api = Api(nick, token)
        panel.addComponent(messagePanel)

        Thread(Runnable {
            while (!isClosed) {
                Thread.sleep(100)
                val newMessages = try{
                    api.getLastMessages(
                        userInChatId,
                        if (chat.size != 0)
                            chat.lastTime
                        else 0
                    )
                } catch (_: ConnectException) {
                    System.err.println("Connection trouble")
                    throw InternetConnectionException()
                }

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
                    try {
                        api.messageSend(userInChatId, input.text)
                    } catch (_: ConnectException) {
                        System.err.println("Error on sending message")
                        throw InternetConnectionException()
                    }
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

    // this method used in tests
    internal fun checkDataIsCorrect(): Boolean = nick.isNotEmpty() && token.isNotEmpty() && userInChatId != -1
}


fun main(args: Array<out String>) {
    try{
        Main(*args).main()
    } catch (_: ChatBaseException) {
        exitProcess(1)
    }
}