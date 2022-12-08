package com.intuisoft.plaid.common.local.db

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.intuisoft.plaid.common.model.UtxoTransfer

class StringListItemConverter {
    @TypeConverter
    fun fromType(type: List<String>) : String {
        return Gson().toJson(type)
    }

    @TypeConverter
    fun toType(type: String): List<String> {
        return Gson().fromJson(type, List::class.java) as List<String>
    }
}
