package com.intuisoft.plaid.common.network.blockstreaminfo.repository

import com.intuisoft.plaid.common.model.NetworkFeeRate
import com.intuisoft.plaid.common.network.blockstreaminfo.api.BlockStreamInfoApi
import kotlin.math.roundToInt

interface BlockstreamInfoRepository {
    val TAG: String
        get() = this.javaClass.simpleName

    fun getFeeEstimates(): Result<NetworkFeeRate>

    private class Impl(
        private val api: BlockStreamInfoApi,
    ) : BlockstreamInfoRepository {

        override fun getFeeEstimates(): Result<NetworkFeeRate> {
            try {
                val estimates = api.getFeeEstimates().execute().body()

                val low = adjustFeeIfNeeded(estimates!!.low, 0)
                val med = adjustFeeIfNeeded(estimates!!.med, low)
                val high = adjustFeeIfNeeded(estimates!!.high, med)

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

        private fun adjustFeeIfNeeded(feeRate: Double, prevFee: Int) : Int {
            var satPerByte = feeRate.roundToInt()

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
            api: BlockStreamInfoApi,
        ): BlockstreamInfoRepository = Impl(api)
    }
}