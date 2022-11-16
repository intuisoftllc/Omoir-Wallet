package com.intuisoft.plaid.common.local.db

import androidx.room.TypeConverter

class FloatListItemConverter {
    @TypeConverter
    fun fromListType(type: List<Float>) : String {
        return type.toMutableList().joinToString(",")
    }

    @TypeConverter
    fun toListType(type: String): List<Float> {
        return type.split(",").map { it.toFloat() }
    }
}
