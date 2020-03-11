package fed.api

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.Request
import jdk.nashorn.internal.parser.JSONParser
import java.lang.StringBuilder
import java.util.concurrent.TimeUnit

class Api(private val userId: Int, private val token: String) {
    private val baseUrl = "http://localhost:35309/"
    private val client = OkHttpClient()

    init {
        client.setConnectTimeout(10L, TimeUnit.SECONDS)
    }

    private fun execute(method: String, params: Map<String, Any>): JsonObject {
        val paramsString = params.map { "${it.key}=${it.value}" }.joinToString("&")

        val req = Request.Builder()
            .url("$baseUrl$method?$paramsString&secret=$token")
            .build()
        return JsonParser.parseString(client.newCall(req).execute().body()!!.string()).asJsonObject
    }

    fun getMessages(fromId: Int): JsonObject {
        return execute("messages.get", mapOf("userid" to userId, "by" to fromId))
    }

    fun messageSend(toId: Int, msg: String): JsonObject {
        return execute("message.send", mapOf(
            "sender" to userId,
            "receiver" to toId,
            "message" to msg,
            "token" to token
        ))
    }

    fun register(nick: String) {

    }
}