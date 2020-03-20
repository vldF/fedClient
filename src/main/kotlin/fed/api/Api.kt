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

class Api(private val nickname: String, private val token: String) {
    private val baseUrl = "http://localhost:35309/"
    private val client = OkHttpClient()
    var userId = -1
    private val messagesType = object: TypeToken<List<Message>>() {}.type

    init {
        client.setConnectTimeout(10L, TimeUnit.SECONDS)
        if (token.isNotEmpty()) {
            // if user have account...
            val resp = execute(
                "account.getOwnInfo", mapOf(
                    "nick" to nickname
                )
            )
            userId = resp["id"].asInt
        }
    }

    private fun execute(method: String, params: Map<String, Any>): JsonObject {
        val paramsString = params.map { "${it.key}=${it.value}" }.joinToString("&")

        val req = Request.Builder()
            .url("$baseUrl$method?$paramsString&token=$token")
            .build()
        val resp = client.newCall(req).execute().body().string()
        try {
            return JsonParser.parseString(resp).asJsonObject
        } catch (e: Exception) {
            println(resp)
            println("$baseUrl$method?$paramsString&token=$token")
            throw e
        }
    }

    fun getLastMessages(fromId: Int, time: Long): List<Message> {
        return Gson().fromJson(execute("messages.getLast", mapOf(
            "userid" to userId,
            "by" to fromId,
            "last_time" to time
        ))["data"].asJsonArray, messagesType)
    }

    fun messageSend(toId: Int, msg: String): JsonObject {
        return execute("messages.send", mapOf(
            "sender" to userId,
            "receiver" to toId,
            "message" to msg,
            "token" to token
        ))
    }

    fun register(): JsonObject {
        return execute("account.register", mapOf(
            "nick" to nickname
        ))
    }

    fun getUserId(nick: String): Int {
        return execute("users.getUserId", mapOf(
            "nick" to nick,
            "userid" to userId
        ))["id"]?.asInt ?: throw AccountErrorException()
    }
}