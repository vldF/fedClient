package fed.api

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.Request
import fed.exceptions.AccountErrorException
import fed.fileSeparator
import fed.newLine
import java.io.File
import java.util.concurrent.TimeUnit
import java.util.logging.FileHandler
import java.util.logging.Logger
import java.util.logging.SimpleFormatter

/**
 * Api for server
 * @param nickname: username on server.
 * @param serverAddress: server address. If it doesn't contains port, default will be added (35309)
 */
class Api(private val nickname: String, private val serverAddress: String) {
    private val baseUrl = "http://${if (serverAddress.contains(':')) serverAddress else "$serverAddress:35309"}/"
    private val client = OkHttpClient()
    private val messagesType = object: TypeToken<List<Message>>() {}.type

    private val log = Logger.getLogger("api")

    private val token: String

    init {
        val handler = FileHandler("api.log", true)
        handler.formatter = SimpleFormatter()
        log.handlers.forEach { log.removeHandler(it) }
        log.addHandler(handler)

        client.setConnectTimeout(10L, TimeUnit.SECONDS)
        val configFile = File("users$fileSeparator$nickname")
        if (!configFile.exists()) {
            // account doesn't exist. Trying to create new
            register()
        }

        val config = configFile.readLines()
        token = config[0]
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
            log.severe(resp)
            log.severe("$baseUrl$method?$paramsString&token=$token")
            throw e
        }
    }

    /**
     * Get all message between user with fromId and current user, that was sent since time.
     * @param fromId: second user ID.
     * @param time: UNIX time. Set this param to 0, if you would to get all messages.
     */
    fun getLastMessages(fromId: Int, time: Long): List<Message> {
        val resp = execute("messages.getLast", mapOf(
                    "receiver" to fromId,
                    "last_time" to time
                ))

        if (resp.has("error"))
            throw AccountErrorException()
        return Gson().fromJson(resp["data"].asJsonArray, messagesType)
    }

    /**
     * Sent new message.
     * @param toId: ID of user, that receive this message.
     * @param msg: message.
     */
    fun messageSend(toId: Int, msg: String): JsonObject = execute("messages.send", mapOf(
            "receiver" to toId,
            "message" to msg,
            "token" to token
        ))

    /**
     * Register new account.
     * If this account exist on the server, error will return. Else new secret token will return.
     */
    private fun register() {
        val resp = execute("account.register", mapOf(
            "nick" to nickname
        ))

        if (resp.has("error") && resp["error"].asBoolean || !resp.has("token")) {
            System.err.println("Error. ${resp["description"]}")
            throw AccountErrorException()
        } else {
            val token = resp["token"].asString
            File("users$fileSeparator$nickname").createNewFile()
            val configFile = File("users$fileSeparator$nickname")
            configFile.writeText("$token$newLine$nickname")

            configFile.readLines()
        }
    }

    /**
     * Get user's ID.
     * @param nick: user's nick, whose ID you would to get.
     */
    fun getUserId(nick: String): Int = execute("users.getUserId", mapOf(
            "nick" to nick
        ))["id"]?.asInt ?: throw AccountErrorException()
}