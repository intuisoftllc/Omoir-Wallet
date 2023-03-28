package com.intuisoft.plaid.common.local.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.intuisoft.plaid.common.model.BlacklistedAddressModel
import com.intuisoft.plaid.common.model.BlacklistedTransactionModel
import com.intuisoft.plaid.common.model.ExchangeInfoDataModel
import java.time.Instant

@Entity(tableName = "transaction_blacklist")
data class TransactionBlacklist(
    @PrimaryKey(autoGenerate = false)  @ColumnInfo(name = "id") var txId: String,
    @ColumnInfo(name = "wallet_id") var walletId: String
) {
    fun from() =
        BlacklistedTransactionModel(
            txId = txId,
            walletId = walletId
        )

    companion object {

        fun consume(txId: String, walletId: String) =
            TransactionBlacklist(
                txId = txId,
                walletId = walletId
            )
    }
}