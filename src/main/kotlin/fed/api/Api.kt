package fed.api

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.Request
import fed.exceptions.AccountErrorException
import java.lang.Exception
import java.util.concurrent.TimeUnit

/**
 * Api for server
 * @param nickname: username on server.
 * @param token: user's secret token. If token is empty, userId will not be got (use this variant for
 * registration new user).
 */
class Api(private val nickname: String, private val token: String) {
    private val baseUrl = "http://localhost:35309/"
    private val client = OkHttpClient()
    var userId = -1
    private val messagesType = object: TypeToken<List<Message>>() {}.type

    init {
        client.setConnectTimeout(10L, TimeUnit.SECONDS)
        if (token.isNotEmpty()) {
            // if user have account (token was set), we get user's ID
            val resp = execute(
                "account.getOwnInfo", mapOf(
                    "nick" to nickname
                )
            )
            userId = resp["id"].asInt
        }
    }

    /**
     * Base function for server's REST API.
     * @param method: servers' API method name.
     * @param params: map like {"key" to value}. Key should be String, value can be String, Int, Boolean or Double.
     */
    private fun execute(method: String, params: Map<String, Any>): JsonObject {
        val paramsString = params.map { "${it.key}=${it.value}" }.joinToString("&")

        val req = Request.Builder()
            .url("$baseUrl$method?$paramsString&token=$token")
            .build()
        val resp = client.newCall(req).execute().body().string()
        try {
            return JsonParser.parseString(resp).asJsonObject
        } catch (e: Exception) {
            // message for debugging. It will shown only if program run in emulation mode
            println(resp)
            println("$baseUrl$method?$paramsString&token=$token")
            throw e
        }
    }

    /**
     * Get all message between user with fromId and current user, that was sent since time.
     * @param fromId: second user ID.
     * @param time: UNIX time. Set this param to 0, if you would to get all messages.
     */
    fun getLastMessages(fromId: Int, time: Long): List<Message> = Gson().fromJson(
        execute("messages.getLast", mapOf(
            "userid" to userId,
            "by" to fromId,
            "last_time" to time
        ))["data"].asJsonArray, messagesType)

    /**
     * Sent new message.
     * @param toId: ID of user, that receive this message.
     * @param msg: message.
     */
    fun messageSend(toId: Int, msg: String): JsonObject = execute("messages.send", mapOf(
            "sender" to userId,
            "receiver" to toId,
            "message" to msg,
            "token" to token
        ))

    /**
     * Register new account.
     * If this account exist on the server, error will return. Else new secret token will return.
     */
    fun register(): JsonObject = execute("account.register", mapOf(
            "nick" to nickname
        ))

    /**
     * Get user's ID.
     * @param nick: user's nick, whose ID you would to get.
     */
    fun getUserId(nick: String): Int = execute("users.getUserId", mapOf(
            "nick" to nick,
            "userid" to userId
        ))["id"]?.asInt ?: throw AccountErrorException()
}