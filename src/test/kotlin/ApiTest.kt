import fed.api.Api
import org.junit.*
import java.util.logging.LogManager
import kotlin.random.Random

/**
 * Base tests for checking API.
 */
internal class ApiTest {
    private var userId = -1
    private lateinit var userName: String
    private lateinit var message: String
    private lateinit var api: Api

    @Test
    fun testAll() {
        register()
        messageSendAndGetLast()
    }

    private fun register() {
        userName = generateRandomString()
        api = Api(userName, serverAddress = server)
    }

    private fun messageSendAndGetLast() {
        val time = System.currentTimeMillis() - 10*1000 // 10*1000 = 10 sec. Inaccuracy due to time deviation

        val rnd = generateRandomString()
        message = "test$rnd"
        val respSend = api.messageSend(userId, message)

        Assert.assertTrue(respSend.has("status") && respSend["status"].asString == "ok")

        val lastMessages = api.getLastMessages(userId, time)

        Assert.assertTrue(lastMessages.isNotEmpty())
        Assert.assertEquals(message, lastMessages.last().message)
    }

    @After
    fun closeLogs() {
        LogManager.getLogManager().reset()
    }
}
