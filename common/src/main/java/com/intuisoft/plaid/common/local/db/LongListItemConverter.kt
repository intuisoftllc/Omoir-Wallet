package com.intuisoft.plaid.common.local.db

import androidx.room.TypeConverter

class LongListItemConverter {
    @TypeConverter
    fun fromListType(type: List<Long>) : String {
        return type.toMutableList().joinToString(",")
    }

    @TypeConverter
    fun toListType(type: String): List<Long> {
        return type.split(",").map { it.toLong() }
    }
}
