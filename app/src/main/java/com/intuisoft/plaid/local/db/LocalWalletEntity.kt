package com.intuisoft.emojiigame.framework.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.intuisoft.plaid.model.WalletType


@Entity(tableName = "local_wallet")
data class LocalWallet(
    @PrimaryKey(autoGenerate = true)  @ColumnInfo(name = "id") var id: Int = 0,
    @ColumnInfo(name = "name") var name: String,
    @ColumnInfo(name = "type") var type: WalletType,
    @ColumnInfo(name = "test_net") var testNetWallet: Boolean
)
