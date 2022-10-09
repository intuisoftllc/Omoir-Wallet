package com.intuisoft.emojiigame.framework.db

import androidx.room.TypeConverter
import com.intuisoft.plaid.model.WalletType


class WalletTypeConverter {
    @TypeConverter
    fun fromWalletType(type: Long) : WalletType {
        return WalletType.values()[type.toInt()]
    }

    @TypeConverter
    fun longToWalletType(type: WalletType): Long {
        return type.ordinal.toLong()
    }
}