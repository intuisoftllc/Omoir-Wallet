package com.intuisoft.plaid.features.dashboardflow.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.intuisoft.plaid.PlaidApp
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.SingleLiveData
import com.intuisoft.plaid.androidwrappers.SwapPairItemView
import com.intuisoft.plaid.androidwrappers.WalletViewModel
import com.intuisoft.plaid.common.model.ExchangeInfoDataModel
import com.intuisoft.plaid.common.network.blockchair.response.SupportedCurrencyModel
import com.intuisoft.plaid.common.repositories.ApiRepository
import com.intuisoft.plaid.common.repositories.LocalStoreRepository
import com.intuisoft.plaid.common.util.Constants
import com.intuisoft.plaid.common.util.Constants.Strings.BTC_TICKER
import com.intuisoft.plaid.common.util.SimpleCoinNumberFormat
import com.intuisoft.plaid.util.NetworkUtil
import com.intuisoft.plaid.walletmanager.AbstractWalletManager
import kotlinx.coroutines.*
import java.util.*


class ExchangeViewModel(
    application: Application,
    private val apiRepository: ApiRepository,
    private val localStoreRepository: LocalStoreRepository,
    private val walletManager: AbstractWalletManager
): WalletViewModel(application, localStoreRepository, apiRepository, walletManager) {

    protected val _sendPairInfo = SingleLiveData<SwapPairInfo>()
    val sendPairInfo: LiveData<SwapPairInfo> = _sendPairInfo

    protected val _recievePairInfo = SingleLiveData<SwapPairInfo>()
    val recievePairInfo: LiveData<SwapPairInfo> = _recievePairInfo

    protected val _conversionAmount = SingleLiveData<Double>()
    val conversionAmount: LiveData<Double> = _conversionAmount

    protected val _fixedRange = SingleLiveData<Pair<String, String>?>()
    val fixedRange: LiveData<Pair<String, String>?> = _fixedRange

    protected val _floatingRange = SingleLiveData<Pair<String, String>?>()
    val floatingRange: LiveData<Pair<String, String>?> = _floatingRange

    protected val _screenFunctionsEnabled = SingleLiveData<Boolean>()
    val screenFunctionsEnabled: LiveData<Boolean> = _screenFunctionsEnabled

    protected val _onDisplayExplanation = SingleLiveData<String>()
    val onDisplayExplanation: LiveData<String> = _onDisplayExplanation

    protected val _confirmButtonEnabled = SingleLiveData<Boolean>()
    val confirmButtonEnabled: LiveData<Boolean> = _confirmButtonEnabled

    protected val _showContent = SingleLiveData<Boolean>()
    val showContent: LiveData<Boolean> = _showContent

    protected val _creatingExchange = SingleLiveData<Boolean>()
    val creatingExchange: LiveData<Boolean> = _creatingExchange

    protected val _getReceiveAddress = SingleLiveData<Pair<String, Pair<String, String?>>>()
    val getReceiveAddress: LiveData<Pair<String, Pair<String, String?>>> = _getReceiveAddress

    protected val _getRefundAddress = SingleLiveData<Pair<String, Pair<String, String?>>>()
    val getRefundAddress: LiveData<Pair<String, Pair<String, String?>>> = _getRefundAddress

    protected val _exchangeInfoDisplay = SingleLiveData<ExchangeInfoDisplay>()
    val exchangeInfoDisplay: LiveData<ExchangeInfoDisplay> = _exchangeInfoDisplay

    protected val _onNext = SingleLiveData<ExchangeInfoDataModel>()
    val onNext: LiveData<ExchangeInfoDataModel> = _onNext

    private var fixed: Boolean = true
    private var sendTicker = ""
    private var receiveTicker = ""
    private var receiveAddress = ""
    private var receiveAddressMemo = ""
    private var refundAddress = ""
    private var refundAddressMemo = ""
    private var min: Double = 0.0
    private var max: Double? = null
    private var sendAmount: Double = 0.0
    private var receiveAmount: Double = 0.0
    private var wholeCoinConversion: Double = 0.0
    private var bitcoinAmountLimitErrors = 0
    private var maxSwapErrors = 0
    private var ignoreNoNetwork = false
    private var searchValue = ""

    fun setFixed(fixed: Boolean) {
        this.fixed = fixed
        setFixedFloatingRange()
    }

    fun updateSearchValue(search: String, onResult: (Pair<List<SupportedCurrencyModel>, List<SupportedCurrencyModel>>) -> Unit) {
        searchValue = search.trim()
        performSearch(onResult)
    }

    private fun performSearch(onResult: (Pair<List<SupportedCurrencyModel>, List<SupportedCurrencyModel>>) -> Unit) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val exchangedCurrencies = localStoreRepository.getAllExchanges(getWalletId())
                    .takeLast(20).map {
                        if (it.fromShort.lowercase() == BTC_TICKER)
                            it.toShort
                        else it.fromShort
                    }
                val timesUsed = mutableListOf<Pair<String, Int>>()

                for (item in exchangedCurrencies.distinct()) {
                    timesUsed.add(item to Collections.frequency(exchangedCurrencies, item))
                }

                val supportedCurrencies = apiRepository.getSupportedCurrencies(fixed)
                val sorted = timesUsed.sortedByDescending { it.second }.take(3)
                    .filter { frequent ->
                        supportedCurrencies.find { it.ticker == frequent.first.lowercase() } != null
                    }.map { frequent ->
                        supportedCurrencies.find { frequent.first.lowercase() == it.ticker }!!
                    }

                if (searchValue.isBlank()) {
                    withContext(Dispatchers.Main) {
                        onResult(sorted to supportedCurrencies)
                    }
                } else {
                    val filtered = supportedCurrencies.filter {
                        it.ticker.contains(searchValue) || it.name.contains(searchValue)
                    }

                    withContext(Dispatchers.Main) {
                        onResult(sorted to filtered)
                    }
                }
            }
        }
    }

    fun validateSendAmount(sending: Double?) : Boolean {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                updateWholeCoinConversion(
                    sendTicker,
                    receiveTicker
                ) // refresh the value when needed
            }
        }

        sending?.let { it ->
            if(sendTicker.lowercase() == "btc" && it > Constants.Limit.BITCOIN_SUPPLY_CAP
                || (sendTicker.lowercase() != "btc" && (it * wholeCoinConversion) > Constants.Limit.BITCOIN_SUPPLY_CAP)) {
                onBitcoinAmountLimitError()
                return false
            }

            max?.let { it2 ->
                if(it > it2) {
                    onMaxSwapReachedErrorsError()
                    return false
                }
            }
        }

        sendAmount = sending ?: 0.0
        receiveAmount = sendAmount * wholeCoinConversion
        _conversionAmount.postValue(receiveAmount)
        _confirmButtonEnabled.postValue(min != 0.0 && sendAmount >= min)
        return true
    }

    fun onNoInternet(hasInternet: Boolean) {
        viewModelScope.launch{
            withContext(Dispatchers.IO) {
                if(!ignoreNoNetwork) {
                    if (!hasInternet) {
                        _showContent.postValue(false)
                    } else {
                        _showContent.postValue(true)
                        if (sendTicker.isNotEmpty() && receiveTicker.isNotEmpty())
                            modifySendReceive(sendTicker, receiveTicker)
                        else setInitialValues()
                    }
                }
            }
        }
    }

    fun setRefundAddress(address: String, memo: String) {
        refundAddress = address
        refundAddressMemo = memo
    }

    fun setReceiveAddress(address: String, memo: String) {
        receiveAddress = address
        receiveAddressMemo = memo
    }

    fun getMemo(): String {
        val isBitcoin = receiveTicker.lowercase() == "btc"

        if(isBitcoin) {
            if(refundAddressMemo.isNotEmpty()) return refundAddressMemo
            else return getApplication<PlaidApp>().getString(R.string.not_applicable)
        } else {
            if(receiveAddressMemo.isNotEmpty()) return receiveAddressMemo
            else return getApplication<PlaidApp>().getString(R.string.not_applicable)
        }
    }

    fun confirmExchange() {
        val sendingBitcoin = sendTicker.lowercase() == "btc"
        val receivingBitcoin = receiveTicker.lowercase() == "btc"

        _exchangeInfoDisplay.postValue(
            ExchangeInfoDisplay(
                recipient =
                    if(receivingBitcoin) getApplication<PlaidApp>().getString(R.string.swap_auto_generated_recipient_address, receiveAddress)
                    else  receiveAddress,
                sender =
                    if(sendingBitcoin) getApplication<PlaidApp>().getString(R.string.swap_bitcoin_sender)
                    else getApplication<PlaidApp>().getString(R.string.swap_external_wallet_sender, sendTicker.uppercase()),
                refundAddress =
                    if(receivingBitcoin) refundAddress
                    else getApplication<PlaidApp>().getString(R.string.swap_auto_generated_recipient_address, refundAddress),
                memo = getMemo(),
                exchangeType = if(fixed) getApplication<PlaidApp>().getString(R.string.swap_type_fixed) else getApplication<PlaidApp>().getString(R.string.swap_type_floating),
                amountSent = "${SimpleCoinNumberFormat.format(sendAmount)} $sendTicker",
                amountReceived = "~${if(receiveAmount == 0.0) " ?" else SimpleCoinNumberFormat.format(receiveAmount)} $receiveTicker"
            )
        )
    }

    fun createExchange() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                ignoreNoNetwork = true
                _creatingExchange.postValue(true)
                val exchangeData = apiRepository.createExchange(
                    fixed = fixed,
                    from = sendTicker,
                    to = receiveTicker,
                    receiveAddress = receiveAddress,
                    receiveAddressMemo = receiveAddressMemo,
                    refundAddress = refundAddress,
                    refundAddressMemo = refundAddressMemo,
                    amount = sendAmount,
                    walletId = getWalletId()
                )

                if (exchangeData != null) {
                    _onNext.postValue(exchangeData!!)
                } else {
                    _onDisplayExplanation.postValue(getApplication<PlaidApp>().getString(R.string.swap_failed_error))
                }

                _creatingExchange.postValue(false)
                ignoreNoNetwork = false
            }
        }
    }

    fun setInitialValues() {
        if(NetworkUtil.hasInternet(getApplication<PlaidApp>())) {
            this.fixed = true
            sendAmount = 0.0
            receiveAmount = 0.0
            min = 0.0
            max = null
            wholeCoinConversion = 0.0
            clearAddresses()
            _showContent.postValue(true)
            _confirmButtonEnabled.postValue(false)
            modifySendReceive("btc", "eth")
        } else {
            onNoInternet(false)
        }
    }

    fun setSendCurrency(sender: String) {
        modifySendReceive(sender, receiveTicker)
    }

    fun setReceiveCurrency(recipient: String) {
        modifySendReceive(sendTicker, recipient)
    }

    private fun modifySendReceive(from: String, to: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _screenFunctionsEnabled.postValue(false)
                setSendCurrencyInternal(from).join()
                setReceiveCurrencyInternal(to).join()
                setFixedFloatingRange().join()
                wholeCoinConversion = 0.0
                updateWholeCoinConversion(from, to)
                _screenFunctionsEnabled.postValue(true)
            }
        }
    }

    private suspend fun updateWholeCoinConversion(from: String, to: String) {
        if(wholeCoinConversion == 0.0)
            wholeCoinConversion =  apiRepository.getConversion(from, to, fixed)
    }

    private fun onBitcoinAmountLimitError() {
        bitcoinAmountLimitErrors++

        if(bitcoinAmountLimitErrors % errorThreshold == 0) {
            _onDisplayExplanation.postValue(getApplication<PlaidApp>().getString(R.string.swap_bitcoin_amount_limit_error))
        }
    }

    private fun onMaxSwapReachedErrorsError() {
        maxSwapErrors++

        if(maxSwapErrors % errorThreshold == 0) {
            _onDisplayExplanation.postValue(getApplication<PlaidApp>().getString(R.string.swap_limit_reached_error, sendTicker.uppercase(), max?.toString() ?: "0"))
        }
    }

    fun swapSendReceive() {
        val newSend = receiveTicker
        val newRecipient = sendTicker
        sendAmount = 0.0
        receiveAmount = 0.0
        clearAddresses()
        modifySendReceive(newSend, newRecipient)
    }

    private fun clearAddresses() {
        refundAddress = ""
        refundAddressMemo = ""
        receiveAddress = ""
        receiveAddressMemo = ""
    }

    fun onNext() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                if (max == null || sendAmount <= (max ?: 0.0)) {
                    val isBitcoin = sendTicker.lowercase() == "btc"
                    clearAddresses()

                    if (isBitcoin) {
                        refundAddress = getRecieveAddress()
                        val currency = apiRepository.getSupportedCurrencies(fixed)
                            .filter { it.ticker.lowercase() == receiveTicker.lowercase() }
                        val recipient = currency.first()

                        _getReceiveAddress.postValue(recipient.ticker to (recipient.validAddressRegex to recipient.validMemoRegex))
                    } else {
                        receiveAddress = getRecieveAddress()
                        val currency = apiRepository.getSupportedCurrencies(fixed)
                            .filter { it.ticker.lowercase() == sendTicker.lowercase() }
                        val sender = currency.first()

                        _getRefundAddress.postValue(sender.ticker to (sender.validAddressRegex to sender.validMemoRegex))
                    }
                } else {
                    _onDisplayExplanation.postValue(
                        getApplication<PlaidApp>().getString(
                            R.string.swap_limit_reached_error,
                            sendTicker.uppercase(),
                            max?.toString() ?: "0"
                        )
                    )
                    _confirmButtonEnabled.postValue(false)
                }
            }
        }
    }

    private fun setSendCurrencyInternal(ticker: String): Job {
        sendTicker = ticker

        return viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val currency = apiRepository.getSupportedCurrencies(fixed)
                    .filter { it.ticker.lowercase() == ticker.lowercase() }

                if (currency.isNotEmpty()) {
                    val crypto = currency.first()
                    val isBitcoin = ticker.lowercase() == "btc"

                    _sendPairInfo.postValue(
                        SwapPairInfo(
                            ticker.uppercase(),
                            if (isBitcoin) null else crypto.image,
                            sendAmount,
                            getApplication<PlaidApp>().getString(
                                if (isBitcoin) R.string.swap_send_variant_1 else R.string.swap_send_variant_2,
                                crypto.name
                            ),
                            if (isBitcoin) SwapPairItemView.ENTER_VALUE_VARIANT_1 else SwapPairItemView.ENTER_VALUE_VARIANT_2
                        )
                    )
                } else {
                    _onDisplayExplanation.postValue(
                        getApplication<PlaidApp>().getString(
                            R.string.swap_could_not_find_crypto_error,
                            ticker
                        )
                    )
                }
            }
        }
    }

    private fun setReceiveCurrencyInternal(ticker: String) : Job {
        receiveTicker = ticker

        return viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val currency = apiRepository.getSupportedCurrencies(fixed)
                    .filter { it.ticker.lowercase() == ticker.lowercase() }

                if (currency.isNotEmpty()) {
                    val crypto = currency.first()
                    val isBitcoin = ticker.lowercase() == "btc"

                    _recievePairInfo.postValue(
                        SwapPairInfo(
                            ticker.uppercase(),
                            if (isBitcoin) null else crypto.image,
                            receiveAmount,
                            getApplication<PlaidApp>().getString(
                                if (isBitcoin) R.string.swap_receive_variant_2 else R.string.swap_receive_variant_1,
                                crypto.name
                            ),
                            if (isBitcoin) SwapPairItemView.SHOW_VALUE_VARIANT_2 else SwapPairItemView.SHOW_VALUE_VARIANT_1
                        )
                    )
                } else {
                    _onDisplayExplanation.postValue(
                        getApplication<PlaidApp>().getString(
                            R.string.swap_could_not_find_crypto_error,
                            ticker
                        )
                    )
                }
            }
        }
    }

    private fun setFixedFloatingRange() : Job {
        return viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val range = apiRepository.getCurrencyRangeLimit(sendTicker, receiveTicker, fixed)
                if (range != null) {
                    min = range.min.toDouble()
                    max = range.max?.toDouble()
                    if (fixed) _fixedRange.postValue(range.min to (range.max ?: "∞"))
                    else _floatingRange.postValue(range.min to (range.max ?: "∞"))
                } else {
                    min = 0.0
                    max = 0.0
                    if (fixed) _fixedRange.postValue(null)
                    else _floatingRange.postValue(null)
                    _onDisplayExplanation.postValue(
                        getApplication<PlaidApp>().getString(
                            R.string.swap_could_not_load_min_max_error,
                            sendTicker,
                            receiveTicker
                        )
                    )
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

    data class ExchangeInfoDisplay(
        val recipient: String,
        val sender: String,
        val refundAddress: String,
        val memo: String,
        val exchangeType: String,
        val amountSent: String,
        val amountReceived: String
    )

    companion object {
        private const val errorThreshold = 5
    }
}