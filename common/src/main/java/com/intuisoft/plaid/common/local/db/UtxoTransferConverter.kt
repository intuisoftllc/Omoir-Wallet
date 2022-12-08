package com.intuisoft.plaid.common.local.db

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.intuisoft.plaid.common.model.AssetTransferStatus
import com.intuisoft.plaid.common.model.UtxoTransfer
import java.time.Instant
import java.time.format.DateTimeFormatter

class UtxoTransferConverter {
    @TypeConverter
    fun fromType(type: List<UtxoTransfer>) : String {
        return Gson().toJson(type)
    }

    @TypeConverter
    fun toType(type: String): List<UtxoTransfer> {
        return Gson().fromJson(type, List::class.java) as List<UtxoTransfer>
    }
}
