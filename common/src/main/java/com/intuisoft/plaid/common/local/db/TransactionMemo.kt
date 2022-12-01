package com.intuisoft.plaid.common.local.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.intuisoft.plaid.common.model.TransactionMemoModel

@Entity(tableName = "transaction_memo")
data class TransactionMemo(
    @PrimaryKey(autoGenerate = false)  @ColumnInfo(name = "transaction_id") var transactionId: String,
    @ColumnInfo(name = "memo") var memo: String
) {
    fun from() =
        TransactionMemoModel(
            transactionId = transactionId,
            memo = memo
        )

    companion object {

        fun consume(transactionId: String, memo: String) =
            TransactionMemo(
                transactionId = transactionId,
                memo = memo
            )
    }
}