package com.intuisoft.plaid.features.dashboardflow.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.intuisoft.plaid.PlaidApp
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.SingleLiveData
import com.intuisoft.plaid.androidwrappers.WalletViewModel
import com.intuisoft.plaid.common.model.*
import com.intuisoft.plaid.common.repositories.ApiRepository
import com.intuisoft.plaid.common.repositories.LocalStoreRepository
import com.intuisoft.plaid.common.util.Constants
import com.intuisoft.plaid.common.util.RateConverter
import com.intuisoft.plaid.common.util.SimpleCoinNumberFormat
import com.intuisoft.plaid.common.util.SimpleCurrencyFormat
import com.intuisoft.plaid.common.util.extensions.humanReadableByteCountSI
import com.intuisoft.plaid.util.NetworkUtil
import com.intuisoft.plaid.walletmanager.AbstractWalletManager
import io.horizontalsystems.bitcoincore.models.TransactionInfo
import io.horizontalsystems.bitcoincore.models.TransactionStatus
import io.horizontalsystems.bitcoincore.models.TransactionType
import io.horizontalsystems.bitcoinkit.BitcoinKit
import kotlinx.coroutines.*
import java.time.Instant
import java.time.ZonedDateTime


class DashboardViewModel(
    application: Application,
    private val apiRepository: ApiRepository,
    private val localStoreRepository: LocalStoreRepository,
    private val walletManager: AbstractWalletManager
): WalletViewModel(application, localStoreRepository, apiRepository, walletManager) {



    // keep
    protected val _showChartContent = SingleLiveData<Boolean>()
    val showChartContent: LiveData<Boolean> = _showChartContent

    protected val _noChartData = SingleLiveData<Unit>()
    val noChartData: LiveData<Unit> = _noChartData

    protected val _chartData = SingleLiveData<List<ChartDataModel>>()
    val chartData: LiveData<List<ChartDataModel>> = _chartData

    private var intervalType = ChartIntervalType.INTERVAL_1DAY
    private var walletTransactions: List<TransactionInfo> = listOf()
        set(value) {
            field = value
            balanceHistory = generateBalanceHistory()
        }

    private var balanceHistory: List<Pair<Long, Long>> = listOf()

    fun changeChartInterval(intervalType: ChartIntervalType) {
        if(intervalType != this.intervalType) {
            this.intervalType = intervalType
            updateBalanceHistory()
        }
    }

    fun onDisplayUnitChanged() {
        updateBalanceHistory()
    }

    private fun updateBalanceHistory() {
        if(localStoreRepository.getBitcoinDisplayUnit() != BitcoinDisplayUnit.FIAT || NetworkUtil.hasInternet(getApplication<PlaidApp>())) {
            _showChartContent.postValue(true)
            updateWalletBalanceHistory()
        } else {
            onNoInternet(false)
        }
    }

    fun onTransactionsUpdated(txList: List<TransactionInfo>) {
        walletTransactions = txList.filter { it.status != TransactionStatus.INVALID }
        updateBalanceHistory()
    }

    fun transformScrubValue(data: ChartDataModel): String {
        val rateConverter = RateConverter(
            0.0
        )

        return when(localStoreRepository.getBitcoinDisplayUnit()) {
            BitcoinDisplayUnit.SATS -> {
                rateConverter.setLocalRate(RateConverter.RateType.SATOSHI_RATE, data.value.toDouble())
                rateConverter.from(RateConverter.RateType.SATOSHI_RATE, localStoreRepository.getLocalCurrency()).second
            }

            BitcoinDisplayUnit.BTC -> {
                rateConverter.setLocalRate(RateConverter.RateType.BTC_RATE, data.value.toDouble())
                rateConverter.from(RateConverter.RateType.BTC_RATE, localStoreRepository.getLocalCurrency()).second
            }

            BitcoinDisplayUnit.FIAT -> {
                SimpleCurrencyFormat.formatValue(
                    localStoreRepository.getLocalCurrency(),
                    data.value.toDouble()
                )
            }
        }
    }

    private fun updateWalletBalanceHistory() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                if(localStoreRepository.isProEnabled()) {
                    if (balanceHistory.isEmpty()) {
                        _noChartData.postValue(Unit)
                    } else {
                        val history = getBalanceHistoryForInterval()
                        val rateConverter = RateConverter(
                            localStoreRepository.getRateFor(localStoreRepository.getLocalCurrency())?.currentPrice
                                ?: 0.0
                        )

                        val gain: Double

                        if(localStoreRepository.getBitcoinDisplayUnit() != BitcoinDisplayUnit.FIAT) {
                            val start = history.first().first
                            val end = history.last().first
                            gain = 100 * ((end.toDouble() - start) / start.toDouble())
                        } else {
                            val data = apiRepository.getTickerPriceChartData(intervalType)
                                ?.filter {
                                    (it.time / Constants.Time.MILLS_PER_SEC) >= history.first().second
                                }

                            if(data != null) {
                                val start = getBalanceAtTime(data.first().time / Constants.Time.MILLS_PER_SEC) * data.first().value
                                val end = getBalanceAtTime(data.last().time / Constants.Time.MILLS_PER_SEC) * data.last().value
                                gain = 100 * ((end.toDouble() - start) / start.toDouble())
                            } else {
                                gain = 0.0
                            }
                        }
                        _percentageGain.postValue(gain)

                        when (localStoreRepository.getBitcoinDisplayUnit()) {
                            BitcoinDisplayUnit.SATS -> {
                                _chartData.postValue(
                                    history.map {
                                        ChartDataModel(
                                            time = it.second,
                                            value = it.first.toFloat()
                                        )
                                    }
                                )
                                
                                _showChartContent.postValue(true)
                            }

                            BitcoinDisplayUnit.BTC -> {
                                _chartData.postValue(
                                    history.map {
                                        rateConverter.setLocalRate(
                                            RateConverter.RateType.SATOSHI_RATE,
                                            it.first.toDouble()
                                        )

                                        ChartDataModel(
                                            time = it.second,
                                            value = rateConverter.getRawBtcRate().toFloat()
                                        )
                                    }
                                )
                                _showChartContent.postValue(true)
                            }

                            BitcoinDisplayUnit.FIAT -> {
                                val data = apiRepository.getTickerPriceChartData(intervalType)
                                    ?.filter {
                                        (it.time / Constants.Time.MILLS_PER_SEC) >= history.first().second
                                    }

                                if(data != null) {
                                    _chartData.postValue(
                                        data.map {
                                            ChartDataModel(
                                                time = it.time / Constants.Time.MILLS_PER_SEC,
                                                value = getBalanceAtTime(it.time / Constants.Time.MILLS_PER_SEC) * it.value
                                            )
                                        }
                                    )

                                    _showChartContent.postValue(true)
                                } else {
                                    _showChartContent.postValue(false)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun getBalanceAtTime(time: Long): Float {
        val closestBalance = balanceHistory.reversed().first { it.second <= time }
        return closestBalance.first.toFloat() / Constants.Limit.SATS_PER_BTC
    }

    private fun getBalanceHistoryForInterval(): List<Pair<Long, Long>> {
        val txStartTime: Instant

        when(intervalType) {
            ChartIntervalType.INTERVAL_1DAY -> {
                txStartTime = ZonedDateTime.now().minusDays(1).toInstant()
            }
            ChartIntervalType.INTERVAL_1WEEK -> {
                txStartTime = ZonedDateTime.now().minusWeeks(1).toInstant()
            }
            ChartIntervalType.INTERVAL_1MONTH -> {
                txStartTime = ZonedDateTime.now().minusMonths(1).toInstant()
            }
            ChartIntervalType.INTERVAL_3MONTHS -> {
                txStartTime = ZonedDateTime.now().minusMonths(3).toInstant()
            }
            ChartIntervalType.INTERVAL_6MONTHS -> {
                txStartTime = ZonedDateTime.now().minusMonths(6).toInstant()
            }
            ChartIntervalType.INTERVAL_1YEAR -> {
                txStartTime = ZonedDateTime.now().minusYears(1).toInstant()
            }
            ChartIntervalType.INTERVAL_ALL_TIME -> {
                txStartTime = Instant.ofEpochSecond(balanceHistory.firstOrNull()?.second ?: 0)
            }
        }

        var filtered = balanceHistory.filter {
            it.second >= txStartTime.epochSecond
        }

        if(filtered.size == 1) {
            filtered = filtered + filtered
        } else if(filtered.isEmpty() && balanceHistory.isNotEmpty()) {
            filtered = listOf(balanceHistory.last(), balanceHistory.last())
        }

        return filtered
    }

    private fun generateBalanceHistory(): List<Pair<Long, Long>> {
        val balanceHistory = mutableListOf<Pair<Long, Long>>()
        var balance = 0L

        walletTransactions.reversed().forEach {
            when(it.type) {
                TransactionType.Incoming -> {
                    balance += it.amount
                }

                TransactionType.Outgoing -> {
                    balance -= it.amount + (it.fee?: 0)
                }

                TransactionType.SentToSelf -> {
                    balance -= it.fee ?: 0
                }
            }

            balanceHistory.add(balance to it.timestamp)
        }

        return balanceHistory
    }


    fun onNoInternet(hasInternet: Boolean) {
        CoroutineScope(Dispatchers.IO).launch {

            val data = apiRepository.getTickerPriceChartData(intervalType)

            if(data != null) {
                _showChartContent.postValue(true)
                updateWalletBalanceHistory()
            } else {
                _showChartContent.postValue(hasInternet)

                if (hasInternet) {
                    updateWalletBalanceHistory()
                }
            }
        }
    }

    // keep end

    protected val _marketCap = SingleLiveData<String>()
    val marketCap: LiveData<String> = _marketCap

    protected val _volume24Hr = SingleLiveData<String>()
    val volume24Hr: LiveData<String> = _volume24Hr

    protected val _circulatingSupply = SingleLiveData<String>()
    val circulatingSupply: LiveData<String> = _circulatingSupply

    protected val _maxSupply = SingleLiveData<String>()
    val maxSupply: LiveData<String> = _maxSupply

    protected val _network = SingleLiveData<String>()
    val network: LiveData<String> = _network

    protected val _blockHeight = SingleLiveData<String>()
    val blockHeight: LiveData<String> = _blockHeight

    protected val _difficulty = SingleLiveData<String>()
    val difficulty: LiveData<String> = _difficulty

    protected val _blockchainSize = SingleLiveData<String>()
    val blockchainSize: LiveData<String> = _blockchainSize

    protected val _addressesWithBalance = SingleLiveData<String>()
    val addressesWithBalance: LiveData<String> = _addressesWithBalance

    protected val _txPerSecond = SingleLiveData<String>()
    val txPerSecond: LiveData<String> = _txPerSecond

    protected val _memoryPoolSize = SingleLiveData<String>()
    val memoryPoolSize: LiveData<String> = _memoryPoolSize

    protected val _nodesOnNetwork = SingleLiveData<String>()
    val nodesOnNetwork: LiveData<String> = _nodesOnNetwork

    protected val _avgFeeRate = SingleLiveData<String>()
    val avgFeeRate: LiveData<String> = _avgFeeRate

    protected val _unconfirmedTxs = SingleLiveData<String>()
    val unconfirmedTxs: LiveData<String> = _unconfirmedTxs

    protected val _avgConfTime = SingleLiveData<String>()
    val avgConfTime: LiveData<String> = _avgConfTime

    protected val _couldNotLoadData = SingleLiveData<Unit>()
    val couldNotLoadData: LiveData<Unit> = _couldNotLoadData

    protected val _congestionRating = SingleLiveData<CongestionRating>()
    val congestionRating: LiveData<CongestionRating> = _congestionRating

    protected val _tickerPrice = SingleLiveData<String>()
    val tickerPrice: LiveData<String> = _tickerPrice

    protected val _percentageGain = SingleLiveData<Double>()
    val percentageGain: LiveData<Double> = _percentageGain

    protected val _upgradeToPro = SingleLiveData<Boolean>()
    val upgradeToPro: LiveData<Boolean> = _upgradeToPro

    fun checkProStatus() {
        _upgradeToPro.postValue(!localStoreRepository.isProEnabled())
    }

    // for testing purposes only
    fun printCongestionRatingPoints() {
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

    fun updateExtendedMarketData() {
        if(!localStoreRepository.isProEnabled()) return

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                if(getWalletNetwork() == BitcoinKit.NetworkType.TestNet) {
                    _network.postValue(getApplication<PlaidApp>().getString(R.string.market_data_extended_item_1_description2))
                } else {
                    _network.postValue(getApplication<PlaidApp>().getString(R.string.market_data_extended_item_1_description))
                }

                val extendedData = apiRepository.getExtendedNetworkData(getWalletNetwork() == BitcoinKit.NetworkType.TestNet)

                if(extendedData != null) {
                    _blockHeight.postValue(SimpleCoinNumberFormat.format(extendedData.height.toLong()))
                    _difficulty.postValue(SimpleCoinNumberFormat.format(extendedData.difficulty))
                    _blockchainSize.postValue(extendedData.blockchainSize.humanReadableByteCountSI())
                    _addressesWithBalance.postValue(SimpleCoinNumberFormat.formatSatsShort(extendedData.addressesWithBalance) + " ${getApplication<PlaidApp>().getString(R.string.addresses)}")
                    _memoryPoolSize.postValue(extendedData.memPoolSize.humanReadableByteCountSI())
                    _unconfirmedTxs.postValue(SimpleCoinNumberFormat.format(extendedData.unconfirmedTxs.toLong()))

                    if(getWalletNetwork() == BitcoinKit.NetworkType.TestNet || extendedData == null) {
                        _congestionRating.postValue(CongestionRating.NA)
                        _avgConfTime.postValue(getApplication<PlaidApp>().getString(R.string.not_applicable))
                        _nodesOnNetwork.postValue(getApplication<PlaidApp>().getString(R.string.not_applicable))
                        _txPerSecond.postValue(getApplication<PlaidApp>().getString(R.string.not_applicable))
                    } else {
                        _avgConfTime.postValue(SimpleCoinNumberFormat.formatCurrency(extendedData.avgConfTime))
                        _nodesOnNetwork.postValue(SimpleCoinNumberFormat.format(extendedData.nodesOnNetwork.toLong()) + " ${getApplication<PlaidApp>().getString(R.string.nodes)}")
                        _txPerSecond.postValue(SimpleCoinNumberFormat.format(extendedData.txPerSecond.toLong()) + " ${getApplication<PlaidApp>().getString(R.string.tx_per_sec)}")

                        var points: Int

                        when {
                            Constants.CongestionRating.LIGHT.contains(extendedData.unconfirmedTxs) -> {
                                points = -1
                            }
                            Constants.CongestionRating.NORMAL.contains(extendedData.unconfirmedTxs) -> {
                                points = 0
                            }
                            Constants.CongestionRating.MED.contains(extendedData.unconfirmedTxs) -> {
                                points = 1
                            }
                            Constants.CongestionRating.BUSY.contains(extendedData.unconfirmedTxs) -> {
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
                            extendedData.avgConfTime in 0.0..9.9 -> {
                                points--
                            }

                            extendedData.avgConfTime in 10.0..100.0 -> {
                                points++
                            }

                            extendedData.avgConfTime in 101.0..200.0 -> {
                                points += 2
                            }

                            extendedData.avgConfTime > 200 -> {
                                points += 3
                            }
                        }
                                //        [    light   ]   [   normal   ]  [ med ]  [ busy ] [congested]
                        when { // range: [-6, -5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10]
                            points in -6 .. -3 -> {
                                _congestionRating.postValue(CongestionRating.LIGHT)
                            }
                            points in -2..2 -> {
                                _congestionRating.postValue(CongestionRating.NORMAL)
                            }
                            points in 3..5 -> {
                                _congestionRating.postValue(CongestionRating.MED)
                            }
                            points in 6..8 -> {
                                _congestionRating.postValue(CongestionRating.BUSY)
                            }
                            else -> {
                                _congestionRating.postValue(CongestionRating.CONGESTED)
                            }
                        }
                    }
                } else {
                    _couldNotLoadData.postValue(Unit)
                }
            }
        }
    }


    fun updateChartData() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val data = getChartData()

                if(data != null)
                    _chartData.postValue(data!!)
                else
                    _couldNotLoadData.postValue(Unit)
            }
        }
    }

    private suspend fun getChartData() = apiRepository.getTickerPriceChartData(intervalType)
}