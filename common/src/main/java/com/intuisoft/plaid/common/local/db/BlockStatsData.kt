package com.intuisoft.plaid.common.local.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.intuisoft.plaid.common.model.BlockStatsDataModel

@Entity(tableName = "block_stats_data")
data class BlockStatsData(
    @PrimaryKey(autoGenerate = false)  @ColumnInfo(name = "id") var id: String,
    @ColumnInfo(name = "testnet_data") var testNetData: Boolean,
    @ColumnInfo(name = "coin") var coin: String,
    @ColumnInfo(name = "height") var height: Int,
    @ColumnInfo(name = "difficulty") var difficulty: Long,
    @ColumnInfo(name = "blockchain_size") var blockchainSize: Long,
    @ColumnInfo(name = "unconfirmed_txs") var unconfirmedTxs: Int,
    @ColumnInfo(name = "nodes_on_network") var nodesOnNetwork: Int,
    @ColumnInfo(name = "mem_pool_size") var memPoolSize: Long,
    @ColumnInfo(name = "tx_per_second") var txPerSecond: Int,
    @ColumnInfo(name = "addresses_with_balance") var addressesWithBalance: Long,
    @ColumnInfo(name = "market_dominance") var marketDominance: Double
) {
    fun from() =
        BlockStatsDataModel(
            height = height,
            difficulty = difficulty,
            blockchainSize = blockchainSize,
            nodesOnNetwork = nodesOnNetwork,
            memPoolSize = memPoolSize,
            txPerSecond = txPerSecond,
            unconfirmedTxs = unconfirmedTxs,
            addressesWithBalance = addressesWithBalance,
            marketDominance = marketDominance
        )

    companion object {

        fun consume(testNet: Boolean, coin: String, data: BlockStatsDataModel): BlockStatsData {
            val id = "testnet: $testNet - coin: $coin"

            return BlockStatsData(
                id = id,
                testNetData = testNet,
                coin = coin,
                height = data.height,
                difficulty = data.difficulty,
                blockchainSize = data.blockchainSize,
                memPoolSize = data.memPoolSize,
                txPerSecond = data.txPerSecond,
                unconfirmedTxs = data.unconfirmedTxs,
                addressesWithBalance = data.addressesWithBalance,
                nodesOnNetwork = data.nodesOnNetwork,
                marketDominance = data.marketDominance,
            )
        }
    }
}