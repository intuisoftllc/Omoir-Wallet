package com.intuisoft.plaid.common.network.blockchair.repository

import com.intuisoft.plaid.common.model.BlockStatsDataModel
import com.intuisoft.plaid.common.model.NetworkFeeRate
import com.intuisoft.plaid.common.network.blockchair.api.BlockchairApi
import java.util.*
import kotlin.math.roundToInt

interface BlockchairRepository {
    val TAG: String
        get() = this.javaClass.simpleName

    fun getBlockStats(coin: String): Result<BlockStatsDataModel>

    fun getTestnetBlockStats(coin: String): Result<BlockStatsDataModel>

    private class Impl(
        private val api: BlockchairApi,
        private val apiKey: String?,
    ) : BlockchairRepository {

        override fun getBlockStats(coin: String): Result<BlockStatsDataModel> {
            try {
                val info = api.getBlockStats(coin, apiKey).execute()

                return Result.success(
                    BlockStatsDataModel(
                        height = info.body()!!.data.blocks,
                        difficulty = info.body()!!.data.difficulty.toLong(),
                        blockchainSize = info.body()!!.data.blockchain_size,
                        unconfirmedTxs = info.body()!!.data.mempool_transactions,
                        memPoolSize = info.body()!!.data.mempool_size,
                        nodesOnNetwork = info.body()!!.data.nodes,
                        txPerSecond = info.body()!!.data.mempool_tps.roundToInt(),
                        addressesWithBalance = info.body()!!.data.hodling_addresses,
                        marketDominance = info.body()!!.data.market_dominance_percentage
                    )
                )
            } catch (t: Throwable) {
                return Result.failure(t)
            }
        }

        override fun getTestnetBlockStats(coin: String): Result<BlockStatsDataModel> {
            try {
                val info = api.getTestnetBlockStats(coin, apiKey).execute()

                return Result.success(
                    BlockStatsDataModel(
                        height = info.body()!!.data.blocks,
                        difficulty = info.body()!!.data.difficulty.toLong(),
                        blockchainSize = info.body()!!.data.blockchain_size,
                        unconfirmedTxs = info.body()!!.data.mempool_transactions,
                        memPoolSize = info.body()!!.data.mempool_size,
                        nodesOnNetwork = info.body()!!.data.nodes,
                        txPerSecond = info.body()!!.data.mempool_tps.roundToInt(),
                        addressesWithBalance = info.body()!!.data.hodling_addresses,
                        marketDominance = info.body()!!.data.market_dominance_percentage
                    )
                )
            } catch (t: Throwable) {
                return Result.failure(t)
            }
        }
    }

    companion object {
        @JvmStatic
        fun create(
            api: BlockchairApi,
            apiKey: String?
        ): BlockchairRepository = Impl(api, apiKey)
    }
}