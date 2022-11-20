package com.intuisoft.plaid.features.dashboardflow.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.intuisoft.plaid.androidwrappers.SingleLiveData
import com.intuisoft.plaid.androidwrappers.WalletViewModel
import com.intuisoft.plaid.common.repositories.ApiRepository
import com.intuisoft.plaid.common.repositories.LocalStoreRepository
import com.intuisoft.plaid.walletmanager.AbstractWalletManager
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