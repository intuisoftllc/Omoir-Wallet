package com.intuisoft.plaid.features.dashboardflow.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import com.intuisoft.plaid.PlaidApp
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.SingleLiveData
import com.intuisoft.plaid.androidwrappers.WalletViewModel
import com.intuisoft.plaid.common.model.BitcoinDisplayUnit
import com.intuisoft.plaid.common.repositories.ApiRepository
import com.intuisoft.plaid.common.repositories.LocalStoreRepository
import com.intuisoft.plaid.common.util.RateConverter
import com.intuisoft.plaid.common.util.SimpleCurrencyFormat
import com.intuisoft.plaid.util.entensions.addChars
import com.intuisoft.plaid.util.entensions.charsAfter
import com.intuisoft.plaid.walletmanager.AbstractWalletManager
import io.horizontalsystems.bitcoincore.models.BitcoinPaymentData
import io.horizontalsystems.bitcoincore.storage.UnspentOutput


class InvoiceViewModel(
    application: Application,
    apiRepository: ApiRepository,
    private val localStoreRepository: LocalStoreRepository,
    private val walletManager: AbstractWalletManager
): WalletViewModel(application, localStoreRepository, apiRepository, walletManager) {

    protected val _invoiceLoaded = SingleLiveData<InvoiceDetails>()
    val invoiceLoaded: LiveData<InvoiceDetails> = _invoiceLoaded

    protected val _enableNext = SingleLiveData<Unit>()
    val enableNext: LiveData<Unit> = _enableNext

    protected val _onDisplayExplanation = SingleLiveData<String>()
    val onDisplayExplanation: LiveData<String> = _onDisplayExplanation

    protected val _onNextStep = SingleLiveData<InvoiceDetails>()
    val onNextStep: LiveData<InvoiceDetails> = _onNextStep

    protected val _onAvailableBalanceUpdated = SingleLiveData<String>()
    val onAvailableBalanceUpdated: LiveData<String> = _onAvailableBalanceUpdated

    private var selectedUTXOs: MutableList<UnspentOutput> = mutableListOf()
    private var amountToSpend: RateConverter = RateConverter(localStoreRepository.getRateFor(localStoreRepository.getLocalCurrency())?.currentPrice ?: 0.0)
    private var description: String = ""
    private var address: String = ""

    fun getSatsToSpend() = amountToSpend.getRawRate()

    fun loadInvoice(invoiceData: String) {
        val invoice = walletManager.parseInvoice(invoiceData)
        amountToSpend.setLocalRate(RateConverter.RateType.BTC_RATE, invoice.amount ?: 0.0)
        description = invoice.label ?: ""
        address = invoice.address

        _invoiceLoaded.postValue(
            InvoiceDetails(
                amountToSpend.from(localStoreRepository.getBitcoinDisplayUnit().toRateType(), localStoreRepository.getLocalCurrency(), false).second,
                address,
                if(description.isNotEmpty() && description.isNotBlank())
                    description
                else getApplication<PlaidApp>().getString(R.string.not_applicable)
            )
        )
        _enableNext.postValue(Unit)
    }

    fun updateAvailableBalance() {
        _onAvailableBalanceUpdated.postValue(
            getMaxSpend().from(localStoreRepository.getBitcoinDisplayUnit().toRateType(), localStoreRepository.getLocalCurrency(), false).second!!
        )
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

    fun getMaxSpend() : RateConverter {
        val rate = RateConverter(amountToSpend.getFiatRate())
        rate.setLocalRate(RateConverter.RateType.SATOSHI_RATE, getWalletBalance().toDouble())
        return rate
    }

    private fun isSpendOverBalance(converter: RateConverter) : Boolean {
        if(converter.getRawRate() <= getMaxSpend().getRawRate()) {
            return false
        } else {
            overBalanceError()
            return true
        }
    }

    private fun overBalanceError() {
        _onDisplayExplanation.postValue(getApplication<PlaidApp>().getString(R.string.invoice_error_over_balance))
    }

    fun onNextStep() {
        if(!isAddressValid(address)) {
            invalidAddressError()
            return
        } else {
            if (!isSpendOverBalance(amountToSpend)) {
                val result: Long

                if (selectedUTXOs.isNotEmpty()) {
                    result = calculateFee(selectedUTXOs, amountToSpend.getRawRate(), 1, null, true)
                } else {
                    result = calculateFee(amountToSpend.getRawRate(), 1, null, true)
                }

                when (result) {
                    -1L -> { // not enough funds
                        if (selectedUTXOs.isNotEmpty() && selectedUTXOs.size != getUnspentOutputs().size)
                            _onDisplayExplanation.postValue(getApplication<PlaidApp>().getString(R.string.withdraw_error_not_enough_funds_with_selected_utxos))
                        else
                            _onDisplayExplanation.postValue(getApplication<PlaidApp>().getString(R.string.withdraw_error_not_enough_funds))
                    }

                    -2L -> { // low payment amount
                        _onDisplayExplanation.postValue(getApplication<PlaidApp>().getString(R.string.withdraw_error_low_payment_amount))
                    }

                    0L -> { // something went wrong
                        _onDisplayExplanation.postValue(getApplication<PlaidApp>().getString(R.string.withdraw_error_something_went_wrong))
                    }

                    else -> { // good
                        _onNextStep.postValue(
                            InvoiceDetails(
                                amountToSpend.from(localStoreRepository.getBitcoinDisplayUnit().toRateType(), localStoreRepository.getLocalCurrency()).second,
                                address,
                                description
                            )
                        )
                    }
                }
            }
        }
    }

    private fun invalidAddressError() {
        if(localWallet!!.testNetWallet && walletManager.getBaseWallet(mainNet = true).isAddressValid(address ?: "")) {
            _onDisplayExplanation.postValue(getApplication<PlaidApp>().getString(R.string.withdraw_confirmation_error_invalid_address_test_net))
        } else if(!localWallet!!.testNetWallet && walletManager.getBaseWallet(mainNet = false).isAddressValid(address ?: "")) {
            _onDisplayExplanation.postValue(getApplication<PlaidApp>().getString(R.string.withdraw_confirmation_error_invalid_address_main_net))
        } else {
            _onDisplayExplanation.postValue(getApplication<PlaidApp>().getString(R.string.invoice_error_invalid_address))
        }
    }

    fun updateUTXOs(utxos: MutableList<UnspentOutput>) {
        selectedUTXOs = utxos
        updateAvailableBalance()
    }

    fun addSingleUTXO(utxo: UnspentOutput) {
        if(selectedUTXOs.find { it == utxo } == null) {
            selectedUTXOs.add(utxo)
            updateAvailableBalance()
        }
    }

    fun getSelectedUTXOs() = selectedUTXOs

    data class InvoiceDetails(
        val amount: String,
        val address: String,
        val description: String
    )
}