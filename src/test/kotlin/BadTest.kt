import fed.api.Api
import fed.exceptions.AccountErrorException
import org.junit.After
import org.junit.Assert
import org.junit.Test
import java.util.logging.LogManager

class BadTest {
    private val server = "130.61.203.95"

    @Test(expected = AccountErrorException::class)
    fun badRegistration() {
        Api("", serverAddress = server)
    }

    @Test
    fun badMessageSend() {
        val api = Api("vldf2", serverAddress = server)
        val resp = api.messageSend(-1, "test")
        Assert.assertTrue(resp.has("error"))
    }

    @Test(expected = AccountErrorException::class)
    fun badGetUserId() {
        val api = Api("vldf2", serverAddress = server)
        api.getUserId("")
    }

    @Test(expected = AccountErrorException::class)
    fun badGetLastMessages() {
        val api = Api("vldf2", serverAddress = server)
        api.getLastMessages(-1, 0)
    }

    @After
    fun closeLogs() {
        LogManager.getLogManager().reset()
    }
}