package com.intuisoft.plaid.common.local.db

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.intuisoft.plaid.common.model.UtxoTransfer

class UtxoTransferConverter {
    @TypeConverter
    fun fromType(type: List<UtxoTransfer>) : String {
        return Gson().toJson(type)
    }

    @TypeConverter
    fun toType(type: String): List<UtxoTransfer> {
        return Gson().fromJson(type, object : TypeToken<List<UtxoTransfer>>() {}.getType())
    }
}
