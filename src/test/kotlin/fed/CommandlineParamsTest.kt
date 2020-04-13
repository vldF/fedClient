package fed

import org.junit.Assert
import org.junit.Test
import ru.vldf.fed.exceptions.AccountErrorException
import ru.vldf.fed.exceptions.WrongArgumentException
import server

/**
 * Testing command line params.
 */
class CommandlineParamsTest {
    @Test
    fun withParam() {
        val args = arrayOf("--with", "vldf", "-s", server)
        Assert.assertTrue(Main(*args).checkDataIsCorrect())
    }

    @Test(expected = WrongArgumentException::class)
    fun wrongWithParam() {
        Main()
    }

    @Test(expected = AccountErrorException::class)
    fun checkWrongUser() {
        Main("--with", "user", "-s", server)
    }
}
