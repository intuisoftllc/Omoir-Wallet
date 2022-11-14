package com.intuisoft.plaid.common.network.nownodes.api

import com.intuisoft.plaid.common.network.nownodes.response.BlockHeightResponse
import retrofit2.Call
import retrofit2.http.*

interface BlockBookApi {

    @GET("block-index/{blockHeight}")
    fun getBlockHash(
        @Path("blockHeight") height: Int
    ): Call<BlockHeightResponse>

}