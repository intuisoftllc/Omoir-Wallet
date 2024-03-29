package com.intuisoft.plaid.features.dashboardflow.shared.viewModel

import android.app.Application
import androidx.lifecycle.LiveData
import com.intuisoft.plaid.OmoirApp
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.SingleLiveData
import com.intuisoft.plaid.androidwrappers.WalletViewModel
import com.intuisoft.plaid.common.model.BitcoinDisplayUnit
import com.intuisoft.plaid.common.repositories.ApiRepository
import com.intuisoft.plaid.common.repositories.LocalStoreRepository
import com.intuisoft.plaid.common.util.RateConverter
import com.intuisoft.plaid.common.util.SimpleCurrencyFormat
import com.intuisoft.plaid.common.util.extensions.addChars
import com.intuisoft.plaid.common.util.extensions.charsAfter
import com.intuisoft.plaid.walletmanager.AbstractWalletManager
import io.horizontalsystems.bitcoincore.storage.UnspentOutput


class WithdrawalViewModel(
    application: Application,
    apiRepository: ApiRepository,
    private val localStoreRepository: LocalStoreRepository,
    private val walletManager: AbstractWalletManager
): WalletViewModel(application, localStoreRepository, apiRepository, walletManager) {

    protected val _localSpendAmount = SingleLiveData<String>()
    val localSpendAmount: LiveData<String> = _localSpendAmount

    protected val _maximumSpend = SingleLiveData<String>()
    val maximumSpend: LiveData<String> = _maximumSpend

    protected val _shouldAdvance = SingleLiveData<Boolean>()
    val shouldAdvance: LiveData<Boolean> = _shouldAdvance

    protected val _onInputRejected = SingleLiveData<Unit>()
    val onInputRejected: LiveData<Unit> = _onInputRejected

    protected val _onDisplayExplanation = SingleLiveData<String>()
    val onDisplayExplanation: LiveData<String> = _onDisplayExplanation

    protected val _onNextStep = SingleLiveData<Unit>()
    val onNextStep: LiveData<Unit> = _onNextStep

    private var amountToSpend: RateConverter = RateConverter(localStoreRepository.getRateFor(localStoreRepository.getLocalCurrency())?.currentPrice ?: 0.0)
    private var internalAmountString: String = "0"
    private var overBalanceErrors = 0
    private var maxDecimalErrors = 0
    private var decimalPlaceNotAllowedErrors = 0
    private var decimalPlace = 0
    private var decimalEntry = false

    private fun countFloatingZeros() : Int {

        var zeros = 0
        internalAmountString.reversed().forEach {
            if(it == '0') {
                zeros++
            } else return zeros
        }

        return zeros
    }

    fun displaySpendAmount() {
        val spend = amountToSpend.from(getDisplayUnit().toRateType(),
            localStoreRepository.getLocalCurrency(),false)

        if(getDisplayUnit().toRateType() == RateConverter.RateType.FIAT_RATE) {
            if(decimalPlace == 0 && !decimalEntry) {
                _localSpendAmount.postValue(spend.second!!.replace(".00", ""))
            } else if(decimalPlace == 0 && decimalEntry) {
                _localSpendAmount.postValue(spend.second!!.replace(".00", "."))
            } else if(decimalPlace == 1 && internalAmountString.find { it == '.'} != null) {
                _localSpendAmount.postValue(spend.second!!.replace(".${internalAmountString.last()}0", ".${internalAmountString.last()}"))
            } else {
                _localSpendAmount.postValue(spend.second!!)
            }
        } else if((countFloatingZeros() > 0 && internalAmountString.find { it == '.'} != null)
            || (decimalEntry && countFloatingZeros() == 0)) {
            var finalAmount = if(getDisplayUnit().toRateType() != RateConverter.RateType.FIAT_RATE) spend.first else spend.second
            if(spend.first.find { it == '.' } == null) {
                finalAmount += ".".addChars('0', countFloatingZeros())
            } else {
                finalAmount += "".addChars('0', countFloatingZeros())
            }

            _localSpendAmount.postValue(RateConverter.prefixPostfixValue(finalAmount, getDisplayUnit().toRateType()))
        } else
            _localSpendAmount.postValue(spend.second!!)
        _shouldAdvance.postValue(amountToSpend.getRawRate() > 0L)
    }

    fun displayTotalBalance() {
        if(selectedUTXOs.isEmpty()) {
            _maximumSpend.postValue(
                getMaxSpend().from(
                    getDisplayUnit().toRateType(),
                    localStoreRepository.getLocalCurrency(),
                    false
                ).second!!
            )
        } else if(selectedUTXOs.size == 1) {
            _maximumSpend.postValue(
                getApplication<OmoirApp>().getString(R.string.withdraw_selected_single_utxo_amount,
                    getMaxSpend().from(
                        getDisplayUnit().toRateType(),
                        localStoreRepository.getLocalCurrency(),
                        false
                    ).second!!
                )
            )
        }
        else {
            _maximumSpend.postValue(
                getApplication<OmoirApp>().getString(R.string.withdraw_selected_utxo_amount,
                    getMaxSpend().from(
                        getDisplayUnit().toRateType(),
                        localStoreRepository.getLocalCurrency(),
                        false
                    ).second!!,
                    selectedUTXOs.size.toString()
                )
            )
        }
    }

    fun spendMaxBalance() {
        amountToSpend.copyRate(getMaxSpend())
        setInternalAmount()
        displaySpendAmount()
    }

    fun getSatsToSpend() = amountToSpend.getRawRate()

    fun activateDecimalEntry() {
        if(!decimalEntry && getDisplayUnit() != BitcoinDisplayUnit.SATS) {
            decimalEntry = true
            decimalPlace = 0
            if(internalAmountString.isEmpty())
                internalAmountString = "0"

            internalAmountString += "."
        } else if(getDisplayUnit() == BitcoinDisplayUnit.SATS) {
            _onInputRejected.postValue(Unit)
            decimalPlaceNotAllowedErrors++

            if(decimalPlaceNotAllowedErrors % errorThreshold == 0) {
                _onDisplayExplanation.postValue(getApplication<OmoirApp>().getString(R.string.withdraw_error_decimal_not_allowed))
            }
        }

        displaySpendAmount()
    }

    private fun setInternalAmount() {
        if(amountToSpend.getRawRate() != 0L) {
            internalAmountString = amountToSpend.from(
                getDisplayUnit().toRateType(),
                localStoreRepository.getLocalCurrency(), false
            ).first!!
        } else {
            internalAmountString = "0"
        }

        when(getDisplayUnit()) {
            BitcoinDisplayUnit.SATS -> {
                decimalEntry = false
            }

            BitcoinDisplayUnit.BTC,
            BitcoinDisplayUnit.FIAT -> {
                var charsAfter = internalAmountString.charsAfter('.')

                if(charsAfter > 0 && amountToSpend.getRawRate() != 0L) {
                    decimalEntry = true
                    decimalPlace = charsAfter
                } else decimalEntry = false
            }
        }
    }

    fun changeDisplayUnit(displayUnit: BitcoinDisplayUnit) {
        setDisplayUnit(displayUnit)
        setInternalAmount()
    }

    private fun increaseDecimalBasedNumber(
        number: Int,
        maxDecimals: Int
    ) {
        if(!decimalEntry) {
            if(number == 0 && (internalAmountString.isEmpty()
                        || normalizeInternalAmount().toDouble() == 0.0))
                return

            internalAmountString += number
        } else if((number == 0 && decimalPlace < maxDecimals) || decimalPlace < maxDecimals) {
            decimalPlace++
            internalAmountString += number
        } else if(decimalPlace == maxDecimals) {
            maxDecimalsError()
        }
        else if(number != 0 || (number == 0 && amountToSpend.getRawRate() > 0)) {
            overBalanceError()
        }
    }

    fun normalizeInternalAmount(): String {
        return internalAmountString.replace(",", "").replace(SimpleCurrencyFormat.getSymbol(localStoreRepository.getLocalCurrency()), "")
    }

    fun decreaseBy(singleNum: Boolean) {
        if(decimalPlace > 0) decimalPlace--

        if(singleNum && internalAmountString.length > 1) {
            internalAmountString = internalAmountString.dropLast(1)
        } else {
            internalAmountString = ""
        }

        if(internalAmountString.isEmpty()) {
            amountToSpend.setLocalRate(RateConverter.RateType.SATOSHI_RATE, 0.0)
        }
        else {
            amountToSpend.setLocalRate(getDisplayUnit().toRateType(), normalizeInternalAmount().toDouble())
        }

        if(decimalPlace == 0 && decimalEntry) {
            decimalEntry = false

            if(internalAmountString.endsWith("."))
                internalAmountString = internalAmountString.dropLast(1) // drop the .
        }

        if(amountToSpend.getRawRate() == 0L && decimalEntry) {
            decimalEntry = false
            decimalPlace = 0
            internalAmountString = ""
        }

        internalAmountString = normalizeInternalAmount()
        displaySpendAmount()
    }

    override fun setInitialSpendUtxo(utxo: String) {
        super.setInitialSpendUtxo(utxo)
        displayTotalBalance()
    }

    override fun updateUTXOs(utxos: MutableList<UnspentOutput>) {
        super.updateUTXOs(utxos)
        displayTotalBalance()
    }

    override fun addSingleUTXO(utxo: UnspentOutput) {
        super.addSingleUTXO(utxo)
        displayTotalBalance()
    }

    private fun overBalanceError() {
        _onInputRejected.postValue(Unit)
        overBalanceErrors++

        if(overBalanceErrors % errorThreshold == 0) {
            _onDisplayExplanation.postValue(getApplication<OmoirApp>().getString(R.string.withdraw_error_over_balance))
        }
    }

    private fun maxDecimalsError() {
        _onInputRejected.postValue(Unit)
        maxDecimalErrors++

        if(maxDecimalErrors % errorThreshold == 0) {
            _onDisplayExplanation.postValue(getApplication<OmoirApp>().getString(R.string.withdraw_error_max_decimals))
        }
    }

    fun increaseBy(number: Int) {
        if(number == 0 && internalAmountString.isEmpty() && decimalPlace == -1)
            return

        when(getDisplayUnit()) {
            BitcoinDisplayUnit.SATS -> {
                if(number == 0 && (internalAmountString.isEmpty() || (normalizeInternalAmount().toDouble() != 0.0)))
                    internalAmountString += "0"
                else if(number != 0)
                    internalAmountString += number
            }

            BitcoinDisplayUnit.BTC -> {
                increaseDecimalBasedNumber(
                    number = number,
                    maxDecimals = 8
                )
            }

            BitcoinDisplayUnit.FIAT -> {
                increaseDecimalBasedNumber(
                    number = number,
                    maxDecimals = 2
                )
            }
        }

        val tempRate = RateConverter(amountToSpend.getFiatRate())
        if(internalAmountString.isEmpty()) {
            tempRate.setLocalRate(RateConverter.RateType.SATOSHI_RATE, 0.0)
        }
        else {
            tempRate.setLocalRate(getDisplayUnit().toRateType(), normalizeInternalAmount().toDouble())
        }

        if(getDisplayUnit() == BitcoinDisplayUnit.FIAT &&
            (getMaxSpend().from(RateConverter.RateType.FIAT_RATE, localStoreRepository.getLocalCurrency()).first ==
                    tempRate.from(RateConverter.RateType.FIAT_RATE, localStoreRepository.getLocalCurrency()).first)) {
            // this solves a rounding issue for when a users balance causes entering the full balance to be slightly more than they have
            spendMaxBalance()
        } else if(!isSpendOverBalance(tempRate)) {
            amountToSpend.copyRate(tempRate)
            displaySpendAmount()
        }
    }

    private fun isSpendOverBalance(converter: RateConverter) : Boolean {
        if(converter.getRawRate() <= getMaxSpend().getRawRate()) {
            return false
        } else {
            if(internalAmountString.isNotEmpty()) {
                internalAmountString = internalAmountString.dropLast(1)
                if(decimalPlace > 0) decimalPlace--
            }
            overBalanceError()
            return true
        }
    }

    fun onNextStep() {
        if(!isSpendOverBalance(amountToSpend)) {
            val result: Long

            if(selectedUTXOs.isNotEmpty()) {
                result = calculateFee(selectedUTXOs, amountToSpend.getRawRate(), 1, null, true)
            } else {
                result = calculateFee(getUnspentOutputs(), amountToSpend.getRawRate(), 1, null, true)
            }

            when(result) {
                -1L  -> { // not enough funds
                    if(selectedUTXOs.isNotEmpty() && selectedUTXOs.size != getUnspentOutputs().size)
                        _onDisplayExplanation.postValue(getApplication<OmoirApp>().getString(R.string.withdraw_error_not_enough_funds_with_selected_utxos))
                    else
                        _onDisplayExplanation.postValue(getApplication<OmoirApp>().getString(R.string.withdraw_error_not_enough_funds))
                }

                -2L -> { // low payment amount
                    _onDisplayExplanation.postValue(getApplication<OmoirApp>().getString(R.string.withdraw_error_low_payment_amount))
                }

                0L -> { // something went wrong
                    _onDisplayExplanation.postValue(getApplication<OmoirApp>().getString(R.string.withdraw_error_something_went_wrong))
                }

                else ->  { // good
                    _onNextStep.postValue(Unit)
                }
            }
        }
    }

    companion object {
        private const val errorThreshold = 5
    }
}