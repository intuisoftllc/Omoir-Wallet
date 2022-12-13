package com.intuisoft.plaid.common.local.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.intuisoft.plaid.common.model.*
import java.time.Instant

@TypeConverters(value = [AssetTransferStatusConverter::class, UtxoTransferConverter::class])
@Entity(tableName = "batch_data")
data class BatchData(
    @PrimaryKey(autoGenerate = false) @ColumnInfo(name = "id") var id: String,
    @ColumnInfo(name = "batch_number") var batchNumber: Int,
    @ColumnInfo(name = "completion_height") var completionHeight: Int,
    @ColumnInfo(name = "blocks_remaining") var blocksRemaining: Int,
    @ColumnInfo(name = "transfer_id") var transferId: String,
    @ColumnInfo(name = "utxos") val utxos: List<UtxoTransfer>,
    @ColumnInfo(name = "status") val status: AssetTransferStatus
) {
    fun from() =
        BatchDataModel(
            id = id,
            transferId = transferId,
            batchNumber = batchNumber,
            completionHeight = completionHeight,
            blocksRemaining = blocksRemaining,
            utxos = utxos,
            status = status
        )

    companion object {

        fun consume(
            id: String,
            transferId: String,
            batchNumber: Int,
            completionHeight: Int,
            blocksRemaining: Int,
            utxos: List<UtxoTransfer>,
            status: AssetTransferStatus
        ) =
            BatchData(
                id = id,
                transferId = transferId,
                batchNumber = batchNumber,
                completionHeight = completionHeight,
                blocksRemaining = blocksRemaining,
                utxos = utxos,
                status = status
            )
    }
}