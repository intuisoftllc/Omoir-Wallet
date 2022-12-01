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
    @ColumnInfo(name = "unconfirmed_txs") var unconfirmedTxs: Int,
    @ColumnInfo(name = "avg_conf_time") var avgConfTime: Double,
    @ColumnInfo(name = "nodes_on_network") var nodesOnNetwork: Int,
    @ColumnInfo(name = "mem_pool_size") var memPoolSize: Long,
    @ColumnInfo(name = "tx_per_second") var txPerSecond: Int,
    @ColumnInfo(name = "addresses_with_balance") var addressesWithBalance: Long
) {
    fun from() =
        ExtendedNetworkDataModel(
            height = height,
            difficulty = difficulty,
            blockchainSize = blockchainSize,
            nodesOnNetwork = nodesOnNetwork,
            memPoolSize = memPoolSize,
            txPerSecond = txPerSecond,
            unconfirmedTxs = unconfirmedTxs,
            addressesWithBalance = addressesWithBalance,
            avgConfTime = avgConfTime
        )

    companion object {

        fun consume(testNetWallet: Boolean, extendedData: ExtendedNetworkDataModel) =
            ExtendedNetworkData(
                testNetData = testNetWallet,
                height = extendedData.height,
                difficulty = extendedData.difficulty,
                blockchainSize = extendedData.blockchainSize,
                memPoolSize = extendedData.memPoolSize,
                txPerSecond = extendedData.txPerSecond,
                unconfirmedTxs = extendedData.unconfirmedTxs,
                addressesWithBalance = extendedData.addressesWithBalance,
                nodesOnNetwork = extendedData.nodesOnNetwork,
                avgConfTime = extendedData.avgConfTime
            )
    }
}