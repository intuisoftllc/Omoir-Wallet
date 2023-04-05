package com.intuisoft.plaid.common.delegates.network

import com.intuisoft.plaid.common.model.BasicTickerDataModel
import com.intuisoft.plaid.common.model.ChartDataModel
import com.intuisoft.plaid.common.model.ChartIntervalType

abstract class NetworkDataDelegate {
    abstract val blockchairId: String
    abstract val explorer: String

    protected abstract val txUrl: String
    protected abstract val testnetTxUrl: String

    abstract suspend fun fetchExtendedNetworkData(testnet: Boolean): List<Pair<String, String>>

    abstract fun buildTxUrl(data: String): String
    abstract fun buildTestnetTxUrl(data: String): String
    abstract fun getExtendedNetworkDataTitles(): List<String>
    abstract fun getLastBlockStatsUpdateTime(testnet: Boolean): Long
    abstract fun setLastBlockStatsUpdateTime(testnet: Boolean, time: Long)
}