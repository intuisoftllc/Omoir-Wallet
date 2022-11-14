package com.intuisoft.plaid.common.network.nownodes.request

data class FeeSuggestionRequest(
    val jsonrpc: String = "2.0",
    val method: String = "estimatesmartfee",
    val id: String,
    val params: List<Any>
) {

    companion object {

        fun build(uuid: String, blockTarget: Int): FeeSuggestionRequest =
            FeeSuggestionRequest(
                id = uuid,
                params = listOf(
                    blockTarget,
                    "ECONOMICAL"
                )
            )
    }
}
