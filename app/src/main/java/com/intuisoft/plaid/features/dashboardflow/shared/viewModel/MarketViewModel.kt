package com.intuisoft.plaid.features.dashboardflow.shared.viewModel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.intuisoft.plaid.PlaidApp
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.SingleLiveData
import com.intuisoft.plaid.androidwrappers.WalletViewModel
import com.intuisoft.plaid.common.coroutines.PlaidScope
import com.intuisoft.plaid.common.model.ChartDataModel
import com.intuisoft.plaid.common.model.ChartIntervalType
import com.intuisoft.plaid.common.model.CongestionRating
import com.intuisoft.plaid.common.repositories.ApiRepository
import com.intuisoft.plaid.common.repositories.LocalStoreRepository
import com.intuisoft.plaid.common.util.Constants
import com.intuisoft.plaid.common.util.SimpleCoinNumberFormat
import com.intuisoft.plaid.common.util.SimpleCurrencyFormat
import com.intuisoft.plaid.common.util.extensions.humanReadableByteCountSI
import com.intuisoft.plaid.common.util.extensions.safeWalletScope
import com.intuisoft.plaid.util.NetworkUtil
import com.intuisoft.plaid.walletmanager.AbstractWalletManager
import io.horizontalsystems.bitcoinkit.BitcoinKit
import kotlinx.coroutines.*


