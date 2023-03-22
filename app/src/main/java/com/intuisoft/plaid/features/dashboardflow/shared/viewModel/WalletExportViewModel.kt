package com.intuisoft.plaid.features.dashboardflow.shared.viewModel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.intuisoft.plaid.OmoirApp
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.SingleLiveData
import com.intuisoft.plaid.androidwrappers.WalletViewModel
import com.intuisoft.plaid.common.repositories.ApiRepository
import com.intuisoft.plaid.common.repositories.LocalStoreRepository
import com.intuisoft.plaid.common.util.Constants
import com.intuisoft.plaid.walletmanager.AbstractWalletManager
import io.horizontalsystems.bitcoincore.models.BitcoinPaymentData
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class WalletExportViewModel(
    application: Application,
    apiRepository: ApiRepository,
    private val localStoreRepository: LocalStoreRepository,
    private val walletManager: AbstractWalletManager
): WalletViewModel(application, localStoreRepository, apiRepository, walletManager) {


    protected val _xpubClickable = SingleLiveData<Boolean>()
    val xpubClickable: LiveData<Boolean> = _xpubClickable

    protected val _xpubData = SingleLiveData<String>()
    val xpubData: LiveData<String> = _xpubData

    protected val _copyXpub = SingleLiveData<Boolean>()
    val copyXpub: LiveData<Boolean> = _copyXpub

    protected val _showInvoice = SingleLiveData<BitcoinPaymentData>()
    val showInvoice: LiveData<BitcoinPaymentData> = _showInvoice

    fun copyXpubToClipboard(xpub: String) {
        viewModelScope.launch {
            _xpubClickable.postValue(false)
            _xpubData.postValue(getApplication<OmoirApp>().getString(R.string.export_wallet_copied_to_clipboard))
            _copyXpub.postValue(true)

            delay(Constants.Time.ITEM_COPY_DELAY_LONG.toLong())
            _copyXpub.postValue(false)
            _xpubData.postValue(xpub)
            _xpubClickable.postValue(true)
        }
    }

    fun setInvoice(amount: Double, description: String) {
        _showInvoice.postValue(
            BitcoinPaymentData(
                address = getRecieveAddress(),
                amount = amount,
                label = description
            )
        )
    }
}