package com.intuisoft.plaid.features.settings.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.intuisoft.plaid.androidwrappers.SingleLiveData
import com.intuisoft.plaid.androidwrappers.WalletViewModel
import com.intuisoft.plaid.delegates.DelegateManager
import com.intuisoft.plaid.common.model.SavedAddressModel
import com.intuisoft.plaid.common.repositories.ApiRepository
import com.intuisoft.plaid.common.repositories.LocalStoreRepository
import com.intuisoft.plaid.common.delegates.wallet.WalletDelegate
import kotlinx.coroutines.launch


class AddressBookViewModel(
    application: Application,
    apiRepository: ApiRepository,
    private val localStoreRepository: LocalStoreRepository,
    private val walletManager: WalletDelegate,
    private val delegateManager: DelegateManager
): WalletViewModel(application, localStoreRepository, apiRepository, walletManager, delegateManager) {

    private val _addresses = SingleLiveData<List<SavedAddressModel>>()
    val addresses: LiveData<List<SavedAddressModel>> = _addresses

    fun savedAddressExists(name: String) : Boolean {
        return localStoreRepository.getSavedAddresses().find { it.addressName == name } != null
    }

    fun saveAddress(name: String, address: String) {
        localStoreRepository.saveAddress(name, address)
    }

    fun updateAddress(oldName: String, name: String, address: String) {
        localStoreRepository.updateSavedAddress(oldName, name, address)
    }

    fun removeAddress(name: String) {
        localStoreRepository.deleteSavedAddress(name)
    }

    fun showAddresses() {
        viewModelScope.launch {
            _addresses.postValue(localStoreRepository.getSavedAddresses())
        }
    }
}