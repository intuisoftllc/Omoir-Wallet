package com.intuisoft.plaid.features.dashboardflow.shared.viewModel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.intuisoft.plaid.PlaidApp
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.SingleLiveData
import com.intuisoft.plaid.androidwrappers.SwapPairItemView
import com.intuisoft.plaid.androidwrappers.WalletViewModel
import com.intuisoft.plaid.common.coroutines.PlaidScope
import com.intuisoft.plaid.delegates.DelegateManager
import com.intuisoft.plaid.common.model.EstimatedReceiveAmountModel
import com.intuisoft.plaid.common.model.ExchangeInfoDataModel
import com.intuisoft.plaid.common.network.blockchair.response.SupportedCurrencyModel
import com.intuisoft.plaid.common.repositories.ApiRepository
import com.intuisoft.plaid.common.repositories.LocalStoreRepository
import com.intuisoft.plaid.common.util.Constants
import com.intuisoft.plaid.common.util.Constants.Strings.BTC_TICKER
import com.intuisoft.plaid.common.util.SimpleCoinNumberFormat
import com.intuisoft.plaid.common.util.extensions.roundTo
import com.intuisoft.plaid.common.util.extensions.safeWalletScope
import com.intuisoft.plaid.util.NetworkUtil
import com.intuisoft.plaid.common.delegates.wallet.WalletDelegate
import kotlinx.coroutines.*
import java.time.Instant
import java.util.*


