package com.intuisoft.plaid.common.network.adapters

import android.os.Build
import androidx.annotation.RequiresApi
import com.google.gson.*
import java.lang.reflect.Type
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class InstantAdapter : JsonDeserializer<Instant>, JsonSerializer<Instant> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): Instant {
        val date = json?.asString
        return Instant.parse(date)
    }

    override fun serialize(
        src: Instant?,
        typeOfSrc: Type?,
        context: JsonSerializationContext?
    ): JsonElement {
        val json = DateTimeFormatter.ISO_INSTANT.format(src)
        return JsonPrimitive(json)
    }
}
