package com.intuisoft.plaid.common.network.nownodes.repository

import com.intuisoft.plaid.common.model.ExtendedNetworkDataModel
import com.intuisoft.plaid.common.network.nownodes.api.BlockchainInfoApi
import com.intuisoft.plaid.common.network.nownodes.response.BasicNetworkDataResponse
import com.intuisoft.plaid.common.util.Constants

interface BlockchainInfoRepository {
    val TAG: String
        get() = this.javaClass.simpleName

    fun getBasicNetworkData(): Result<BasicNetworkDataResponse>

    fun getExtendedNetworkData(): Result<ExtendedNetworkDataModel>

    private class Impl(
        private val api: BlockchainInfoApi,
    ) : BlockchainInfoRepository {
        override fun getBasicNetworkData(): Result<BasicNetworkDataResponse> {
            try {
                val supply = api.getCirculatingSupply().execute().body()
                val txCount = api.getUnconfirmedTxSize().execute().body()

                return Result.success(BasicNetworkDataResponse(supply!! / Constants.Limit.SATS_PER_BTC, txCount!!))
            } catch (t: Throwable) {
                return Result.failure(t)
            }
        }

        override fun getExtendedNetworkData(): Result<ExtendedNetworkDataModel> {
            try {
                val stats = api.getBlockchainStats().execute().body()
                val txCount = api.getUnconfirmedTxSize().execute().body()

                return Result.success(
                    ExtendedNetworkDataModel(
                        height = 0,
                        difficulty = 0,
                        blockchainSize = 0,
                        avgFeeRate = 0,
                        avgTxSize = 0,
                        avgConfTime = stats!!.minutes_between_blocks,
                        unconfirmedTxs = txCount!!
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
            api: BlockchainInfoApi,
        ): BlockchainInfoRepository = Impl(api)
    }
}