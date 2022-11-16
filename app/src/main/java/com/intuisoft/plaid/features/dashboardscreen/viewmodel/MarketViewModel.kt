package com.intuisoft.plaid.features.dashboardscreen.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.intuisoft.plaid.PlaidApp
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.SingleLiveData
import com.intuisoft.plaid.androidwrappers.WalletViewModel
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

    fun updateBasicMarketData() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val data = apiRepository.getBasicTickerData()

                var mktCap = SimpleCurrencyFormat.formatValue(
                    localStoreRepository.getLocalCurrency(),
                    data.marketCap.toDouble()
                )

                if(mktCap.contains(".00")) {
                    mktCap = mktCap.replace(".00", "")
                }

                _marketCap.postValue(mktCap)
                _circulatingSupply.postValue(SimpleCoinNumberFormat.format(data.circulatingSupply) + " BTC")
                _maxSupply.postValue(SimpleCoinNumberFormat.format(data.maxSupply.toLong()) + " BTC")

                if(getWalletNetwork() == BitcoinKit.NetworkType.TestNet) {
                    _congestionRating.postValue(CongestionRating.NA)
                } else {
                    when {
                        Constants.CongestionRating.LIGHT.contains(data.memPoolTxCount) -> {
                            _congestionRating.postValue(CongestionRating.LIGHT)
                        }
                        Constants.CongestionRating.NORMAL.contains(data.memPoolTxCount) -> {
                            _congestionRating.postValue(CongestionRating.NORMAL)
                        }
                        Constants.CongestionRating.MED.contains(data.memPoolTxCount) -> {
                            _congestionRating.postValue(CongestionRating.MED)
                        }
                        Constants.CongestionRating.BUSY.contains(data.memPoolTxCount) -> {
                            _congestionRating.postValue(CongestionRating.BUSY)
                        }
                        else -> {
                            _congestionRating.postValue(CongestionRating.CONGESTED)
                        }
                    }
                }
            }
        }
    }

    fun updateExtendedMarketData() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                if(getWalletNetwork() == BitcoinKit.NetworkType.TestNet) {
                    _hideMainChainDataContainer.postValue(Unit)
                    _network.postValue(getApplication<PlaidApp>().getString(R.string.market_data_extended_item_1_description2))
                } else {
                    _network.postValue(getApplication<PlaidApp>().getString(R.string.market_data_extended_item_1_description))
                }

                val data = apiRepository.getExtendedMarketData(getWalletNetwork() == BitcoinKit.NetworkType.TestNet)

                if(data != null) {
                    _blockHeight.postValue(SimpleCoinNumberFormat.format(data.height.toLong()))
                    _difficulty.postValue(SimpleCoinNumberFormat.format(data.difficulty))
                    _blockchainSize.postValue(data.blockchainSize.humanReadableByteCountSI())
                    _avgTxSize.postValue(SimpleCoinNumberFormat.format(data.avgTxSize.toLong()) + " ${getApplication<PlaidApp>().getString(R.string.bytes)}")
                    _avgFeeRate.postValue(SimpleCoinNumberFormat.format(data.avgFeeRate.toLong()) + " ${getApplication<PlaidApp>().getString(R.string.sat_per_byte)}")
                    _unconfirmedTxs.postValue(SimpleCoinNumberFormat.format(data.unconfirmedTxs.toLong()))
                    _avgConfTime.postValue(SimpleCoinNumberFormat.formatCurrency(data.avgConfTime))
                } else {
                    _couldNotLoadData.postValue(Unit)
                }
            }
        }
    }
}