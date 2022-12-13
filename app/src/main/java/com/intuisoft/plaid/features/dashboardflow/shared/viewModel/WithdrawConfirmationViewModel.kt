package com.intuisoft.plaid.features.dashboardflow.shared.viewModel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.intuisoft.plaid.PlaidApp
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.SingleLiveData
import com.intuisoft.plaid.androidwrappers.WalletViewModel
import com.intuisoft.plaid.common.model.FeeType
import com.intuisoft.plaid.common.model.NetworkFeeRate
import com.intuisoft.plaid.common.repositories.ApiRepository
import com.intuisoft.plaid.common.repositories.LocalStoreRepository
import com.intuisoft.plaid.util.NetworkUtil
import com.intuisoft.plaid.common.util.RateConverter
import com.intuisoft.plaid.walletmanager.AbstractWalletManager
import io.horizontalsystems.bitcoincore.models.TransactionDataSortType
import io.horizontalsystems.bitcoincore.storage.FullTransaction
import io.horizontalsystems.bitcoinkit.BitcoinKit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class WithdrawConfirmationViewModel(
    application: Application,
    apiRepository: ApiRepository,
    private val localStoreRepository: LocalStoreRepository,
    private val walletManager: AbstractWalletManager
): WalletViewModel(application, localStoreRepository, apiRepository, walletManager) {

    protected val _onInputRejected = SingleLiveData<Unit>()
    val onInputRejected: LiveData<Unit> = _onInputRejected

    protected val _onSpendFullBalance = SingleLiveData<Unit>()
    val onSpendFullBalance: LiveData<Unit> = _onSpendFullBalance

    protected val _onAnimateSentAmount = SingleLiveData<String>()
    val onAnimateSentAmount: LiveData<String> = _onAnimateSentAmount

    protected val _onDisplayExplanation = SingleLiveData<String>()
    val onDisplayExplanation: LiveData<String> = _onDisplayExplanation

    protected val _onNetworkError = SingleLiveData<String>()
    val onNetworkError: LiveData<String> = _onNetworkError

    protected val _onConfirm = SingleLiveData<Unit>()
    val onConfirm: LiveData<Unit> = _onConfirm

    private var invoiceSend: Boolean = false
    private var feeRate: Int = 0
    private var address: String? = null
    private var invalidAddressErrors = 0
    private var amountToSpend: RateConverter = RateConverter(localStoreRepository.getRateFor(localStoreRepository.getLocalCurrency())?.currentPrice ?: 0.0)
    private var networkFeeRate: NetworkFeeRate = NetworkFeeRate(1, 2, 6)

    fun getFeeRate() = feeRate

    fun getSpendAmount() = amountToSpend

    fun getNetworkFeeRate() = networkFeeRate

    fun getLocalAddress() = address

    fun isInvoiceSend() = invoiceSend

    fun setInvoiceSend(fromInvoice: Boolean) {
        invoiceSend = fromInvoice
    }

    fun setNetworkFeeRate() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val suggestedRates = getSuggestedFees(
                    this@WithdrawConfirmationViewModel.getWalletNetwork() == BitcoinKit.NetworkType.TestNet
                )

                suggestedRates?.let {
                    this@WithdrawConfirmationViewModel.networkFeeRate = it
                }

                setDefaultFeeRate()
            }
        }
    }

    fun animateSentAmount(period: Long) {
        if(period < 100) throw IllegalStateException("period cannot be less than 100")
        val multiplier: Long
        val sleepTime = period / 120

        if(amountToSpend.getRawRate() < 100) {
            multiplier = 1
        } else {
            multiplier = amountToSpend.getRawRate() / 100
        }

        viewModelScope.launch {
            var temp: RateConverter = amountToSpend.clone()
            temp.setLocalRate(RateConverter.RateType.SATOSHI_RATE, 0.0)

            var count = 1
            while(count < 99) {
                _onAnimateSentAmount.postValue(temp.from(getDisplayUnit().toRateType(),
                    localStoreRepository.getLocalCurrency(), false).second!!)

                temp.setLocalRate(RateConverter.RateType.SATOSHI_RATE, temp.getRawRate() + multiplier.toDouble())
                delay(sleepTime)
                count++
            }

            _onAnimateSentAmount.postValue(amountToSpend.from(getDisplayUnit().toRateType(),
                localStoreRepository.getLocalCurrency(), false).second!!)
        }
    }

    private fun setDefaultFeeRate() {
        when(localStoreRepository.getDefaultFeeType()) {
            FeeType.LOW -> {
                feeRate = networkFeeRate.lowFee
            }

            FeeType.MED -> {
                feeRate = networkFeeRate.medFee
            }

            FeeType.HIGH -> {
                feeRate = networkFeeRate.highFee
            }
        }
    }

    fun broadcast(fullTransaction: FullTransaction): Boolean {
        if(NetworkUtil.hasInternet(getApplication<PlaidApp>())) {
            if(localWallet!!.walletKit!!.canSendTransaction())
                localWallet!!.walletKit!!.broadcast(fullTransaction)
            else {
                walletManager.synchronize(localWallet!!)
                _onNetworkError.postValue(getApplication<PlaidApp>().getString(R.string.reconnecting_to_core))
                return false
            }

            return true
        } else {
            _onNetworkError.postValue(getApplication<PlaidApp>().getString(R.string.no_internet_connection))
            return false
        }
    }

    private fun invalidAddressError() {
        _onInputRejected.postValue(Unit)
        invalidAddressErrors++

        if(invalidAddressErrors % errorThreshold == 0) {
            if(localWallet!!.testNetWallet && walletManager.getBaseWallet(mainNet = true).isAddressValid(address ?: "")) {
                _onDisplayExplanation.postValue(getApplication<PlaidApp>().getString(R.string.withdraw_confirmation_error_invalid_address_test_net))
            } else if(!localWallet!!.testNetWallet && walletManager.getBaseWallet(mainNet = false).isAddressValid(address ?: "")) {
                _onDisplayExplanation.postValue(getApplication<PlaidApp>().getString(R.string.withdraw_confirmation_error_invalid_address_main_net))
            } else {
                _onDisplayExplanation.postValue(getApplication<PlaidApp>().getString(R.string.withdraw_confirmation_error_invalid_address))
            }
        }
    }

    fun nextStep() {
        if(!isAddressValid(address ?: "")) {
            invalidAddressError()
            return
        }
        else {
            val result = getTotalFee()

            when (result) {
                -1L -> { // not enough funds
                    _onDisplayExplanation.postValue(getApplication<PlaidApp>().getString(R.string.withdraw_error_fee_too_high))
                }

                -2L -> { // low payment amount
                    _onDisplayExplanation.postValue(getApplication<PlaidApp>().getString(R.string.withdraw_error_low_payment_amount))
                }

                0L -> { // something went wrong
                    _onDisplayExplanation.postValue(getApplication<PlaidApp>().getString(R.string.withdraw_error_something_went_wrong))
                }

                else ->  { // good
                    val feeResult = getTotalFee(false)
                    if(feeResult == -1L) {
                        _onSpendFullBalance.postValue(Unit)
                    } else {
                        _onConfirm.postValue(Unit)
                    }
                }
            }
        }
    }

    fun getTotalFee(retry: Boolean = true): Long {
        if(selectedUTXOs.isNotEmpty()) {
            return calculateFee(selectedUTXOs, amountToSpend.getRawRate(), feeRate, address, retry)
        } else {
            return calculateFee(amountToSpend.getRawRate(), feeRate, address, retry)
        }
    }

    fun setFeeRate(rate: Int) {
        if(rate <= 0) {
            feeRate = 1
        } else feeRate = rate
    }

    private fun adjustLocalSpendToFitFee(spend: RateConverter) {
        if(selectedUTXOs.isNotEmpty()) {
            spend.setLocalRate(RateConverter.RateType.SATOSHI_RATE, localWallet!!.walletKit!!.maximumSpendableValue(selectedUTXOs, address, feeRate).toDouble())
        } else {
            spend.setLocalRate(RateConverter.RateType.SATOSHI_RATE, localWallet!!.walletKit!!.maximumSpendableValue(address, feeRate).toDouble())
        }
    }

    fun createTransaction(fullSpend: Boolean) : Pair<RateConverter, FullTransaction>? {
        try {
            val spend = amountToSpend.clone()
            if(fullSpend) adjustLocalSpendToFitFee(spend)

            if(selectedUTXOs.isNotEmpty()) {
                return spend to localWallet!!.walletKit!!.redeem(
                    unspentOutputs = selectedUTXOs,
                    value = spend.getRawRate(),
                    address = address!!,
                    feeRate = feeRate,
                    sortType = TransactionDataSortType.Shuffle,
                    createOnly = true
                )
            } else {
                return spend to localWallet!!.walletKit!!.redeem(
                    unspentOutputs = getUnspentOutputs(),
                    value = spend.getRawRate(),
                    address = address!!,
                    feeRate = feeRate,
                    sortType = TransactionDataSortType.Shuffle,
                    createOnly = true
                )
            }
        } catch(e: Exception) {
            _onDisplayExplanation.postValue(getApplication<PlaidApp>().getString(R.string.withdraw_confirmation_create_transaction_error))
            return null
        }
    }

    fun updateUTXOAddresses(utxos: MutableList<String>) {
        selectedUTXOs = getUnspentOutputs().filter { utxos.find { utxo -> it.output.address!! == utxo } != null }.toMutableList()
    }

    fun updateSendAmount(amount: Long) {
        amountToSpend.setLocalRate(RateConverter.RateType.SATOSHI_RATE, amount.toDouble())
    }

    fun setLocalAddress(addr: String) {
        address = addr
    }

    companion object {
        private const val errorThreshold = 5
    }
}