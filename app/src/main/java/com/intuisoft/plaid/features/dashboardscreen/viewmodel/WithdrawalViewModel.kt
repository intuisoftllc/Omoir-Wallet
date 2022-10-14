package com.intuisoft.plaid.features.dashboardscreen.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import com.intuisoft.plaid.androidwrappers.SingleLiveData
import com.intuisoft.plaid.androidwrappers.WalletViewModel
import com.intuisoft.plaid.model.BitcoinDisplayUnit
import com.intuisoft.plaid.model.FeeType
import com.intuisoft.plaid.model.NetworkFeeRate
import com.intuisoft.plaid.repositories.LocalStoreRepository
import com.intuisoft.plaid.util.Constants
import com.intuisoft.plaid.util.RateConverter
import com.intuisoft.plaid.walletmanager.WalletManager


class WithdrawalViewModel(
    application: Application,
    private val localStoreRepository: LocalStoreRepository,
    private val walletManager: WalletManager
): WalletViewModel(application, localStoreRepository, walletManager) {

    protected val _onLocalSpendAmountUpdated = SingleLiveData<Long>()
    val onLocalAmountUpdated: LiveData<Long> = _onLocalSpendAmountUpdated

    protected val _onNextStep = SingleLiveData<Unit>()
    val onNextStep: LiveData<Unit> = _onNextStep

    protected val _onInvalidAddress = SingleLiveData<Unit>()
    val onInvalidAddress: LiveData<Unit> = _onInvalidAddress

    protected val _onLowPaymetAmount = SingleLiveData<Unit>()
    val onLowPaymetAmount: LiveData<Unit> = _onLowPaymetAmount

    protected val _onSpendFullBalance = SingleLiveData<Unit>()
    val onSpendFullBalance: LiveData<Unit> = _onSpendFullBalance

    private var localRate: RateConverter = RateConverter(19000.0)
    private var feeRate: Int = 0
    private var address: String? = null
    private var amountCurrencySwap: Boolean = false
    private var networkFeeRate: NetworkFeeRate = NetworkFeeRate(1, 2, 6)

    fun swapCurrencyAndAmount() = amountCurrencySwap

    fun getFeeRate() = feeRate

    fun getNetworkFeeRate() = networkFeeRate

    fun getLocalRate() = localRate

    fun setLocalAddress(addr: String) {
        address = addr
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

    fun setCurrencyAmountSwap(swap: Boolean) {
        amountCurrencySwap = swap
    }

    fun nextStep() {
        if(!isAddressValid(address ?: "")) {
            _onInvalidAddress.postValue(Unit)
            return
        }
         else {
             val feeResult = calculateFee(localRate.getRawRate(), feeRate, address)

            if(feeResult == 0L) {
                _onLowPaymetAmount.postValue(Unit)
                return
            }
            else if(feeResult == -1L) {
                _onSpendFullBalance.postValue(Unit)
                return
            }
        }

        _onNextStep.postValue(Unit)
    }

    fun setLocalSpendAmount(amount: Double, type: RateConverter.RateType) {
        localRate.setLocalRate(type, amount)
        _onLocalSpendAmountUpdated.postValue(localRate.getRawRate())
    }

    fun setFeeRate(rate: Int) {
        feeRate = rate
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