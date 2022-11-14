package com.intuisoft.plaid.common.network.nownodes.repository

import com.intuisoft.plaid.common.model.NetworkFeeRate
import com.intuisoft.plaid.common.network.nownodes.api.NodeApi
import com.intuisoft.plaid.common.network.nownodes.request.FeeSuggestionRequest
import com.intuisoft.plaid.common.repositories.LocalStoreRepository
import java.util.*

interface NodeRepository {
    val TAG: String
        get() = this.javaClass.simpleName

    fun getFeeSuggestions(): Result<NetworkFeeRate>

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