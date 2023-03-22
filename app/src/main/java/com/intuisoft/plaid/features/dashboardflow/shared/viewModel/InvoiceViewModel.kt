package com.intuisoft.plaid.features.dashboardflow.shared.viewModel

import android.app.Application
import androidx.lifecycle.LiveData
import com.intuisoft.plaid.OmoirApp
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.SingleLiveData
import com.intuisoft.plaid.androidwrappers.WalletViewModel
import com.intuisoft.plaid.common.repositories.ApiRepository
import com.intuisoft.plaid.common.repositories.LocalStoreRepository
import com.intuisoft.plaid.common.util.RateConverter
import com.intuisoft.plaid.walletmanager.AbstractWalletManager
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

    private var amountToSpend: RateConverter = RateConverter(localStoreRepository.getRateFor(localStoreRepository.getLocalCurrency())?.currentPrice ?: 0.0)
    private var description: String = ""
    private var address: String = ""
    private var exchangeId: String? = null

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
                else getApplication<OmoirApp>().getString(R.string.not_applicable),
                exchangeId
            )
        )
        _enableNext.postValue(Unit)
    }

    fun setExchangeId(id: String?) {
        exchangeId = id
    }

    fun updateAvailableBalance() {
        _onAvailableBalanceUpdated.postValue(
            getMaxSpend().from(localStoreRepository.getBitcoinDisplayUnit().toRateType(), localStoreRepository.getLocalCurrency(), false).second!!
        )
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
        _onDisplayExplanation.postValue(getApplication<OmoirApp>().getString(R.string.invoice_error_over_balance))
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

                    else -> { // good
                        _onNextStep.postValue(
                            InvoiceDetails(
                                amountToSpend.from(localStoreRepository.getBitcoinDisplayUnit().toRateType(), localStoreRepository.getLocalCurrency()).second,
                                address,
                                description,
                                exchangeId
                            )
                        )
                    }
                }
            }
        }
    }

    private fun invalidAddressError() {
        if(localWallet!!.testNetWallet && walletManager.getBaseWallet(mainNet = true).isAddressValid(address ?: "")) {
            _onDisplayExplanation.postValue(getApplication<OmoirApp>().getString(R.string.withdraw_confirmation_error_invalid_address_test_net))
        } else if(!localWallet!!.testNetWallet && walletManager.getBaseWallet(mainNet = false).isAddressValid(address ?: "")) {
            _onDisplayExplanation.postValue(getApplication<OmoirApp>().getString(R.string.withdraw_confirmation_error_invalid_address_main_net))
        } else {
            _onDisplayExplanation.postValue(getApplication<OmoirApp>().getString(R.string.invoice_error_invalid_address))
        }
    }

    override fun updateUTXOs(utxos: MutableList<UnspentOutput>) {
        super.updateUTXOs(utxos)
        updateAvailableBalance()
    }

    override fun addSingleUTXO(utxo: UnspentOutput) {
        super.addSingleUTXO(utxo)
        updateAvailableBalance()
    }

    data class InvoiceDetails(
        val amount: String,
        val address: String,
        val description: String,
        val exchangeId: String?
    )
}