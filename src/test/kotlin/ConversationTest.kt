import fed.api.Api
import org.junit.Assert
import org.junit.Test

class ConversationTest {
    private lateinit var apiFirst: Api
    private lateinit var apiSecond: Api

    private fun createUser(): String = generateRandomString()

    @Test
    fun conversationTest() {
        val userFirst = createUser()
        val userSecond = createUser()

        apiFirst = Api(userFirst, server)
        apiSecond = Api(userSecond, server)

        val secondId = apiFirst.getUserId(userSecond)
        val firstId = apiSecond.getUserId(userFirst)
        val randomMessage = generateRandomString()
        apiFirst.messageSend(secondId, randomMessage)

        val messages = apiSecond.getLastMessages(firstId, 0)

        Assert.assertTrue(messages.isNotEmpty())
        Assert.assertTrue(messages.any{ it.message == randomMessage })
    }
}