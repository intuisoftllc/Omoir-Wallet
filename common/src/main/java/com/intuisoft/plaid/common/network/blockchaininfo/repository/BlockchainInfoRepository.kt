package com.intuisoft.plaid.common.network.nownodes.repository

import com.intuisoft.plaid.common.model.NetworkFeeRate
import com.intuisoft.plaid.common.network.nownodes.api.BlockchainInfoApi
import com.intuisoft.plaid.common.network.nownodes.request.FeeSuggestionRequest
import com.intuisoft.plaid.common.network.nownodes.response.PriceConversionRatesResponse
import java.util.*

interface BlockchainInfoRepository {
    val TAG: String
        get() = this.javaClass.simpleName

    fun getPriceConversions(): Result<PriceConversionRatesResponse>

    private class Impl(
        private val api: BlockchainInfoApi,
    ) : BlockchainInfoRepository {

        override fun getPriceConversions(): Result<PriceConversionRatesResponse> {
            try {
                val rates = api.getPriceConversionRates().execute().body()

                return Result.success(rates!!)
            } catch (t: Throwable) {
                return Result.failure(t)
            }
        }
    }

    companion object {
        @JvmStatic
        fun create(
            api: BlockchainInfoApi,
        ): BlockchainInfoRepository = Impl(api)
    }
}