import fed.api.Api
import org.junit.AfterClass
import org.junit.Assert
import org.junit.BeforeClass
import org.junit.Test
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

    @Test
    fun testContent() {
        val file = File("./users/$userName")
        Assert.assertEquals(2, file.readLines().size)
        Assert.assertEquals(userName, file.readLines()[1])
        Assert.assertTrue(file.readLines()[0].isNotEmpty())
    }
}