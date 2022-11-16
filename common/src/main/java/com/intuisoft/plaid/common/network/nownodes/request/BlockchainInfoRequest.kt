package com.intuisoft.plaid.common.network.nownodes.request

data class BlockchainInfoRequest(
    val jsonrpc: String = "2.0",
    val method: String = "getblockchaininfo",
    val id: String,
    val params: List<Any>
) {

    companion object {

        fun build(uuid: String): BlockchainInfoRequest =
            BlockchainInfoRequest(
                id = uuid,
                params = listOf()
            )
    }
}
