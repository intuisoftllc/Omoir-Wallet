package com.intuisoft.plaid.common.network.blockstreaminfo.api

import com.intuisoft.plaid.common.network.blockstreaminfo.response.FeeEstimatesResponse
import retrofit2.Call
import retrofit2.http.*

interface BlockStreamInfoApi {

    @GET("fee-estimates")
    fun getFeeEstimates(): Call<FeeEstimatesResponse>
}