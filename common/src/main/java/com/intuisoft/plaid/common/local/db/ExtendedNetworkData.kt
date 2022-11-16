package com.intuisoft.plaid.common.local.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.intuisoft.plaid.common.model.ExtendedNetworkDataModel

@Entity(tableName = "extended_network_data")
data class ExtendedNetworkData(
    @ColumnInfo(name = "testnet_data") var testNetData: Boolean,
    @PrimaryKey(autoGenerate = false)  @ColumnInfo(name = "id") var id: Int = if(testNetData) 1 else 0,
    @ColumnInfo(name = "height") var height: Int,
    @ColumnInfo(name = "difficulty") var difficulty: Long,
    @ColumnInfo(name = "blockchain_size") var blockchainSize: Long,
    @ColumnInfo(name = "avg_tx_size") var avgTxSize: Int,
    @ColumnInfo(name = "avg_fee_rate") var avgFeeRate: Int,
    @ColumnInfo(name = "unconfirmed_txs") var unconfirmedTxs: Int,
    @ColumnInfo(name = "avg_conf_time") var avgConfTime: Double
) {
    fun from() =
        ExtendedNetworkDataModel(
            height = height,
            difficulty = difficulty,
            blockchainSize = blockchainSize,
            avgTxSize = avgTxSize,
            avgFeeRate = avgFeeRate,
            unconfirmedTxs = unconfirmedTxs,
            avgConfTime = avgConfTime
        )

    companion object {

        fun consume(testNetWallet: Boolean, extendedData: ExtendedNetworkDataModel) =
            ExtendedNetworkData(
                testNetData = testNetWallet,
                height = extendedData.height,
                difficulty = extendedData.difficulty,
                blockchainSize = extendedData.blockchainSize,
                avgTxSize = extendedData.avgTxSize,
                avgFeeRate = extendedData.avgFeeRate,
                unconfirmedTxs = extendedData.unconfirmedTxs,
                avgConfTime = extendedData.avgConfTime
            )
    }
}