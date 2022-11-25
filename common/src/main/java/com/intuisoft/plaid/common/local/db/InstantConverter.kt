package com.intuisoft.plaid.common.local.db

import androidx.room.TypeConverter
import java.time.Instant
import java.time.format.DateTimeFormatter

class InstantConverter {
    @TypeConverter
    fun fromInstantType(type: Instant) : String {
        return DateTimeFormatter.ISO_INSTANT.format(type)
    }

    @TypeConverter
    fun toInstantType(type: String): Instant {
        return Instant.parse(type)
    }
}
