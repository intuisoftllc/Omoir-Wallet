package com.intuisoft.plaid.common.network.blockchair.repository

import com.intuisoft.plaid.common.model.ExtendedNetworkDataModel
import com.intuisoft.plaid.common.network.blockchair.api.BlockchainInfoApi
import com.intuisoft.plaid.common.network.blockchair.response.BasicNetworkDataResponse
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

                return Result.success(BasicNetworkDataResponse(supply!! / Constants.Limit.SATS_PER_BTC))
            } catch (t: Throwable) {
                return Result.failure(t)
            }
        }

        override fun getExtendedNetworkData(): Result<ExtendedNetworkDataModel> {
            try {
                val stats = api.getBlockchainStats().execute().body()

                return Result.success(
                    ExtendedNetworkDataModel(
                        height = 0,
                        difficulty = 0,
                        blockchainSize = 0,
                        nodesOnNetwork = 0,
                        memPoolSize = 0,
                        txPerSecond = 0,
                        addressesWithBalance = 0,
                        unconfirmedTxs = 0,
                        avgConfTime = stats!!.minutes_between_blocks
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