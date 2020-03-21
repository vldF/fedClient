package fed.api

import com.google.gson.*
import java.lang.reflect.Type

/**
 * Message class for present and deserialize from JSON message object.
 */
data class Message(
    val sender: Int,
    val senderNick: String,
    val receiver: Int,
    val message: String,
    val time: Long
) : JsonDeserializer<Message> {
    override fun deserialize(json: JsonElement, typeOfT: Type?, context: JsonDeserializationContext?): Message {
        val obj = json.asJsonObject
        return Message(
            obj["by"].asInt,
            obj["senderNick"].asString,
            obj["receiver"].asInt,
            obj["msg"].asString,
            obj["time"].asLong
        )
    }
}