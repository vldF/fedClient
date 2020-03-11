package fed.api

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.Request
import java.util.concurrent.TimeUnit

class Api(private val nickname: String, private val token: String) {
    private val baseUrl = "http://localhost:35309/"
    private val client = OkHttpClient()
    private var userId = -1
    init {
        client.setConnectTimeout(10L, TimeUnit.SECONDS)
        val resp = execute("account.getUserInfo", mapOf(
            "nick" to nickname,
            "token" to token
        ))
        userId = resp["id"].asInt
    }

    private fun execute(method: String, params: Map<String, Any>): JsonObject {
        val paramsString = params.map { "${it.key}=${it.value}" }.joinToString("&")

        val req = Request.Builder()
            .url("$baseUrl$method?$paramsString&token=$token")
            .build()
        return JsonParser.parseString(client.newCall(req).execute().body()!!.string()).asJsonObject
    }

    fun getMessages(fromId: Int): JsonArray? {
        return execute("messages.get", mapOf(
            "userid" to userId,
            "by" to fromId
        ))["data"].asJsonArray
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