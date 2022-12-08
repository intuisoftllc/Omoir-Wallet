package com.intuisoft.plaid.common.local.db

import androidx.room.TypeConverter

class LongListItemConverter {
    @TypeConverter
    fun fromType(type: List<Long>) : String {
        return type.toMutableList().joinToString(",")
    }

    @TypeConverter
    fun toType(type: String): List<Long> {
        return type.split(",").map { it.toLong() }
    }
}
