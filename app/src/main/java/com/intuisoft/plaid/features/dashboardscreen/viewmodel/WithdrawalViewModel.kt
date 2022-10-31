package com.intuisoft.plaid.features.dashboardscreen.viewmodel

import android.app.Application
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.LiveData
import com.intuisoft.plaid.androidwrappers.SingleLiveData
import com.intuisoft.plaid.androidwrappers.WalletViewModel
import com.intuisoft.plaid.model.BitcoinDisplayUnit
import com.intuisoft.plaid.model.FeeType
import com.intuisoft.plaid.model.NetworkFeeRate
import com.intuisoft.plaid.repositories.LocalStoreRepository
import com.intuisoft.plaid.util.Constants
import com.intuisoft.plaid.util.NetworkUtil
import com.intuisoft.plaid.util.RateConverter
import com.intuisoft.plaid.walletmanager.AbstractWalletManager
import com.intuisoft.plaid.walletmanager.WalletManager
import io.horizontalsystems.bitcoincore.models.TransactionDataSortType
import io.horizontalsystems.bitcoincore.storage.FullTransaction
import io.horizontalsystems.bitcoincore.storage.UnspentOutput


class WithdrawalViewModel(
    application: Application,
    private val localStoreRepository: LocalStoreRepository,
    private val walletManager: AbstractWalletManager
): WalletViewModel(application, localStoreRepository, walletManager) {

    protected val _onLocalSpendAmountUpdated = SingleLiveData<Long>()
    val onLocalAmountUpdated: LiveData<Long> = _onLocalSpendAmountUpdated

    protected val _onConfirm = SingleLiveData<Unit>()
    val onConfirm: LiveData<Unit> = _onConfirm

    protected val _onInvalidAddress = SingleLiveData<Unit>()
    val onInvalidAddress: LiveData<Unit> = _onInvalidAddress

    protected val _onLowPaymetAmount = SingleLiveData<Unit>()
    val onLowPaymetAmount: LiveData<Unit> = _onLowPaymetAmount

    protected val _onSpendFullBalance = SingleLiveData<Unit>()
    val onSpendFullBalance: LiveData<Unit> = _onSpendFullBalance

    protected val _onNotEnoughFunds = SingleLiveData<Unit>()
    val onNotEnoughFunds: LiveData<Unit> = _onNotEnoughFunds

    protected val _somethingWentWrong = SingleLiveData<Unit>()
    val somethingWentWrong: LiveData<Unit> = _somethingWentWrong

    protected val _notEnoughPeers = SingleLiveData<Unit>()
    val notEnoughPeers: LiveData<Unit> = _notEnoughPeers

    protected val _onTransactionCreationFailed = SingleLiveData<Unit>()
    val onTransactionCreationFailed: LiveData<Unit> = _onTransactionCreationFailed

    protected val _onTransactionSent = SingleLiveData<Unit>()
    val onTransactionSent: LiveData<Unit> = _onTransactionSent

    private var localRate: RateConverter = RateConverter(19000.0)
    private var feeRate: Int = 0
    private var address: String? = null
    private var amountCurrencySwap: Boolean = false
    private var networkFeeRate: NetworkFeeRate = NetworkFeeRate(1, 2, 6)
    private var selectedUTXOs: MutableList<UnspentOutput> = mutableListOf()

    fun swapCurrencyAndAmount() = amountCurrencySwap

    fun getFeeRate() = feeRate

    fun getNetworkFeeRate() = networkFeeRate

    fun getLocalRate() = localRate

    fun getSelectedUTXOs() = selectedUTXOs

    fun getLocalAddress() = address

    fun updateUTXOs(context: Context, utxos: MutableList<UnspentOutput>) {
        selectedUTXOs = utxos
        showWalletBalance(context)
    }

    fun setLocalAddress(addr: String) {
        address = addr
    }

    fun getTotalFee(retry: Boolean = true): Long {
        if(selectedUTXOs.isNotEmpty()) {
            return calculateFee(selectedUTXOs, localRate.getRawRate(), feeRate, address, retry)
        } else {
            return calculateFee(localRate.getRawRate(), feeRate, address, retry)
        }
    }

    fun getUnspentOutputs() = localWallet!!.walletKit!!.getUnspentOutputs()

    fun broadcast(context: Context, fullTransaction: FullTransaction): Boolean {
        if(NetworkUtil.hasInternet(context)) {
            if(walletManager.arePeersReady()) {
                localWallet!!.walletKit!!.broadcast(fullTransaction)
                _onTransactionSent.postValue(Unit)
                return true
            } else {

                // this protection ensures that our transaction will most likely propagate to the network successfully
                _notEnoughPeers.postValue(Unit)
                return false
            }
        } else {
            walletManager.arePeersReady()
            Toast.makeText(context, Constants.Strings.NO_INTERNET, Toast.LENGTH_LONG).show()
            return false
        }
    }

    fun setInitialFeeRate() {
        if(localStoreRepository.getDefaultFeeType() == FeeType.LOW) {
            feeRate = networkFeeRate.lowFee
        } else if(localStoreRepository.getDefaultFeeType() == FeeType.MED) {
            feeRate = networkFeeRate.medFee
        } else {
            feeRate = networkFeeRate.highFee
        }
    }


    override fun getWalletBalance() : Long {
        if(selectedUTXOs.isEmpty())
            return localWallet!!.walletKit!!.balance.spendable
        else {
            var balance = 0L
            selectedUTXOs.forEach {
                balance += it.output.value
            }

            return balance
        }
    }

    fun setCurrencyAmountSwap(swap: Boolean) {
        amountCurrencySwap = swap
    }

    fun nextStep() {
        if(!isAddressValid(address ?: "")) {
            _onInvalidAddress.postValue(Unit)
            return
        }
         else {
             val feeResult = getTotalFee()

            if(feeResult == -2L) {
                _onLowPaymetAmount.postValue(Unit)
                return
            }
            else if(feeResult == -1L) {
                _onNotEnoughFunds.postValue(Unit)
                return
            }
            else if(feeResult == 0L) {
                _somethingWentWrong.postValue(Unit)
                return
            }
        }

        val feeResult = getTotalFee(false)
        if(feeResult == -1L) {
            _onSpendFullBalance.postValue(Unit)
        } else {
            _onConfirm.postValue(Unit)
        }
    }

    fun createTransaction() : FullTransaction? {
        try {
            if(selectedUTXOs.isNotEmpty()) {
                return localWallet!!.walletKit!!.redeem(
                    unspentOutputs = selectedUTXOs,
                    value = localRate.getRawRate(),
                    address = address!!,
                    feeRate = feeRate,
                    sortType = TransactionDataSortType.Bip69,
                    createOnly = true
                )
            } else {
                return localWallet!!.walletKit!!.send(
                    address = address!!,
                    value = localRate.getRawRate(),
                    senderPay = true,
                    feeRate = feeRate,
                    sortType = TransactionDataSortType.Bip69,
                    pluginData = mapOf(),
                    createOnly = true
                )
            }
        } catch(e: Exception) {
            _onTransactionCreationFailed.postValue(Unit)
            return null
        }
    }

    fun setLocalSpendAmount(amount: Double, type: RateConverter.RateType) {
        localRate.setLocalRate(type, amount)
        _onLocalSpendAmountUpdated.postValue(localRate.getRawRate())
    }

    fun adjustLocalSpendToFitFee() {
        if(selectedUTXOs.isNotEmpty()) {
            localRate.setLocalRate(RateConverter.RateType.SATOSHI_RATE, localWallet!!.walletKit!!.maximumSpendableValue(selectedUTXOs, address, feeRate).toDouble())
        } else {
            localRate.setLocalRate(RateConverter.RateType.SATOSHI_RATE, localWallet!!.walletKit!!.maximumSpendableValue(address, feeRate).toDouble())
        }
    }

    fun setFeeRate(rate: Int) {
        if(rate <= 0) {
            feeRate = 1
        } else feeRate = rate
        _onLocalSpendAmountUpdated.postValue(localRate.getRawRate())
    }

    fun getCurrentRateConversion(): RateConverter.RateType {
        if(amountCurrencySwap) {
            return RateConverter.RateType.FIAT_RATE
        } else if(localStoreRepository.getBitcoinDisplayUnit() == BitcoinDisplayUnit.SATS) {
            return RateConverter.RateType.SATOSHI_RATE
        } else {
            return RateConverter.RateType.BTC_RATE
        }
    }

    fun validateSendAmount(amountStr: String) : Boolean {
        if(amountStr.count { it == '.' } > 1)
            return false
        else if(localStoreRepository.getBitcoinDisplayUnit() == BitcoinDisplayUnit.SATS
            && !amountCurrencySwap && amountStr.find { it == '.' } != null) {
            return false
        }

        val clone = localRate.clone()
        clone.setLocalRate(getCurrentRateConversion(), amountStr.toDouble())

        if(clone.getRawRate() <= getWalletBalance()) {
            setLocalSpendAmount(clone.getRawRate().toDouble(), RateConverter.RateType.SATOSHI_RATE)
            return true
        } else {
            return false
        }
    }
}