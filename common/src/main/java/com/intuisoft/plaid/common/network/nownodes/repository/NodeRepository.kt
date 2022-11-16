package com.intuisoft.plaid.common.network.nownodes.repository

import com.intuisoft.plaid.common.model.ExtendedNetworkDataModel
import com.intuisoft.plaid.common.model.NetworkFeeRate
import com.intuisoft.plaid.common.network.nownodes.api.NodeApi
import com.intuisoft.plaid.common.network.nownodes.request.BlockStatsRequest
import com.intuisoft.plaid.common.network.nownodes.request.BlockchainInfoRequest
import com.intuisoft.plaid.common.network.nownodes.request.FeeSuggestionRequest
import java.util.*

interface NodeRepository {
    val TAG: String
        get() = this.javaClass.simpleName

    fun getFeeSuggestions(): Result<NetworkFeeRate>

    fun getExtendedNetworkData(): Result<ExtendedNetworkDataModel>

    private class Impl(
        private val api: NodeApi,
    ) : NodeRepository {

        override fun getFeeSuggestions(): Result<NetworkFeeRate> {
            try {
                val fastFeeResponse = api.getFeeSuggestion(FeeSuggestionRequest.build(UUID.randomUUID().toString(), 1)).execute().body() // ~10m
                val medFeeResponse = api.getFeeSuggestion(FeeSuggestionRequest.build(UUID.randomUUID().toString(), 6)).execute().body() // ~1hr
                val slowFeeResponse = api.getFeeSuggestion(FeeSuggestionRequest.build(UUID.randomUUID().toString(), 36)).execute().body() // ~6hrs

                val low = convertToSatsPerByte(slowFeeResponse!!.result.feerate, 0)
                val med = convertToSatsPerByte(medFeeResponse!!.result.feerate, low)
                val high = convertToSatsPerByte(fastFeeResponse!!.result.feerate, med)

                return Result.success(
                    NetworkFeeRate(
                        lowFee = low,
                        medFee = med,
                        highFee = high,
                    )
                )
            } catch (t: Throwable) {
                return Result.failure(t)
            }
        }

        override fun getExtendedNetworkData(): Result<ExtendedNetworkDataModel> {
            try {
                val info = api.getBlockchainInfo(BlockchainInfoRequest.build(UUID.randomUUID().toString())).execute().body()
                val blockStats = api.getBlockStats(BlockStatsRequest.build(UUID.randomUUID().toString(), info!!.result.blocks)).execute().body()


                return Result.success(
                    ExtendedNetworkDataModel(
                        height = info.result.blocks,
                        difficulty = info.result.difficulty.toLong(),
                        blockchainSize = info.result.size_on_disk,
                        avgFeeRate = blockStats!!.result.avgfeerate,
                        avgTxSize = blockStats!!.result.avgtxsize,
                        avgConfTime = 0.0,
                        unconfirmedTxs = 0
                    )
                )
            } catch (t: Throwable) {
                return Result.failure(t)
            }
        }

        private fun convertToSatsPerByte(feeRate: Double, prevFee: Int) : Int {
            val sats = feeRate / 0.00000001
            var satPerByte = sats.toInt() / 1000

            if(satPerByte > 0) {
                if(satPerByte == prevFee) {
                    satPerByte += ((satPerByte / 2) % 10) + 1
                }

                return satPerByte
            } else return 1
        }
    }

    companion object {
        @JvmStatic
        fun create(
            api: NodeApi,
        ): NodeRepository = Impl(api)
    }
}