package com.intuisoft.plaid.common.local.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.intuisoft.plaid.common.model.*
import java.time.Instant

@TypeConverters(value = [
    AssetTransferStatusConverter::class,
    StringListItemConverter::class
])
@Entity(tableName = "asset_transfers")
data class AssetTransfer(
    @PrimaryKey(autoGenerate = false) @ColumnInfo(name = "id") var id: String,
    @ColumnInfo(name = "created_at") var createdAt: Long,
    @ColumnInfo(name = "wallet_id") var walletId: String,
    @ColumnInfo(name = "recipient_wallet") var recipientWallet: String,
    @ColumnInfo(name = "batch_gap") val batchGap: Int,
    @ColumnInfo(name = "batch_size") val batchSize: Int,
    @ColumnInfo(name = "expected_amount") val expectedAmount: Long,
    @ColumnInfo(name = "retries") val retries: Long,
    @ColumnInfo(name = "sent") val sent: Long,
    @ColumnInfo(name = "fees_paid") val feesPaid: Long,
    @ColumnInfo(name = "fee_range_low") val feeRangeLow: Int,
    @ColumnInfo(name = "fee_range_high") val feeRangeHigh: Int,
    @ColumnInfo(name = "dynamic_fees") val dynamicFees: Boolean,
    @ColumnInfo(name = "status") val status: AssetTransferStatus,
    @ColumnInfo(name = "batches") val batches: List<String>
) {
    fun from() =
        AssetTransferModel(
            id = id,
            walletId = walletId,
            recipientWallet = recipientWallet,
            createdAt = createdAt,
            batchGap = batchGap,
            batchSize = batchSize,
            expectedAmount = expectedAmount,
            sent = sent,
            retries = retries,
            feeRangeLow = feeRangeLow,
            feeRangeHigh = feeRangeHigh,
            feesPaid = feesPaid,
            dynamicFees = dynamicFees,
            status = status,
            batches = batches
        )

    companion object {

        fun consume(
            id: String,
           walletId: String,
           recipientWallet: String,
           createdAt: Long,
           batchGap: Int,
           batchSize: Int,
           expectedAmount: Long,
           retries: Long,
           sent: Long,
           feesPaid: Long,
           feeRangeLow: Int,
           feeRangeHigh: Int,
           dynamicFees: Boolean,
           status: AssetTransferStatus,
           batches: List<String>
        ) =
            AssetTransfer(
                id = id,
                walletId = walletId,
                recipientWallet = recipientWallet,
                batchGap = batchGap,
                createdAt = createdAt,
                batchSize = batchSize,
                expectedAmount = expectedAmount,
                retries = retries,
                sent = sent,
                feesPaid = feesPaid,
                feeRangeLow = feeRangeLow,
                feeRangeHigh = feeRangeHigh,
                dynamicFees = dynamicFees,
                status = status,
                batches = batches
            )
    }
}