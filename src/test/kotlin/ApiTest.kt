import fed.api.Api
import org.junit.*
import kotlin.random.Random

/**
 * Base tests for checking API.
 */
internal class ApiTest {
    private var userId = -1
    private lateinit var userName: String
    private lateinit var userToken: String
    private lateinit var message: String
    private lateinit var api: Api

    private val random by lazy { Random }

    private fun generateRandomString(): String {
        return (1..32).map { (random.nextInt('A'.toInt(), 'Z'.toInt()).toChar()) }.joinToString(separator = "")
    }

    @Test
    fun testAll() {
        register()
        messageSendAndGetLast()
    }


    private fun register() {
        userName = generateRandomString()

        val regApi = Api(userName, "", serverAddress = "localhost")
        val registerResp = regApi.register()
        Assert.assertTrue(registerResp.has("status"))
        Assert.assertTrue(registerResp.has("token"))
        Assert.assertEquals("ok", registerResp["status"].asString)

        userToken = registerResp["token"].asString
        Assert.assertTrue(userToken.isNotEmpty())

        api = Api(userName, userToken, serverAddress = "localhost")
        userId = api.userId
        Assert.assertTrue(userId != -1)
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
}