class ExchangeViewModel(
    application: Application,
    private val apiRepository: ApiRepository,
    private val localStoreRepository: LocalStoreRepository,
    private val walletManager: WalletDelegate,
    private val delegateManager: DelegateManager
): WalletViewModel(application, localStoreRepository, apiRepository, walletManager, delegateManager) {

    protected val _sendPairInfo = SingleLiveData<SwapPairInfo>()
    val sendPairInfo: LiveData<SwapPairInfo> = _sendPairInfo

    protected val _recievePairInfo = SingleLiveData<SwapPairInfo>()
    val recievePairInfo: LiveData<SwapPairInfo> = _recievePairInfo

    protected val _conversionAmount = SingleLiveData<Double>()
    val conversionAmount: LiveData<Double> = _conversionAmount

    protected val _range = SingleLiveData<Pair<String, String>?>()
    val range: LiveData<Pair<String, String>?> = _range

    protected val _screenFunctionsEnabled = SingleLiveData<Boolean>()
    val screenFunctionsEnabled: LiveData<Boolean> = _screenFunctionsEnabled

    protected val _onDisplayExplanation = SingleLiveData<String>()
    val onDisplayExplanation: LiveData<String> = _onDisplayExplanation

    protected val _confirmButtonEnabled = SingleLiveData<Boolean>()
    val confirmButtonEnabled: LiveData<Boolean> = _confirmButtonEnabled

    protected val _disableBtcSeding = SingleLiveData<Unit>()
    val disableBtcSeding: LiveData<Unit> = _disableBtcSeding

    protected val _showContent = SingleLiveData<Boolean>()
    val showContent: LiveData<Boolean> = _showContent

    protected val _creatingExchange = SingleLiveData<Boolean>()
    val creatingExchange: LiveData<Boolean> = _creatingExchange

    protected val _getReceiveAddress = SingleLiveData<SupportedCurrencyModel>()
    val getReceiveAddress: LiveData<SupportedCurrencyModel> = _getReceiveAddress

    protected val _getRefundAddress = SingleLiveData<SupportedCurrencyModel>()
    val getRefundAddress: LiveData<SupportedCurrencyModel> = _getRefundAddress

    protected val _exchangeInfoDisplay = SingleLiveData<ExchangeInfoDisplay>()
    val exchangeInfoDisplay: LiveData<ExchangeInfoDisplay> = _exchangeInfoDisplay

    protected val _onNext = SingleLiveData<ExchangeInfoDataModel>()
    val onNext: LiveData<ExchangeInfoDataModel> = _onNext

    protected val _estimatedReceiveAmount = SingleLiveData<Double?>()
    val estimatedReceiveAmount: LiveData<Double?> = _estimatedReceiveAmount

    private var outboundCurrency: SupportedCurrencyModel? = null
    private var inboundCurrency: SupportedCurrencyModel? = null
    private var receiveAddress = ""
    private var receiveAddressMemo = ""
    private var refundAddress = ""
    private var refundAddressMemo = ""
    private var min: Double = 0.0
    private var max: Double? = null
    private var sendAmount: Double = 0.0
    private var receiveEstimate: EstimatedReceiveAmountModel = EstimatedReceiveAmountModel("", Instant.now(), 0.0)
    private var wholeCoinConversion: Double = 0.0
    private var bitcoinAmountLimitErrors = 0
    private var maxSwapErrors = 0
    private var ignoreNoNetwork = false
    private var searchValue = ""
    private var estimatedReceiveAmountJob: Job? = null
        set(value) {
            synchronized(this) {
                field = value
            }
        }

    fun updateSearchValue(search: String, onResult: (Pair<List<SupportedCurrencyModel>, List<SupportedCurrencyModel>>) -> Unit) {
        searchValue = search.trim()
        performSearch(onResult)
    }

    fun updateEstimatedReceiveAmount() {
        estimatedReceiveAmountJob?.cancel()
        estimatedReceiveAmountJob = PlaidScope.applicationScope.launch(Dispatchers.IO) {
            _estimatedReceiveAmount.postValue(null)
            if(sendAmount != 0.0) delay(Constants.Time.ESTIMATED_RECEIVE_AMOUNT_UPDATE_TIME)
            if(outboundCurrency != null && inboundCurrency != null)
                receiveEstimate = apiRepository.getEstimatedAmount(outboundCurrency!!, inboundCurrency!!, sendAmount)
            else receiveEstimate = EstimatedReceiveAmountModel("", Instant.now(), 0.0)
            _estimatedReceiveAmount.postValue(receiveEstimate.toAmount)
            estimatedReceiveAmountJob = null
            _confirmButtonEnabled.postValue(min != 0.0 && sendAmount >= min)
        }
    }

    private fun performSearch(onResult: (Pair<List<SupportedCurrencyModel>, List<SupportedCurrencyModel>>) -> Unit) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                safeWalletScope {
                    val exchangedCurrencies = localStoreRepository.getAllExchanges(getWalletId())
                        .takeLast(20)
                        .map {
                            if (it.fromShort.lowercase() == BTC_TICKER)
                                it.toId
                            else it.fromId
                        }
                    val timesUsed = mutableListOf<Pair<String, Int>>()

                    for (item in exchangedCurrencies.distinct()) {
                        timesUsed.add(item to Collections.frequency(exchangedCurrencies, item))
                    }

                    val supportedCurrencies = apiRepository.getSupportedCurrencies()
                        .filter {
                            it.ticker != BTC_TICKER
                        }

                    val sorted = timesUsed.sortedByDescending { it.second }.take(3)
                        .filter { frequent ->
                            supportedCurrencies.find { it.id == frequent.first } != null
                        }.map { frequent ->
                            supportedCurrencies.find { frequent.first == it.id }!!
                        }

                    if (searchValue.isBlank()) {
                        withContext(Dispatchers.Main) {
                            safeWalletScope {
                                onResult(sorted to supportedCurrencies)
                            }
                        }
                    } else {
                        val filtered = supportedCurrencies.filter {
                            it.ticker.contains(searchValue, true) || it.name.contains(
                                searchValue,
                                true
                            )
                        }

                        withContext(Dispatchers.Main) {
                            safeWalletScope {
                                onResult(sorted to filtered)
                            }
                        }
                    }
                }
            }
        }
    }

    fun validateSendAmount(sending: Double?) : Boolean {
        sending?.let { it ->
            if(outboundCurrency?.ticker?.lowercase() == BTC_TICKER && it > Constants.Limit.BITCOIN_SUPPLY_CAP
                || (outboundCurrency?.ticker?.lowercase() != BTC_TICKER && (it * wholeCoinConversion) > Constants.Limit.BITCOIN_SUPPLY_CAP)) {
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
        updateEstimatedReceiveAmount()
        _conversionAmount.postValue(receiveEstimate.toAmount)
        _confirmButtonEnabled.postValue(false)
        return true
    }

    fun onNoInternet(hasInternet: Boolean) {
        viewModelScope.launch{
            withContext(Dispatchers.IO) {
                safeWalletScope {
                    if (!ignoreNoNetwork) {
                        if (!hasInternet) {
                            _showContent.postValue(false)
                        } else {
                            _showContent.postValue(true)
                            if (outboundCurrency != null && inboundCurrency != null)
                                modifySendReceive(outboundCurrency!!, inboundCurrency!!)
                            else setInitialValues()
                        }
                    }
                }
            }
        }
    }

    fun setRefundAddress(address: String, memo: String) {
        refundAddress = address
        refundAddressMemo = memo
    }

    fun checkAddress(currency: SupportedCurrencyModel, address: String, onResult: (Pair<Boolean, String?>) -> Unit) {
        PlaidScope.applicationScope.launch(Dispatchers.IO) {
            val result = apiRepository.isAddressValid(currency, address)
            PlaidScope.MainScope.launch {
                onResult(result)
            }
        }
    }

    fun setReceiveAddress(address: String, memo: String) {
        receiveAddress = address
        receiveAddressMemo = memo
    }

    fun getMemo(): String {
        val isBitcoin = inboundCurrency?.ticker?.lowercase() == BTC_TICKER

        if(isBitcoin) {
            if(refundAddressMemo.isNotEmpty()) return refundAddressMemo
            else return getApplication<PlaidApp>().getString(R.string.not_applicable)
        } else {
            if(receiveAddressMemo.isNotEmpty()) return receiveAddressMemo
            else return getApplication<PlaidApp>().getString(R.string.not_applicable)
        }
    }

    fun confirmExchange() {
        val sendingBitcoin = outboundCurrency?.ticker?.lowercase() == BTC_TICKER
        val receivingBitcoin = inboundCurrency?.ticker?.lowercase() == BTC_TICKER

        _exchangeInfoDisplay.postValue(
            ExchangeInfoDisplay(
                recipient =
                    if(receivingBitcoin) getApplication<PlaidApp>().getString(R.string.swap_auto_generated_recipient_address, receiveAddress)
                    else  receiveAddress,
                sender =
                    if(sendingBitcoin) getApplication<PlaidApp>().getString(R.string.swap_bitcoin_sender)
                    else getApplication<PlaidApp>().getString(R.string.swap_external_wallet_sender, outboundCurrency?.ticker?.uppercase()),
                refundAddress =
                    if(receivingBitcoin) refundAddress
                    else getApplication<PlaidApp>().getString(R.string.swap_auto_generated_recipient_address, refundAddress),
                memo = getMemo(),
                amountSent = "${SimpleCoinNumberFormat.format(sendAmount)} ${outboundCurrency!!.ticker}",
                amountReceived = "~${if(receiveEstimate.toAmount == 0.0) " ?" else SimpleCoinNumberFormat.format(receiveEstimate.toAmount)} ${inboundCurrency!!.ticker}"
            )
        )
    }

    fun createExchange() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                safeWalletScope {
                    ignoreNoNetwork = true
                    _creatingExchange.postValue(true)
                    val rateId =
                        if(receiveEstimate.rateId.isNotBlank() && Instant.now().isBefore(receiveEstimate.validUntil))
                            receiveEstimate.rateId
                        else null

                    val exchangeData = apiRepository.createExchange(
                        from = outboundCurrency!!,
                        to = inboundCurrency!!,
                        rateId = rateId,
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
    }

    private fun saveLastTicker() {
        if(outboundCurrency!!.ticker.lowercase() != BTC_TICKER) {
            localStoreRepository.setLastExchangeCurrency(outboundCurrency!!.id)
        } else {
            localStoreRepository.setLastExchangeCurrency(inboundCurrency!!.id)
        }
    }

    fun setInitialValues() {
        PlaidScope.applicationScope.launch(Dispatchers.IO) {
            safeWalletScope {
                if (NetworkUtil.hasInternet(getApplication<PlaidApp>())) {
                    sendAmount = 0.0
                    receiveEstimate = EstimatedReceiveAmountModel("", Instant.now(), 0.0)
                    min = 0.0
                    max = null
                    wholeCoinConversion = 0.0
                    clearAddresses()
                    _showContent.postValue(true)
                    _confirmButtonEnabled.postValue(false)
                    val lastCurrencyId = localStoreRepository.getLastExchangeCurrency()
                    var lastCurrency: SupportedCurrencyModel? = null
                    var supportedCurrencies = apiRepository.getSupportedCurrencies()
                    lastCurrency = supportedCurrencies.find { it.id == lastCurrencyId }

                    if (lastCurrency == null) {
                        lastCurrency = getCurrencyByTicker("eth")
                    }

                    if (isReadOnly()) {
                        _disableBtcSeding.postValue(Unit)
                        modifySendReceive(lastCurrency!!, getCurrencyByTicker(BTC_TICKER)!!)
                    } else {
                        if (localStoreRepository.isSendingBTC()) {
                            modifySendReceive(
                                getCurrencyByTicker(BTC_TICKER)!!,
                                lastCurrency!!
                            )
                        } else {
                            modifySendReceive(
                                lastCurrency!!,
                                getCurrencyByTicker(BTC_TICKER)!!
                            )
                        }
                    }
                } else {
                    onNoInternet(false)
                }
            }
        }
    }

    private suspend fun getCurrencyByTicker(ticker: String): SupportedCurrencyModel? {
        var supportedCurrencies = apiRepository.getSupportedCurrencies()
        var currency: SupportedCurrencyModel? = supportedCurrencies.find { it.ticker == ticker }
        return currency
    }

    fun setOutboundCurrency(outbound: SupportedCurrencyModel) {
        modifySendReceive(outbound, inboundCurrency!!)
    }

    fun setInboundCurrency(inbound: SupportedCurrencyModel) {
        modifySendReceive(outboundCurrency!!, inbound)
    }

    private fun modifySendReceive(from: SupportedCurrencyModel, to: SupportedCurrencyModel) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                safeWalletScope {
                    _screenFunctionsEnabled.postValue(false)
                    setSendCurrencyInternal(from).join()
                    setReceiveCurrencyInternal(to).join()
                    setFixedFloatingRange().join()
                    wholeCoinConversion = 0.0
                    updateEstimatedReceiveAmount()
                    saveLastTicker()
                    _screenFunctionsEnabled.postValue(true)
                }
            }
        }
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
            _onDisplayExplanation.postValue(getApplication<PlaidApp>().getString(R.string.swap_limit_reached_error, outboundCurrency!!.ticker.uppercase(), max?.toString() ?: "0"))
        }
    }

    fun swapSendReceive() {
        val newSend = inboundCurrency!!
        val newRecipient = outboundCurrency!!
        sendAmount = 0.0
        receiveEstimate = EstimatedReceiveAmountModel("", Instant.now(), 0.0)
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
                safeWalletScope {
                    if (max == null || sendAmount <= (max ?: 0.0)) {
                        val isBitcoin = outboundCurrency!!.ticker.lowercase() == BTC_TICKER
                        clearAddresses()

                        if (isBitcoin) {
                            refundAddress = getRecieveAddress()
                            _getReceiveAddress.postValue(inboundCurrency!!)
                        } else {
                            receiveAddress = getRecieveAddress()
                            _getRefundAddress.postValue(outboundCurrency!!)
                        }
                    } else {
                        _onDisplayExplanation.postValue(
                            getApplication<PlaidApp>().getString(
                                R.string.swap_limit_reached_error,
                                outboundCurrency!!.ticker.uppercase(),
                                max?.toString() ?: "0"
                            )
                        )
                        _confirmButtonEnabled.postValue(false)
                    }
                }
            }
        }
    }

    private fun setSendCurrencyInternal(currency: SupportedCurrencyModel): Job {
        outboundCurrency = currency

        return viewModelScope.launch {
            withContext(Dispatchers.IO) {
                safeWalletScope {
                    val currency = apiRepository.getSupportedCurrencies()
                        .filter { it.id == currency.id }

                    if (currency.isNotEmpty()) {
                        val crypto = currency.first()
                        val isBitcoin = outboundCurrency!!.ticker.lowercase() == BTC_TICKER
                        localStoreRepository.setIsSendingBTC(isBitcoin)

                        _sendPairInfo.postValue(
                            SwapPairInfo(
                                outboundCurrency!!.ticker.uppercase(),
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
                                outboundCurrency!!.ticker
                            )
                        )
                    }
                }
            }
        }
    }

    private fun setReceiveCurrencyInternal(currency: SupportedCurrencyModel) : Job {
        inboundCurrency = currency

        return viewModelScope.launch {
            withContext(Dispatchers.IO) {
                safeWalletScope {
                    val currency = apiRepository.getSupportedCurrencies()
                        .filter { it.id == currency.id }

                    if (currency.isNotEmpty()) {
                        val crypto = currency.first()
                        val isBitcoin = inboundCurrency!!.ticker.lowercase() == BTC_TICKER

                        _recievePairInfo.postValue(
                            SwapPairInfo(
                                inboundCurrency!!.ticker.uppercase(),
                                if (isBitcoin) null else crypto.image,
                                receiveEstimate.toAmount,
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
                                inboundCurrency!!.ticker
                            )
                        )
                    }
                }
            }
        }
    }

    private fun setFixedFloatingRange() : Job {
        return viewModelScope.launch {
            withContext(Dispatchers.IO) {
                safeWalletScope {
                    val range =
                        apiRepository.getCurrencyRangeLimit(outboundCurrency!!, inboundCurrency!!)
                    if (range != null) {
                        min = range.min.toDouble().roundTo(8)
                        max = range.max?.toDouble()?.roundTo(8)
                        _range.postValue(range.min to (range.max ?: "∞"))
                    } else {
                        min = 0.0
                        max = 0.0
                        _range.postValue(null)
                        _onDisplayExplanation.postValue(
                            getApplication<PlaidApp>().getString(
                                R.string.swap_could_not_load_min_max_error,
                                outboundCurrency,
                                inboundCurrency
                            )
                        )
                    }
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
        val amountSent: String,
        val amountReceived: String
    )

    companion object {
        private const val errorThreshold = 5
    }
}