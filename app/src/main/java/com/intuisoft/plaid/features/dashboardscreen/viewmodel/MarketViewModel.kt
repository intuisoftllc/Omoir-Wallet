package com.intuisoft.plaid.features.dashboardscreen.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.intuisoft.plaid.PlaidApp
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.SingleLiveData
import com.intuisoft.plaid.androidwrappers.WalletViewModel
import com.intuisoft.plaid.common.model.ChartDataModel
import com.intuisoft.plaid.common.model.ChartIntervalType
import com.intuisoft.plaid.common.model.CongestionRating
import com.intuisoft.plaid.common.repositories.ApiRepository
import com.intuisoft.plaid.common.repositories.LocalStoreRepository
import com.intuisoft.plaid.common.util.Constants
import com.intuisoft.plaid.common.util.SimpleCoinNumberFormat
import com.intuisoft.plaid.common.util.SimpleCurrencyFormat
import com.intuisoft.plaid.common.util.extensions.humanReadableByteCountSI
import com.intuisoft.plaid.walletmanager.AbstractWalletManager
import io.horizontalsystems.bitcoinkit.BitcoinKit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


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

    protected val _avgTxSize = SingleLiveData<String>()
    val avgTxSize: LiveData<String> = _avgTxSize

    protected val _avgFeeRate = SingleLiveData<String>()
    val avgFeeRate: LiveData<String> = _avgFeeRate

    protected val _unconfirmedTxs = SingleLiveData<String>()
    val unconfirmedTxs: LiveData<String> = _unconfirmedTxs

    protected val _avgConfTime = SingleLiveData<String>()
    val avgConfTime: LiveData<String> = _avgConfTime

    protected val _couldNotLoadData = SingleLiveData<Unit>()
    val couldNotLoadData: LiveData<Unit> = _couldNotLoadData

    protected val _hideMainChainDataContainer = SingleLiveData<Unit>()
    val hideMainChainDataContainer: LiveData<Unit> = _hideMainChainDataContainer

    protected val _congestionRating = SingleLiveData<CongestionRating>()
    val congestionRating: LiveData<CongestionRating> = _congestionRating

    protected val _chartData = SingleLiveData<List<ChartDataModel>>()
    val chartData: LiveData<List<ChartDataModel>> = _chartData

    protected val _tickerPrice = SingleLiveData<String>()
    val tickerPrice: LiveData<String> = _tickerPrice

    protected val _percentageGain = SingleLiveData<Double>()
    val percentageGain: LiveData<Double> = _percentageGain

    protected val _upgradeToPro = SingleLiveData<Boolean>()
    val upgradeToPro: LiveData<Boolean> = _upgradeToPro

    private var intervalType = ChartIntervalType.INTERVAL_1DAY

    fun updateBasicMarketData() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val data = apiRepository.getBasicTickerData()

                var mktCap = SimpleCurrencyFormat.formatValue(
                    localStoreRepository.getLocalCurrency(),
                    data.marketCap
                )

                var vol = SimpleCurrencyFormat.formatValue(
                    localStoreRepository.getLocalCurrency(),
                    data.volume24Hr
                )

                if(mktCap.contains(".00")) {
                    mktCap = mktCap.replace(".00", "")
                }

                if(vol.contains(".00")) {
                    vol = vol.replace(".00", "")
                }

                _marketCap.postValue(mktCap)
                _volume24Hr.postValue(vol)
                _circulatingSupply.postValue(SimpleCoinNumberFormat.format(data.circulatingSupply) + " BTC")
                _maxSupply.postValue(SimpleCoinNumberFormat.format(data.maxSupply.toLong()) + " BTC")
            }
        }
    }

    fun checkProStatus() {
        _upgradeToPro.postValue(!localStoreRepository.isProEnabled())
    }

    fun updateExtendedMarketData() {
        if(!localStoreRepository.isProEnabled()) return

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                if(getWalletNetwork() == BitcoinKit.NetworkType.TestNet) {
                    _hideMainChainDataContainer.postValue(Unit)
                    _network.postValue(getApplication<PlaidApp>().getString(R.string.market_data_extended_item_1_description2))
                } else {
                    _network.postValue(getApplication<PlaidApp>().getString(R.string.market_data_extended_item_1_description))
                }

                val basicData = apiRepository.getBasicTickerData()
                val extendedData = apiRepository.getExtendedNetworkData(getWalletNetwork() == BitcoinKit.NetworkType.TestNet)

                if(extendedData != null) {
                    _blockHeight.postValue(SimpleCoinNumberFormat.format(extendedData.height.toLong()))
                    _difficulty.postValue(SimpleCoinNumberFormat.format(extendedData.difficulty))
                    _blockchainSize.postValue(extendedData.blockchainSize.humanReadableByteCountSI())
                    _avgTxSize.postValue(SimpleCoinNumberFormat.format(extendedData.avgTxSize.toLong()) + " ${getApplication<PlaidApp>().getString(R.string.bytes)}")
                    _avgFeeRate.postValue(SimpleCoinNumberFormat.format(extendedData.avgFeeRate.toLong()) + " ${getApplication<PlaidApp>().getString(R.string.sat_per_byte)}")
                    _unconfirmedTxs.postValue(SimpleCoinNumberFormat.format(extendedData.unconfirmedTxs.toLong()))
                    _avgConfTime.postValue(SimpleCoinNumberFormat.formatCurrency(extendedData.avgConfTime))

                    if(getWalletNetwork() == BitcoinKit.NetworkType.TestNet || extendedData == null) {
                        _congestionRating.postValue(CongestionRating.NA)
                    } else {
                        var points: Int

                        when {
                            Constants.CongestionRating.LIGHT.contains(basicData.memPoolTxCount) -> {
                                points = -1
                            }
                            Constants.CongestionRating.NORMAL.contains(basicData.memPoolTxCount) -> {
                                points = 0
                            }
                            Constants.CongestionRating.MED.contains(basicData.memPoolTxCount) -> {
                                points = 1
                            }
                            Constants.CongestionRating.BUSY.contains(basicData.memPoolTxCount) -> {
                                points = 2
                            }
                            else -> {
                                points = 3
                            }
                        }

                        when {
                            extendedData.avgFeeRate <= 5 -> {
                                points--
                            }

                            extendedData.avgFeeRate in 6..9 -> {
                                points++
                            }

                            extendedData.avgFeeRate in 10..16 -> {
                                points += 2
                            }

                            extendedData.avgFeeRate > 17 -> {
                                points += 3
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

                        when {
                            points <= 0 -> { // -3 to 0
                                _congestionRating.postValue(CongestionRating.LIGHT)
                            }
                            points in 1..3 -> {
                                _congestionRating.postValue(CongestionRating.NORMAL)
                            }
                            points in 4..6 -> {
                                _congestionRating.postValue(CongestionRating.MED)
                            }
                            points in 7..8 -> {
                                _congestionRating.postValue(CongestionRating.BUSY)
                            }
                            else -> { // 9
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

    fun setTickerPrice() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _tickerPrice.postValue(SimpleCurrencyFormat.formatValue(localStoreRepository.getLocalCurrency(), apiRepository.getBasicTickerData().price))

                getChartData()?.let {
                    val gain = 100 * ((it.last().value - it.first().value) / it.first().value)
                    _percentageGain.postValue(gain.toDouble())
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