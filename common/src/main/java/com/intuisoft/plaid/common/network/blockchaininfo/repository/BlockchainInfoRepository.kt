package com.intuisoft.plaid.common.network.blockchair.repository

import com.intuisoft.plaid.common.network.blockchair.api.BlockchainInfoApi
import com.intuisoft.plaid.common.network.blockchaininfo.response.BasicNetworkDataResponse
import com.intuisoft.plaid.common.util.Constants

interface BlockchainInfoRepository {
    val TAG: String
        get() = this.javaClass.simpleName

    fun getAverageBTCConfTime(): Result<Double>

    private class Impl(
        private val api: BlockchainInfoApi,
    ) : BlockchainInfoRepository {

        override fun getAverageBTCConfTime(): Result<Double> {
            try {
                val stats = api.getBlockchainStats().execute().body()

                return Result.success(
                    stats!!.minutes_between_blocks
                )
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