class MarketViewModel(
    application: Application,
    private val apiRepository: ApiRepository,
    private val localStoreRepository: LocalStoreRepository,
    private val walletManager: AbstractWalletManager
): WalletViewModel(application, localStoreRepository, apiRepository, walletManager) {

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

    protected val _chartData = SingleLiveData<List<ChartDataModel>>()
    val chartData: LiveData<List<ChartDataModel>> = _chartData

    protected val _tickerPrice = SingleLiveData<String>()
    val tickerPrice: LiveData<String> = _tickerPrice

    protected val _percentageGain = SingleLiveData<Double>()
    val percentageGain: LiveData<Double> = _percentageGain

    protected val _showContent = SingleLiveData<Boolean>()
    val showContent: LiveData<Boolean> = _showContent

    protected val _extendedNetworkDataLoading = SingleLiveData<Boolean>()
    val extendedNetworkDataLoading: LiveData<Boolean> = _extendedNetworkDataLoading

    protected val _basicNetworkDataLoading = SingleLiveData<Boolean>()
    val basicNetworkDataLoading: LiveData<Boolean> = _basicNetworkDataLoading

    protected val _chartDataLoading = SingleLiveData<Boolean>()
    val chartDataLoading: LiveData<Boolean> = _chartDataLoading

    private var intervalType = ChartIntervalType.INTERVAL_1DAY

    fun updateData() {
        if(NetworkUtil.hasInternet(getApplication<PlaidApp>())) {
            _showContent.postValue(true)
            updateBasicMarketData()
            updateExtendedMarketData()
            updateChartData()
            setTickerPrice()
        } else {
            onNoInternet(false)
        }
    }

    fun onNoInternet(hasInternet: Boolean) {
        PlaidScope.IoScope.launch {

            val basicData = apiRepository.getBasicTickerData()
            val extendedData = apiRepository.getExtendedNetworkData(getWalletNetwork() == BitcoinKit.NetworkType.TestNet)
            val chartData = getChartData()

            if(basicData.marketCap != 0.0 && extendedData != null && chartData != null) {
                _showContent.postValue(true)
                updateBasicMarketData()
                updateExtendedMarketData()
                updateChartData()
                setTickerPrice()
            } else {
                _showContent.postValue(hasInternet)

                if (hasInternet) {
                    updateBasicMarketData()
                    updateExtendedMarketData()
                    updateChartData()
                    setTickerPrice()
                }
            }
        }
    }

    fun updateBasicMarketData() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                safeWalletScope {
                    _basicNetworkDataLoading.postValue(true)
                    val data = apiRepository.getBasicTickerData()

                    var mktCap = SimpleCurrencyFormat.formatValue(
                        localStoreRepository.getLocalCurrency(),
                        data.marketCap
                    )

                    var vol = SimpleCurrencyFormat.formatValue(
                        localStoreRepository.getLocalCurrency(),
                        data.volume24Hr
                    )

                    if (mktCap.contains(".00")) {
                        mktCap = mktCap.replace(".00", "")
                    }

                    if (vol.contains(".00")) {
                        vol = vol.replace(".00", "")
                    }

                    _marketCap.postValue(mktCap)
                    _volume24Hr.postValue(vol)
                    _circulatingSupply.postValue(SimpleCoinNumberFormat.format(data.circulatingSupply) + " BTC")
                    _maxSupply.postValue(SimpleCoinNumberFormat.format(data.maxSupply.toLong()) + " BTC")
                    _basicNetworkDataLoading.postValue(false)
                }
            }
        }
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

    fun updateExtendedMarketData() {
        if(!localStoreRepository.isProEnabled()) return

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                safeWalletScope {
                    _extendedNetworkDataLoading.postValue(true)
                    if(getWalletNetwork() == BitcoinKit.NetworkType.TestNet) {
                        _network.postValue(getApplication<PlaidApp>().getString(R.string.market_data_extended_item_1_description2))
                    } else {
                        _network.postValue(getApplication<PlaidApp>().getString(R.string.market_data_extended_item_1_description))
                    }

                    val extendedData = apiRepository.getExtendedNetworkData(getWalletNetwork() == BitcoinKit.NetworkType.TestNet)

                    if(extendedData != null) {
                        _blockHeight.postValue(SimpleCoinNumberFormat.format(extendedData.height.toLong()) ?: "")
                        _difficulty.postValue(SimpleCoinNumberFormat.format(extendedData.difficulty) ?: "")
                        _blockchainSize.postValue(extendedData.blockchainSize.humanReadableByteCountSI() ?: "")
                        _addressesWithBalance.postValue(SimpleCoinNumberFormat.formatSatsShort(extendedData.addressesWithBalance) + " ${getApplication<PlaidApp>().getString(R.string.addresses)}")
                        _memoryPoolSize.postValue(extendedData.memPoolSize.humanReadableByteCountSI() ?: "")
                        _unconfirmedTxs.postValue(SimpleCoinNumberFormat.format(extendedData.unconfirmedTxs.toLong()) ?: "")

                        if(getWalletNetwork() == BitcoinKit.NetworkType.TestNet || extendedData == null) {
                            _congestionRating.postValue(CongestionRating.NA)
                            _avgConfTime.postValue(getApplication<PlaidApp>().getString(R.string.not_applicable))
                            _nodesOnNetwork.postValue(getApplication<PlaidApp>().getString(R.string.not_applicable))
                            _txPerSecond.postValue(getApplication<PlaidApp>().getString(R.string.not_applicable))
                        } else {
                            _avgConfTime.postValue(SimpleCoinNumberFormat.formatCurrency(extendedData.avgConfTime) ?: "")
                            _nodesOnNetwork.postValue(SimpleCoinNumberFormat.format(extendedData.nodesOnNetwork.toLong()) + " ${getApplication<PlaidApp>().getString(R.string.nodes)}")
                            _txPerSecond.postValue(SimpleCoinNumberFormat.format(extendedData.txPerSecond.toLong()) + " ${getApplication<PlaidApp>().getString(R.string.tx_per_sec)}")

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
                            when { // range: [-6, -5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8,   9,  10]
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
                    _extendedNetworkDataLoading.postValue(false)
                }
            }
        }
    }

    fun setTickerPrice() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                safeWalletScope {
                    _tickerPrice.postValue(
                        SimpleCurrencyFormat.formatValue(
                            localStoreRepository.getLocalCurrency(),
                            apiRepository.getBasicTickerData().price
                        )
                    )

                    getChartData()?.let {
                        val gain = 100 * ((it.last().value - it.first().value) / it.first().value)
                        _percentageGain.postValue(gain.toDouble())
                    }
                }
            }
        }
    }

    fun changeChartInterval(intervalType: ChartIntervalType) {
        if(intervalType != this.intervalType) {
            this.intervalType = intervalType
            updateChartData()
            setTickerPrice()
        }
    }



    fun updateChartData() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                safeWalletScope {
                    _chartDataLoading.postValue(true)
                    _chartData.postValue(listOf())

                    val data = getChartData()

                    if (data != null) {
                        _chartDataLoading.postValue(false)
                        _chartData.postValue(data!!)
                    } else {
                        _couldNotLoadData.postValue(Unit)
                    }
                }
            }
        }
    }

    private suspend fun getChartData() = apiRepository.getTickerPriceChartData(intervalType)
}