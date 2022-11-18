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
import kotlinx.coroutines.*


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

    protected val _conversionAmount = SingleLiveData<Double>()
    val conversionAmount: LiveData<Double> = _conversionAmount

    protected val _minMax = SingleLiveData<Pair<String, String>?>()
    val minMax: LiveData<Pair<String, String>?> = _minMax

    protected val _sendReceiveSwapEnabled = SingleLiveData<Boolean>()
    val sendReceiveSwapEnabled: LiveData<Boolean> = _sendReceiveSwapEnabled

    private var fixed: Boolean = true
    private var sendTicker = ""
    private var receiveTicker = ""
    private var min: Double = 0.0
    private var max: Double? = null
    private var sendAmount: Double = 0.0
    private var receiveAmount: Double = 0.0
    private var wholeCoinConversion: Double = 0.0

    fun setFixed(fixed: Boolean) {
        this.fixed = fixed
        setMinMax()
    }

    fun validateSendAmount(sending: Double?) : Boolean {
        sending?.let { it ->
            max?.let { it2 ->
                if(it > it2) {
                    return false
                }
            }
        }

        GlobalScope.launch {
            updateWholeCoinConversion(sendTicker, receiveTicker)
            sendAmount = sending ?: 0.0
            receiveAmount = sendAmount * wholeCoinConversion
            _conversionAmount.postValue(receiveAmount)
        }
        return true
    }

    fun setInitialValues() {
        this.fixed = true
        modifySendReceive("btc", "eth")
    }

    private fun modifySendReceive(from: String, to: String) {
        GlobalScope.launch {
            _sendReceiveSwapEnabled.postValue(false)
            setSendCurrency(from).join()
            setReceiveCurrency(to).join()
            setMinMax().join()
            updateWholeCoinConversion(from, to)
            _sendReceiveSwapEnabled.postValue(true)
        }
    }

    suspend fun updateWholeCoinConversion(from: String, to: String) {
        wholeCoinConversion =  apiRepository.getWholeCoinConversion(from, to, fixed)
    }

    fun swapSendReceive() {
        val newSend = receiveTicker
        val newRecipient = sendTicker
        sendAmount = 0.0
        receiveAmount = 0.0
        modifySendReceive(newSend, newRecipient)
    }

    fun setSendCurrency(ticker: String): Job {
        sendTicker = ticker

        return GlobalScope.launch {
            val currency = apiRepository.getSupportedCurrencies(fixed)
                .filter { it.ticker.lowercase() == ticker.lowercase() }

            if(currency.isNotEmpty()) {
                val crypto = currency.first()
                val isBitcoin = ticker.lowercase() == "btc"

                _sendPairInfo.postValue(
                    SwapPairInfo(
                        ticker.uppercase(),
                        if(isBitcoin) null else crypto.image,
                        sendAmount,
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

    fun setReceiveCurrency(ticker: String) : Job {
        receiveTicker = ticker

        return GlobalScope.launch {
            val currency = apiRepository.getSupportedCurrencies(fixed)
                .filter { it.ticker.lowercase() == ticker.lowercase() }

            if(currency.isNotEmpty()) {
                val crypto = currency.first()
                val isBitcoin = ticker.lowercase() == "btc"

                _recievePairInfo.postValue(
                    SwapPairInfo(
                        ticker.uppercase(),
                        if(isBitcoin) null else crypto.image,
                        receiveAmount,
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

    fun setMinMax() : Job {
        return GlobalScope.launch {
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

    data class SwapPairInfo(
        val ticker: String,
        val symbol: String?,
        val receiveValue: Double,
        val pairSendReciveTitle: String,
        val pairType: Int
    )
}