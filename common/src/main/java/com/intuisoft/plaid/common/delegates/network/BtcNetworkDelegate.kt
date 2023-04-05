package com.intuisoft.plaid.common.delegates.network

import android.app.Application
import android.util.Log
import com.intuisoft.plaid.common.R
import com.intuisoft.plaid.common.local.AppPrefs
import com.intuisoft.plaid.common.model.BasicTickerDataModel
import com.intuisoft.plaid.common.model.ChartDataModel
import com.intuisoft.plaid.common.model.ChartIntervalType
import com.intuisoft.plaid.common.model.CongestionRating
import com.intuisoft.plaid.common.repositories.ApiRepository
import com.intuisoft.plaid.common.repositories.LocalStoreRepository
import com.intuisoft.plaid.common.util.Constants
import com.intuisoft.plaid.common.util.SimpleCoinNumberFormat
import com.intuisoft.plaid.common.util.extensions.humanReadableByteCountSI

class BtcNetworkDelegate(
    private val localStoreRepository: LocalStoreRepository,
    private val apiRepository: ApiRepository,
    private val appPrefs: AppPrefs,
    private val application: Application
): NetworkDataDelegate() {

    override val blockchairId: String = "bitcoin"
    override val explorer: String = "https://www.blockchain.com/explorer?view=btc"

    protected override val txUrl: String = "https://www.blockchain.com/btc/tx/"
    protected override val testnetTxUrl: String = "https://live.blockcypher.com/btc-testnet/tx/"

    private var extendedNetworkDataTitles: List<String> = listOf(
        application.getString(R.string.btc_extended_net_data_title_1),
        application.getString(R.string.btc_extended_net_data_title_2),
        application.getString(R.string.btc_extended_net_data_title_3),
        application.getString(R.string.btc_extended_net_data_title_4),
        application.getString(R.string.btc_extended_net_data_title_5),
        application.getString(R.string.btc_extended_net_data_title_6),
        application.getString(R.string.btc_extended_net_data_title_7),
        application.getString(R.string.btc_extended_net_data_title_8),
        application.getString(R.string.btc_extended_net_data_title_9),
        application.getString(R.string.btc_extended_net_data_title_10),
        application.getString(R.string.btc_extended_net_data_title_11),
    )

    companion object {

    }

    override fun buildTxUrl(data: String): String {
        return txUrl + data
    }

    override fun buildTestnetTxUrl(data: String): String {
        return testnetTxUrl + data
    }

    override fun getExtendedNetworkDataTitles(): List<String> {
        return extendedNetworkDataTitles
    }

    override fun getLastBlockStatsUpdateTime(testnet: Boolean): Long {
        return localStoreRepository.getLastBTCBlockStatsUpdateTime(testnet)
    }

    override fun setLastBlockStatsUpdateTime(testnet: Boolean, time: Long) {
        localStoreRepository.setLastBTCBlockStatsUpdate(time, testnet)
    }

    // for testing purposes only
    private fun printCongestionRatingPoints() {
        val indicator1Points = listOf(-1, 0, 1, 2)
        val indicator2Points = listOf(2, 1, -1, -2)
        val indicator3Points = listOf(-2, -1, 2, 3)
        val indicator4Points = listOf(-1, 1, 2, 3)
        var points = mutableListOf<Int>()

        indicator1Points.forEach { it1 ->
            indicator2Points.forEach { it2 ->
                indicator3Points.forEach { it3 ->
                    indicator4Points.forEach { it4 ->
                        points.add(it1 + it2 + it3 + it4)
                    }
                }
            }
        }

        points = points.distinct().sorted().toMutableList()
        println("points range: $points")
    }

    override suspend fun fetchExtendedNetworkData(testnet: Boolean): List<Pair<String, String>> {
        val data = mutableListOf<Pair<String, String>>()
        val extendedData = apiRepository.getBlockStats(testnet, this)
        val stats = apiRepository.getBitcoinStats()

        Log.e("LOOK", "extended data $extendedData - $stats")
        if(extendedData != null && stats != null) {

            if(testnet) {
                data.add(extendedNetworkDataTitles[0] to application.getString(R.string.btc_extended_net_data_value_1_b))
            } else {
                data.add(extendedNetworkDataTitles[0] to application.getString(R.string.btc_extended_net_data_value_1_a))
            }

            // Block Height
            data.add(extendedNetworkDataTitles[1] to (SimpleCoinNumberFormat.format(extendedData.height.toLong()) ?: ""))

            // Network Difficulty
            data.add(extendedNetworkDataTitles[2] to (SimpleCoinNumberFormat.format(extendedData.difficulty) ?: ""))

            // Blockchain Size
            data.add(extendedNetworkDataTitles[3] to (extendedData.blockchainSize.humanReadableByteCountSI() ?: ""))

            // Noded Online
            data.add(extendedNetworkDataTitles[4] to (
                    if(testnet)
                        application.getString(R.string.not_applicable)
                    else
                        SimpleCoinNumberFormat.format(extendedData.nodesOnNetwork.toLong()) + " ${application.getString(R.string.nodes)}"
                    )
            )

            // Mem Pool Size
            data.add(extendedNetworkDataTitles[5] to (extendedData.memPoolSize.humanReadableByteCountSI() ?: ""))

            // Tx/s
            data.add(extendedNetworkDataTitles[6] to (
                    if(testnet)
                        application.getString(R.string.not_applicable)
                    else
                        SimpleCoinNumberFormat.format(extendedData.txPerSecond.toLong()) + " ${application.getString(R.string.tx_per_sec)}"
                    )
            )

            // Addresses W/ Balance
            data.add(extendedNetworkDataTitles[7] to (SimpleCoinNumberFormat.formatSatsShort(extendedData.addressesWithBalance) + " ${application.getString(R.string.addresses)}"))

            // Unconfirmed Txs
            data.add(extendedNetworkDataTitles[8] to (SimpleCoinNumberFormat.format(extendedData.unconfirmedTxs.toLong()) ?: ""))

            // Avg Conf Time
            data.add(extendedNetworkDataTitles[9] to (SimpleCoinNumberFormat.formatCurrency(stats.avgConfTime)?.plus(" " + application.getString(R.string.min)) ?: ""))

            // Network Congestion Rating
            var points: Int
            when {
                Constants.UnconfirmedTxsCongestion.LIGHT.contains(extendedData.unconfirmedTxs) -> {
                    points = -1
                }
                Constants.UnconfirmedTxsCongestion.NORMAL.contains(extendedData.unconfirmedTxs) -> {
                    points = 0
                }
                Constants.UnconfirmedTxsCongestion.MED.contains(extendedData.unconfirmedTxs) -> {
                    points = 1
                }
                Constants.UnconfirmedTxsCongestion.BUSY.contains(extendedData.unconfirmedTxs) -> {
                    points = 2
                }
                else -> {
                    points = 3
                }
            }

            when {
                extendedData.txPerSecond  in 0..1-> {
                    points += 2
                }

                extendedData.txPerSecond == 2 -> {
                    points++
                }

                extendedData.txPerSecond in 3..4 -> {
                    points--
                }

                extendedData.txPerSecond >= 5 -> {
                    points -= 2
                }
            }

            when {
                extendedData.memPoolSize in 0..5_000_000 -> { // < 5mb
                    points -= 2
                }

                extendedData.memPoolSize in 5_000_001..10_000_000 -> { // 5-10mb
                    points--
                }

                extendedData.memPoolSize in 10_000_001 .. 20_000_000 -> { // 10-20mb
                    points++
                }

                extendedData.memPoolSize in 20_000_001 .. 40_000_000 -> { // 20-40mb
                    points+= 2
                }

                extendedData.memPoolSize >= 40_000_001 -> { // > 40mb
                    points+= 3
                }
            }

            when {
                stats.avgConfTime in 0.0..9.9 -> {
                    points--                 }

                stats.avgConfTime in 10.0..100.0 -> {
                    points++
                }

                stats.avgConfTime in 101.0..200.0 -> {
                    points += 2
                }

                stats.avgConfTime > 200 -> {
                    points += 3
                }
            }
            //        [    light   ]   [   normal   ]  [ med ]  [ busy ] [congested]
            when { // range: [-6, -5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8,   9,  10]
                points in -6 .. -3 -> {
                    data.add(extendedNetworkDataTitles[10] to (application.getString(R.string.btc_extended_net_data_value_11_a)))
                }
                points in -2..2 -> {
                    data.add(extendedNetworkDataTitles[10] to (application.getString(R.string.btc_extended_net_data_value_11_b)))
                }
                points in 3..5 -> {
                    data.add(extendedNetworkDataTitles[10] to (application.getString(R.string.btc_extended_net_data_value_11_c)))
                }
                points in 6..8 -> {
                    data.add(extendedNetworkDataTitles[10] to (application.getString(R.string.btc_extended_net_data_value_11_d)))
                }
                else -> {
                    data.add(extendedNetworkDataTitles[10] to (application.getString(R.string.btc_extended_net_data_value_11_e)))
                }
            }
        }

        return data
    }
}