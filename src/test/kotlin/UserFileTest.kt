import ru.vldf.fed.Api
import org.junit.AfterClass
import org.junit.Assert
import org.junit.BeforeClass
import org.junit.Test
import ru.vldf.fed.exceptions.AccountErrorException
import java.io.File

internal class UserFileTest {
    companion object {
        private val userName = System.currentTimeMillis().toString()
        @BeforeClass
        @JvmStatic fun createFile() {
            Api(userName, server)
        }

        @AfterClass
        @JvmStatic fun clean() {
            File("./users/$userName").deleteOnExit()
        }
    }

    @Test
    fun checkFileExist() {
        Assert.assertTrue(File("./users/$userName").isFile)
    }

    @Test(expected = AccountErrorException::class)
    fun registerWithUsedNick() {
        Api("vldf2", server)
    }

    @Test
    fun testContent() {
        val file = File("./users/$userName")
        Assert.assertEquals(2, file.readLines().size)
        Assert.assertEquals(userName, file.readLines()[1])
        Assert.assertTrue(file.readLines()[0].isNotEmpty())
    }
}