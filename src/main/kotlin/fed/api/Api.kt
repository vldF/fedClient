package fed.api

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.Request
import fed.exceptions.AccountErrorException
import fed.newLine
import java.io.File
import java.io.FileNotFoundException
import java.util.concurrent.TimeUnit
import java.util.logging.FileHandler
import java.util.logging.Logger

/**
 * Api for server
 * @param nickname: username on server.
 * @param token: user's secret token. If token is empty, userId will not be got (use this variant for
 * registration new user).
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
        log.handlers.forEach { log.removeHandler(it) }
        log.addHandler(handler)

        client.setConnectTimeout(10L, TimeUnit.SECONDS)
        val sep = File.separator
        val config: List<String> =
            try {
                val configFile = File("users$sep$nickname")
                configFile.readLines()
            } catch (_: FileNotFoundException) {
                // user set username, but account doesn't exist. Trying to register new account on the server
                val resp = this.register()

                if (resp["status"].asString == "error") {
                    System.err.println("Error. ${resp["description"]}")
                    throw AccountErrorException()
                } else {
                    val token = resp["token"].asString
                    File("users$sep$nickname").createNewFile()
                    val configFile = File("users$sep$nickname")
                    configFile.writeText("$token$newLine$nickname")

                    configFile.readLines()
                }
            }

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
    fun getLastMessages(fromId: Int, time: Long): List<Message> = Gson().fromJson(
        execute("messages.getLast", mapOf(
            "receiver" to fromId,
            "last_time" to time
        ))["data"].asJsonArray, messagesType)

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
    fun register(): JsonObject = execute("account.register", mapOf(
            "nick" to nickname
        ))

    /**
     * Get user's ID.
     * @param nick: user's nick, whose ID you would to get.
     */
    fun getUserId(nick: String): Int = execute("users.getUserId", mapOf(
            "nick" to nick
        ))["id"]?.asInt ?: throw AccountErrorException()
}