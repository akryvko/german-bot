package tk.germanbot.data.es

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type
import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter.ISO_DATE_TIME


class InstantConverter: JsonSerializer<Instant>, JsonDeserializer<Instant> {
    @Throws(JsonParseException::class)
    override fun deserialize(jsonElement: JsonElement, type: Type, jsonDeserializationContext: JsonDeserializationContext): Instant {
        return ZonedDateTime.parse(jsonElement.asString, ISO_DATE_TIME).toInstant()
    }

    override fun serialize(date: Instant, type: Type, jsonSerializationContext: JsonSerializationContext): JsonElement {
        return JsonPrimitive(ZonedDateTime.ofInstant(date, ZoneOffset.UTC).format(ISO_DATE_TIME))
    }
}