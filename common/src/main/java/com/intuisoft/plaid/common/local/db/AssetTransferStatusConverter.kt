package com.intuisoft.plaid.common.local.db

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.intuisoft.plaid.common.model.AssetTransferStatus
import java.time.Instant
import java.time.format.DateTimeFormatter

class AssetTransferStatusConverter {
    @TypeConverter
    fun fromType(type: AssetTransferStatus) : String {
        return type.ordinal.toString()
    }

    @TypeConverter
    fun toType(type: String): AssetTransferStatus {
        return AssetTransferStatus.values().find { it.ordinal == type.toInt() }!!
    }
}
