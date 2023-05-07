package com.intuisoft.plaid.features.dashboardflow.shared.viewModel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.intuisoft.plaid.PlaidApp
import com.intuisoft.plaid.androidwrappers.SingleLiveData
import com.intuisoft.plaid.androidwrappers.WalletViewModel
import com.intuisoft.plaid.common.coroutines.PlaidScope
import com.intuisoft.plaid.delegates.DelegateManager
import com.intuisoft.plaid.common.model.ChartDataModel
import com.intuisoft.plaid.common.model.ChartIntervalType
import com.intuisoft.plaid.common.model.CongestionRating
import com.intuisoft.plaid.common.repositories.ApiRepository
import com.intuisoft.plaid.common.repositories.LocalStoreRepository
import com.intuisoft.plaid.common.util.SimpleCoinNumberFormat
import com.intuisoft.plaid.common.util.SimpleCurrencyFormat
import com.intuisoft.plaid.common.util.extensions.safeWalletScope
import com.intuisoft.plaid.util.NetworkUtil
import com.intuisoft.plaid.common.delegates.wallet.WalletDelegate
import io.horizontalsystems.bitcoinkit.BitcoinKit
import kotlinx.coroutines.*


class MarketViewModel(
    application: Application,
    apiRepository: ApiRepository,
    private val localStoreRepository: LocalStoreRepository,
    private val walletManager: WalletDelegate,
    private val delegateManager: DelegateManager
): WalletViewModel(application, localStoreRepository, apiRepository, walletManager, delegateManager) {

    protected val _marketCap = SingleLiveData<String>()
    val marketCap: LiveData<String> = _marketCap

    protected val _totalVolume = SingleLiveData<String>()
    val totalVolume: LiveData<String> = _totalVolume

    protected val _circulatingSupply = SingleLiveData<String>()
    val circulatingSupply: LiveData<String> = _circulatingSupply

    protected val _maxSupply = SingleLiveData<String>()
    val maxSupply: LiveData<String> = _maxSupply

    protected val _blockStatsTitles = SingleLiveData<List<String>>()
    val blockStatsTitles: LiveData<List<String>> = _blockStatsTitles

    protected val _blockStatsSubTitles = SingleLiveData<List<Pair<String, String>>>()
    val blockStatsSubTitles: LiveData<List<Pair<String, String>>> = _blockStatsSubTitles

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

    protected val _blockStatsDataLoading = SingleLiveData<Boolean>()
    val blockStatsDataLoading: LiveData<Boolean> = _blockStatsDataLoading

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
        PlaidScope.applicationScope.launch(Dispatchers.IO) {
            safeWalletScope {
                val basicData = delegateManager.current().marketDelegate.fetchBasicTickerData()
                val extendedData = delegateManager.current().networkDelegate.fetchExtendedNetworkData(getWalletNetwork() == BitcoinKit.NetworkType.TestNet)
                val chartData = getChartData()

                if (basicData.marketCap != 0.0 && extendedData.isNotEmpty() && chartData != null) {
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
    }

    fun updateBasicMarketData() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                safeWalletScope {
                    _basicNetworkDataLoading.postValue(true)
                    val data = delegateManager.current().marketDelegate.fetchBasicTickerData()

                    var mktCap = SimpleCurrencyFormat.formatValue(
                        localStoreRepository.getLocalCurrency(),
                        data.marketCap
                    )

                    var vol = SimpleCurrencyFormat.formatValue(
                        localStoreRepository.getLocalCurrency(),
                        data.totalVolume
                    )

                    if (mktCap.contains(".00")) {
                        mktCap = mktCap.replace(".00", "")
                    }

                    if (vol.contains(".00")) {
                        vol = vol.replace(".00", "")
                    }

                    _marketCap.postValue(mktCap)
                    _totalVolume.postValue(vol)
                    _circulatingSupply.postValue(SimpleCoinNumberFormat.format(data.circulatingSupply) + " ${delegateManager.current().symbol}")
                    _maxSupply.postValue(SimpleCoinNumberFormat.format(data.maxSupply.toLong()) + " ${delegateManager.current().symbol}")
                    _basicNetworkDataLoading.postValue(false)
                }
            }
        }
    }

    fun updateExtendedMarketData() {
        if(!localStoreRepository.isPremiumUser()) return

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                safeWalletScope {
                    _blockStatsDataLoading.postValue(true)
                    _blockStatsTitles.postValue(delegateManager.current().networkDelegate.getExtendedNetworkDataTitles())
                    val data = delegateManager.current().networkDelegate.fetchExtendedNetworkData(getWalletNetwork() == BitcoinKit.NetworkType.TestNet)

                    if(data.isNotEmpty()) {
                        _blockStatsSubTitles.postValue(
                            data
                        )
                    } else {
                        _couldNotLoadData.postValue(Unit)
                    }

                    _blockStatsDataLoading.postValue(false)
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
                            delegateManager.current().marketDelegate.fetchBasicTickerData().price
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

    private suspend fun getChartData() = delegateManager.current().marketDelegate.fetchChartDataForInterval(intervalType)
}