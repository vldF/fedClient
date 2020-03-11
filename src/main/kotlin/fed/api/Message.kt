package fed.api

import com.google.gson.*
import java.lang.reflect.Type

data class Message(val sender: Int, val receiver: Int, val message: String) : JsonDeserializer<Message> {
    override fun deserialize(json: JsonElement, typeOfT: Type?, context: JsonDeserializationContext?): Message {
        val obj = json.asJsonObject
        return Message(
            obj["by"].asInt,
            obj["receiver"].asInt,
            obj["msg"].asString
        )
    }
}