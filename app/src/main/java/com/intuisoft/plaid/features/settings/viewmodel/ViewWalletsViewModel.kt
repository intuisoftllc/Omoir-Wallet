package com.intuisoft.plaid.features.settings.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.intuisoft.plaid.androidwrappers.BaseViewModel
import com.intuisoft.plaid.androidwrappers.SingleLiveData
import com.intuisoft.plaid.model.LocalWalletModel
import com.intuisoft.plaid.repositories.LocalStoreRepository
import com.intuisoft.plaid.walletmanager.AbstractWalletManager
import kotlinx.coroutines.launch


class ViewWalletsViewModel(
    application: Application,
    private val localStoreRepository: LocalStoreRepository,
    private val walletManager: AbstractWalletManager
): BaseViewModel(application, localStoreRepository, walletManager) {

    private val _wallets = SingleLiveData<List<LocalWalletModel>>()
    val wallets: LiveData<List<LocalWalletModel>> = _wallets

    fun showWallets() {
        viewModelScope.launch {
            _wallets.postValue(walletManager.getWalletsAsync())
        }
    }
}