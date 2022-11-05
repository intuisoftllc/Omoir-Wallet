package com.intuisoft.plaid.features.dashboardscreen.viewmodel

import android.app.Application
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.intuisoft.plaid.PlaidApp
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.SingleLiveData
import com.intuisoft.plaid.androidwrappers.WalletViewModel
import com.intuisoft.plaid.model.BitcoinDisplayUnit
import com.intuisoft.plaid.model.FeeType
import com.intuisoft.plaid.model.LocalWalletModel
import com.intuisoft.plaid.model.NetworkFeeRate
import com.intuisoft.plaid.repositories.LocalStoreRepository
import com.intuisoft.plaid.util.Constants
import com.intuisoft.plaid.util.NetworkUtil
import com.intuisoft.plaid.util.RateConverter
import com.intuisoft.plaid.util.entensions.addChars
import com.intuisoft.plaid.util.entensions.charsAfter
import com.intuisoft.plaid.walletmanager.AbstractWalletManager
import io.horizontalsystems.bitcoincore.models.TransactionDataSortType
import io.horizontalsystems.bitcoincore.storage.FullTransaction
import io.horizontalsystems.bitcoincore.storage.UnspentOutput
import io.horizontalsystems.bitcoinkit.BitcoinKit
import io.horizontalsystems.hdwalletkit.HDExtendedKey
import io.horizontalsystems.hdwalletkit.HDExtendedKeyVersion
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class WithdrawConfirmationViewModel(
    application: Application,
    private val localStoreRepository: LocalStoreRepository,
    private val walletManager: AbstractWalletManager
): WalletViewModel(application, localStoreRepository, walletManager) {

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

    private var feeRate: Int = 0
    private var address: String? = null
    private var invalidAddressErrors = 0
    private var selectedUTXOs: MutableList<String> = mutableListOf()
    private var amountToSpend: RateConverter = RateConverter(19000.0)
    private var networkFeeRate: NetworkFeeRate = NetworkFeeRate(1, 2, 6)

    fun getFeeRate() = feeRate

    fun getSpendAmount() = amountToSpend

    fun getNetworkFeeRate() = networkFeeRate

    fun getLocalAddress() = address

    fun getWallets() = walletManager.getWallets()

    fun canTransferToWallet(recepient: LocalWalletModel): Boolean {
        return recepient.testNetWallet == localWallet!!.testNetWallet
                && recepient.uuid != localWallet!!.uuid // ensure same network transferability
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
                _onAnimateSentAmount.postValue(temp.from(getDisplayUnit().toRateType(), false).second!!)

                temp.setLocalRate(RateConverter.RateType.SATOSHI_RATE, temp.getRawRate() + multiplier.toDouble())
                delay(sleepTime)
                count++
            }

            _onAnimateSentAmount.postValue(amountToSpend.from(getDisplayUnit().toRateType(), false).second!!)
        }
    }

    fun setDefaultFeeRate() {
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
            if(walletManager.arePeersReady()) {
                localWallet!!.walletKit!!.broadcast(fullTransaction)
                return true
            } else {
                walletManager.synchronizeAll()
                // this protection ensures that our transaction will most likely propagate to the network successfully
                _onNetworkError.postValue(getApplication<PlaidApp>().getString(R.string.reconnecting_to_core))
                return false
            }
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
            return calculateFee(utxoToUnspentOutput(), amountToSpend.getRawRate(), feeRate, address, retry)
        } else {
            return calculateFee(amountToSpend.getRawRate(), feeRate, address, retry)
        }
    }

    fun setFeeRate(rate: Int) {
        if(rate <= 0) {
            feeRate = 1
        } else feeRate = rate
    }

    fun adjustLocalSpendToFitFee() {
        if(selectedUTXOs.isNotEmpty()) {
            amountToSpend.setLocalRate(RateConverter.RateType.SATOSHI_RATE, localWallet!!.walletKit!!.maximumSpendableValue(utxoToUnspentOutput(), address, feeRate).toDouble())
        } else {
            amountToSpend.setLocalRate(RateConverter.RateType.SATOSHI_RATE, localWallet!!.walletKit!!.maximumSpendableValue(address, feeRate).toDouble())
        }
    }

    fun createTransaction() : FullTransaction? {
        try {
            if(selectedUTXOs.isNotEmpty()) {
                return localWallet!!.walletKit!!.redeem(
                    unspentOutputs = utxoToUnspentOutput(),
                    value = amountToSpend.getRawRate(),
                    address = address!!,
                    feeRate = feeRate,
                    sortType = TransactionDataSortType.Shuffle,
                    createOnly = true
                )
            } else {
                return localWallet!!.walletKit!!.send(
                    address = address!!,
                    value = amountToSpend.getRawRate(),
                    senderPay = true,
                    feeRate = feeRate,
                    sortType = TransactionDataSortType.Shuffle,
                    pluginData = mapOf(),
                    createOnly = true
                )
            }
        } catch(e: Exception) {
            _onDisplayExplanation.postValue(getApplication<PlaidApp>().getString(R.string.withdraw_confirmation_create_transaction_error))
            return null
        }
    }

    fun getUnspentOutputs() = localWallet!!.walletKit!!.getUnspentOutputs()

    fun utxoToUnspentOutput() : List<UnspentOutput> {
        return getUnspentOutputs().filter { selectedUTXOs.find { utxo -> it.output.address!! == utxo } != null }
    }

    fun updateUTXOs(utxos: MutableList<String>) {
        selectedUTXOs = utxos
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