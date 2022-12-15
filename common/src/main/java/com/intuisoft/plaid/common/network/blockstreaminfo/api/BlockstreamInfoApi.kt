package com.intuisoft.plaid.common.network.blockstreaminfo.api

import com.intuisoft.plaid.common.network.blockstreaminfo.response.AddressTransactionsResponse
import com.intuisoft.plaid.common.network.blockstreaminfo.response.FeeEstimatesResponse
import retrofit2.Call
import retrofit2.http.*

interface BlockStreamInfoApi {

    @GET("fee-estimates")
    fun getFeeEstimates(): Call<FeeEstimatesResponse>

    @GET("address/{address}/txs")
    fun getAddressTransactions(@Path("address") address: String): Call<List<AddressTransactionsResponse>>

    @GET("address/{address}/txs/chain/{lasttxid}")
    fun getAddressTransactions(@Path("address") address: String, @Path("lasttxid") lastTxId: String): Call<List<AddressTransactionsResponse>>

    @GET("block-height/{height}")
    fun getHashForHeight(@Path("height") height: Int): Call<String>
}