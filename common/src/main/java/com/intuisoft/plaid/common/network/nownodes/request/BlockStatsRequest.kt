package com.intuisoft.plaid.common.network.nownodes.request

data class BlockStatsRequest(
    val jsonrpc: String = "2.0",
    val method: String = "getblockstats",
    val id: String,
    val params: List<Any>
) {

    companion object {

        fun build(uuid: String, blockTarget: Int): BlockStatsRequest =
            BlockStatsRequest(
                id = uuid,
                params = listOf(
                    blockTarget
                )
            )
    }
}
