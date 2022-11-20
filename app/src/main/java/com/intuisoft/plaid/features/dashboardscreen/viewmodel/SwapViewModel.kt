package com.intuisoft.plaid.features.dashboardscreen.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import com.intuisoft.plaid.PlaidApp
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.SingleLiveData
import com.intuisoft.plaid.androidwrappers.SwapPairItemView
import com.intuisoft.plaid.androidwrappers.WalletViewModel
import com.intuisoft.plaid.common.repositories.ApiRepository
import com.intuisoft.plaid.common.repositories.LocalStoreRepository
import com.intuisoft.plaid.common.util.Constants
import com.intuisoft.plaid.util.NetworkUtil
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

    protected val _screenFunctionsEnabled = SingleLiveData<Boolean>()
    val screenFunctionsEnabled: LiveData<Boolean> = _screenFunctionsEnabled

    protected val _onDisplayExplanation = SingleLiveData<String>()
    val onDisplayExplanation: LiveData<String> = _onDisplayExplanation

    protected val _confirmButtonEnabled = SingleLiveData<Boolean>()
    val confirmButtonEnabled: LiveData<Boolean> = _confirmButtonEnabled

    protected val _showContent = SingleLiveData<Boolean>()
    val showContent: LiveData<Boolean> = _showContent

    protected val _getReceiveAddress = SingleLiveData<Pair<String, Pair<String, String?>>>()
    val getReceiveAddress: LiveData<Pair<String, Pair<String, String?>>> = _getReceiveAddress

    protected val _getRefundAddress = SingleLiveData<Pair<String, Pair<String, String?>>>()
    val getRefundAddress: LiveData<Pair<String, Pair<String, String?>>> = _getRefundAddress

    protected val _exchangeInfoDisplay = SingleLiveData<ExchangeInfoDisplay>()
    val exchangeInfoDisplay: LiveData<ExchangeInfoDisplay> = _exchangeInfoDisplay

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

        /*
        * Todo
        * finish swap screen
        * add "loading..." functionality to screen show in place of min,max
         */
    fun setFixed(fixed: Boolean) {
        this.fixed = fixed
        setMinMax()
    }

    fun validateSendAmount(sending: Double?) : Boolean {
        GlobalScope.launch {
            updateWholeCoinConversion(sendTicker, receiveTicker) // refresh the value when needed
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
        GlobalScope.launch {
            if(!hasInternet) {
                _showContent.postValue(false)
            } else {
                _showContent.postValue(true)
                if(sendTicker.isNotEmpty() && receiveTicker.isNotEmpty())
                    modifySendReceive(sendTicker, receiveTicker)
                else setInitialValues()
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
                amountSent = "$sendAmount $sendTicker",
                amountReceived = "~$receiveAmount $receiveTicker"
            )
        )
    }

    fun setInitialValues() {
        if(NetworkUtil.hasInternet(getApplication<PlaidApp>())) {
            this.fixed = true
            clearAddresses()
            _showContent.postValue(true)
            _confirmButtonEnabled.postValue(false)
            modifySendReceive("btc", "eth")
        } else {
            onNoInternet(false)
        }
    }

    private fun modifySendReceive(from: String, to: String) {
        GlobalScope.launch {
            _screenFunctionsEnabled.postValue(false)
            setSendCurrency(from).join()
            setReceiveCurrency(to).join()
            setMinMax().join()
            updateWholeCoinConversion(from, to)
            _screenFunctionsEnabled.postValue(true)
        }
    }

    private suspend fun updateWholeCoinConversion(from: String, to: String) {
        wholeCoinConversion =  apiRepository.getWholeCoinConversion(from, to, fixed)
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
        GlobalScope.launch {
            if(max == null || sendAmount <= (max ?: 0.0)) {
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
                _onDisplayExplanation.postValue(getApplication<PlaidApp>().getString(R.string.swap_limit_reached_error, sendTicker.uppercase(), max?.toString() ?: "0"))
                _confirmButtonEnabled.postValue(false)
            }
        }
    }

    private fun setSendCurrency(ticker: String): Job {
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
                _onDisplayExplanation.postValue(getApplication<PlaidApp>().getString(R.string.swap_could_not_find_crypto_error, ticker))
            }
        }
    }

    private fun setReceiveCurrency(ticker: String) : Job {
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
                _onDisplayExplanation.postValue(getApplication<PlaidApp>().getString(R.string.swap_could_not_find_crypto_error, ticker))
            }
        }
    }

    private fun setMinMax() : Job {
        return GlobalScope.launch {
            val range = apiRepository.getCurrencyRangeLimit(sendTicker, receiveTicker, fixed)
            if (range != null) {
                min = range.min.toDouble()
                max = range.max?.toDouble()
                _minMax.postValue(range.min to (range.max ?: "∞"))
            } else {
                min = 0.0
                max = 0.0
                _minMax.postValue(null)
                _onDisplayExplanation.postValue(getApplication<PlaidApp>().getString(R.string.swap_could_not_load_min_max_error, sendTicker, receiveTicker))
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