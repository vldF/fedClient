package fed

import fed.exceptions.AccountErrorException
import fed.exceptions.WrongArgumentException
import org.junit.Assert
import org.junit.Test
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
