package fed

import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.gui2.*
import com.googlecode.lanterna.screen.TerminalScreen
import com.googlecode.lanterna.terminal.DefaultTerminalFactory
import com.googlecode.lanterna.terminal.Terminal

val terminal: Terminal = DefaultTerminalFactory().createTerminal()
val screen = TerminalScreen(terminal)
val window = createGUI()

var maxColumns = terminal.terminalSize.columns
var maxRows = terminal.terminalSize.rows

fun main() {
    screen.startScreen()
    val textGUI = MultiWindowTextGUI(screen)

    terminal.addResizeListener { _, newSize ->
        maxColumns = newSize.columns
        maxRows = newSize.rows
        //todo repaint window
    }

    textGUI.addWindowAndWait(window)
}

private fun createGUI(): BasicWindow {
    val baseWindow = BasicWindow("Чат")

    val contentPanel = Panel(LinearLayout(Direction.VERTICAL))
    contentPanel.size = TerminalSize(100, 100)

    val messagesList = listOf(
        1 to "test message!",
        1 to "test message!",
        1 to "test message!",
        1 to "test message!"
    )

    for (m in 0..10) {
        contentPanel.addComponent(Label("test $m"))
    }

    val input = TextBox(TerminalSize(terminal.terminalSize.rows, 1))
    contentPanel.addComponent(input)
    baseWindow.component = contentPanel

    baseWindow.setHints(listOf(Window.Hint.FULL_SCREEN))

    return baseWindow
}