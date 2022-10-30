package com.intuisoft.plaid.features.dashboardscreen.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.intuisoft.plaid.androidwrappers.BaseViewModel
import com.intuisoft.plaid.androidwrappers.SingleLiveData
import com.intuisoft.plaid.androidwrappers.WalletViewModel
import com.intuisoft.plaid.model.LocalWalletModel
import com.intuisoft.plaid.repositories.LocalStoreRepository
import com.intuisoft.plaid.walletmanager.AbstractWalletManager
import com.intuisoft.plaid.walletmanager.WalletManager
import io.horizontalsystems.bitcoincore.models.TransactionInfo
import io.horizontalsystems.bitcoincore.network.Network
import io.horizontalsystems.bitcoinkit.BitcoinKit
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class WalletExportViewModel(
    application: Application,
    private val localStoreRepository: LocalStoreRepository,
    private val walletManager: AbstractWalletManager
): WalletViewModel(application, localStoreRepository, walletManager) {


    protected val _xpubClickable = SingleLiveData<Boolean>()
    val xpubClickable: LiveData<Boolean> = _xpubClickable

    protected val _xpubData = SingleLiveData<String>()
    val xpubData: LiveData<String> = _xpubData

    protected val _copyXpub = SingleLiveData<Boolean>()
    val copyXpub: LiveData<Boolean> = _copyXpub

    fun copyXpubToClipboard(xpub: String) {
        viewModelScope.launch {
            _xpubClickable.postValue(false)
            _xpubData.postValue("Copied To Clipboard!")
            _copyXpub.postValue(true)

            delay(1000)
            _copyXpub.postValue(false)
            _xpubData.postValue(xpub)
            _xpubClickable.postValue(true)
        }
    }
}