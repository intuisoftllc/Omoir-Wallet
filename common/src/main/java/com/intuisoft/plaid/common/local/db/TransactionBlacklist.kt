package com.intuisoft.plaid.common.local.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.intuisoft.plaid.common.model.BasicNetworkDataModel
import com.intuisoft.plaid.common.model.BlacklistedAddressModel
import com.intuisoft.plaid.common.model.BlacklistedTransactionModel
import com.intuisoft.plaid.common.model.ExchangeInfoDataModel
import java.time.Instant

@Entity(tableName = "transaction_blacklist")
data class TransactionBlacklist(
    @PrimaryKey(autoGenerate = false)  @ColumnInfo(name = "id") var txId: String
) {
    fun from() =
        BlacklistedTransactionModel(
            txId = txId
        )

    companion object {

        fun consume(txId: String) =
            TransactionBlacklist(
                txId = txId
            )
    }
}