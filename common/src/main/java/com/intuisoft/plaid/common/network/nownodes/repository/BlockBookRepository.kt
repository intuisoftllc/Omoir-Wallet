package com.intuisoft.plaid.common.network.nownodes.repository

import com.intuisoft.plaid.common.network.nownodes.api.BlockBookApi
import com.intuisoft.plaid.common.network.nownodes.response.BlockHeightResponse

interface BlockBookRepository {
    val TAG: String
        get() = this.javaClass.simpleName

    fun getBlockHash(height: Int): Result<BlockHeightResponse>

    private class Impl(
        private val api: BlockBookApi
    ) : BlockBookRepository {

        override fun getBlockHash(height: Int): Result<BlockHeightResponse> {
            try {
                val response = api.getBlockHash(height).execute().body()
                return Result.success(response!!)
            } catch (t: Throwable) {
                return Result.failure(t)
            }
        }
    }

    companion object {
        @JvmStatic
        fun create(
            api: BlockBookApi
        ): BlockBookRepository = Impl(api)
    }
}