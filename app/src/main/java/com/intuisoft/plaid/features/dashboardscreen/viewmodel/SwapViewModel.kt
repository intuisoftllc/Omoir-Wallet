package com.intuisoft.plaid.features.dashboardscreen.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.intuisoft.plaid.PlaidApp
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.SingleLiveData
import com.intuisoft.plaid.androidwrappers.SwapPairItemView
import com.intuisoft.plaid.androidwrappers.WalletViewModel
import com.intuisoft.plaid.common.model.ChartIntervalType
import com.intuisoft.plaid.common.repositories.ApiRepository
import com.intuisoft.plaid.common.repositories.LocalStoreRepository
import com.intuisoft.plaid.walletmanager.AbstractWalletManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class SwapViewModel(
    application: Application,
    private val apiRepository: ApiRepository,
    private val localStoreRepository: LocalStoreRepository,
    private val walletManager: AbstractWalletManager
): WalletViewModel(application, localStoreRepository, apiRepository, walletManager) {

    protected val _upgradeToPro = SingleLiveData<Boolean>()
    val upgradeToPro: LiveData<Boolean> = _upgradeToPro

    protected val _sendPairInfo = SingleLiveData<SwapPairInfo>()
    val sendPairInfo: LiveData<SwapPairInfo> = _sendPairInfo

    protected val _recievePairInfo = SingleLiveData<SwapPairInfo>()
    val recievePairInfo: LiveData<SwapPairInfo> = _recievePairInfo

    protected val _minMax = SingleLiveData<Pair<String, String>?>()
    val minMax: LiveData<Pair<String, String>?> = _minMax

    private var fixed: Boolean = true
    private var sendTicker = ""
    private var receiveTicker = ""
    private var min: Double = 0.0
    private var max: Double? = null
    private var sendAmount: Double = 0.0

    fun setFixed(fixed: Boolean) {
        this.fixed = fixed
        updateConversionValues()
    }

    private fun updateConversionValues() {
        setMinMax()
    }

    fun validateSendAmount() {

    }

    fun setInitialValues() {
        this.fixed = true
        setSendCurrency("btc")
        setReceiveCurrency("eth")
        setMinMax()
    }

    fun swapSendReceive() {
        val newSend = receiveTicker
        val newRecipient = sendTicker
        setSendCurrency(newSend)
        setReceiveCurrency(newRecipient)
        setMinMax()
    }

    fun setSendCurrency(ticker: String) {
        sendTicker = ticker

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val currency = apiRepository.getSupportedCurrencies(fixed)
                    .filter { it.ticker.lowercase() == ticker.lowercase() }

                if(currency.isNotEmpty()) {
                    val crypto = currency.first()
                    val isBitcoin = ticker.lowercase() == "btc"

                    _sendPairInfo.postValue(
                        SwapPairInfo(
                            ticker.uppercase(),
                            if(isBitcoin) null else crypto.image,
                            0.0,
                            getApplication<PlaidApp>().getString(
                                if(isBitcoin) R.string.swap_send_variant_1 else R.string.swap_send_variant_2,
                                crypto.name
                            ),
                            if(isBitcoin) SwapPairItemView.ENTER_VALUE_VARIANT_1 else SwapPairItemView.ENTER_VALUE_VARIANT_2
                        )
                    )
                } else {
                    // error: could not find cryptocurrency: <ticker>
                }
            }
        }
    }

    fun setReceiveCurrency(ticker: String) {
        receiveTicker = ticker

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val currency = apiRepository.getSupportedCurrencies(fixed)
                    .filter { it.ticker.lowercase() == ticker.lowercase() }

                if(currency.isNotEmpty()) {
                    val crypto = currency.first()
                    val isBitcoin = ticker.lowercase() == "btc"

                    _recievePairInfo.postValue(
                        SwapPairInfo(
                            ticker.uppercase(),
                            if(isBitcoin) null else crypto.image,
                            0.0,
                            getApplication<PlaidApp>().getString(
                                if(isBitcoin) R.string.swap_receive_variant_2 else R.string.swap_receive_variant_1,
                                crypto.name
                            ),
                            if(isBitcoin) SwapPairItemView.SHOW_VALUE_VARIANT_2 else SwapPairItemView.SHOW_VALUE_VARIANT_1
                        )
                    )
                } else {
                    // error: could not find cryptocurrency: <ticker>
                }
            }
        }
    }

    fun setMinMax() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                if(sendTicker.isEmpty() || receiveTicker.isEmpty()) {
                    Log.e("LOOK", "empty")
                }
                val range = apiRepository.getCurrencyRangeLimit(sendTicker, receiveTicker, fixed)

                if (range != null) {
                    min = range.min.toDouble()
                    max = range.max?.toDouble()
                    _minMax.postValue(range.min to (range.max ?: "∞"))
                } else {
                    // show error
                    min = 0.0
                    max = 0.0
                    _minMax.postValue(null)
                }
            }
        }
    }

    data class SwapPairInfo(
        val ticker: String,
        val symbol: String?,
        val receiveValue: Double,
        val pairSendReciveTitle: String,
        val pairType: Int
    )
}