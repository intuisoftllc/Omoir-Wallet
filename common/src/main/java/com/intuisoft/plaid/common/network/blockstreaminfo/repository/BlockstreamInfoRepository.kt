package com.intuisoft.plaid.common.network.blockstreaminfo.repository

import com.intuisoft.plaid.common.model.*
import com.intuisoft.plaid.common.network.blockstreaminfo.api.BlockStreamInfoApi
import com.intuisoft.plaid.common.network.blockstreaminfo.response.FeeEstimatesResponse
import retrofit2.Call
import retrofit2.http.Path
import retrofit2.http.Query
import kotlin.math.roundToInt

interface BlockstreamInfoRepository {
    val TAG: String
        get() = this.javaClass.simpleName

    fun getFeeEstimates(): Result<NetworkFeeRate>
    fun getAddressTransactions(address: String): Result<List<AddressTransactionData>>
    fun getHashForHeight(height: Int): Result<String>

    private class Impl(
        private val api: BlockStreamInfoApi,
    ) : BlockstreamInfoRepository {

        override fun getHashForHeight(height: Int): Result<String> {
            try {
                val result = api.getHashForHeight(height).execute().body()

                return Result.success(
                    result!!
                )
            } catch (t: Throwable) {
                return Result.failure(t)
            }
        }

        override fun getAddressTransactions(address: String): Result<List<AddressTransactionData>> {
            val data = mutableListOf<AddressTransactionData>()
            var lastTxId = ""

            try {
                while(true) {
                    val txs =
                        if(lastTxId.isEmpty())
                            api.getAddressTransactions(address).execute().body()
                        else api.getAddressTransactions(address, lastTxId).execute().body()

                    if(txs!!.isEmpty()) break
                    lastTxId = txs.last().txid

                    txs.forEach {
                        data.add(
                            AddressTransactionData(
                                status = TxStatus(
                                    height = it.status.block_height,
                                    blockHash = it.status.block_hash
                                ),
                                outputs = it.vout.map {
                                    TxOutput(
                                        script = it.scriptpubkey,
                                        address = it.scriptpubkey_address
                                    )
                                }
                            )
                        )
                    }

                    val confirmed = txs.count { it.status.block_height != null }
                    if(confirmed < 25) break
                }
            } catch (t: Throwable) {
                t.printStackTrace()
            }

            return Result.success(data)
        }

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