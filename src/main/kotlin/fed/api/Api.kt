package fed.api

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.Request
import java.util.concurrent.TimeUnit

class Api(private val nickname: String, private val token: String) {
    private val baseUrl = "http://localhost:35309/"
    private val client = OkHttpClient()
    private var userId = -1
    private val messagesType = object: TypeToken<List<Message>>() {}.type

    init {
        client.setConnectTimeout(10L, TimeUnit.SECONDS)
        val resp = execute("account.getUserInfo", mapOf(
            "nick" to nickname
        ))
        userId = resp["id"].asInt
    }

    private fun execute(method: String, params: Map<String, Any>): JsonObject {
        val paramsString = params.map { "${it.key}=${it.value}" }.joinToString("&")

        val req = Request.Builder()
            .url("$baseUrl$method?$paramsString&token=$token")
            .build()
        //println("$baseUrl$method?$paramsString&token=$token")
        val resp = JsonParser.parseString(client.newCall(req).execute().body()!!.string()).asJsonObject
        //println(resp)
        return resp
    }

    fun getAllMessages(fromId: Int): List<Message> {
        return Gson().fromJson(execute("messages.get", mapOf(
            "userid" to userId,
            "by" to fromId
        ))["data"].asJsonArray, messagesType)
    }

    fun getLastMessages(fromId: Int, time: Long): List<Message> {
        return Gson().fromJson(execute("messages.getLast", mapOf(
            "userid" to userId,
            "by" to fromId,
            "last_time" to time
        ))["data"].asJsonArray, messagesType)
    }

    fun messageSend(toId: Int, msg: String): JsonObject {
        return execute("message.send", mapOf(
            "sender" to userId,
            "receiver" to toId,
            "message" to msg,
            "token" to token
        ))
    }

    fun register(nick: String): JsonObject {
        return execute("account.register", mapOf(
            "nick" to nick
        ))
    }
}