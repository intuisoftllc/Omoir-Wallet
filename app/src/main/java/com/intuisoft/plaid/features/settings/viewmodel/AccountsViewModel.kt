package com.intuisoft.plaid.features.settings.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.intuisoft.plaid.androidwrappers.SingleLiveData
import com.intuisoft.plaid.androidwrappers.WalletViewModel
import com.intuisoft.plaid.delegates.DelegateManager
import com.intuisoft.plaid.common.model.SavedAccountModel
import com.intuisoft.plaid.common.repositories.ApiRepository
import com.intuisoft.plaid.common.repositories.LocalStoreRepository
import com.intuisoft.plaid.common.delegates.wallet.WalletDelegate
import kotlinx.coroutines.launch


class AccountsViewModel(
    application: Application,
    apiRepository: ApiRepository,
    private val localStoreRepository: LocalStoreRepository,
    private val walletManager: WalletDelegate,
    private val delegateManager: DelegateManager
): WalletViewModel(application, localStoreRepository, apiRepository, walletManager, delegateManager) {

    private val _accounts = SingleLiveData<List<SavedAccountModel>>()
    val accounts: LiveData<List<SavedAccountModel>> = _accounts

    fun isAccountInUse(name: String): Boolean {
        return walletManager.getHiddenWallets().get(name) != null
    }

    fun showAccounts() {
        viewModelScope.launch {
            _accounts.postValue(localStoreRepository.getSavedAccounts())
        }
    }
}