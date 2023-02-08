package com.intuisoft.plaid.features.settings.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.intuisoft.plaid.androidwrappers.SingleLiveData
import com.intuisoft.plaid.androidwrappers.WalletViewModel
import com.intuisoft.plaid.common.model.SavedAddressModel
import com.intuisoft.plaid.common.repositories.ApiRepository
import com.intuisoft.plaid.common.repositories.LocalStoreRepository
import com.intuisoft.plaid.walletmanager.AbstractWalletManager
import com.revenuecat.purchases.models.StoreProduct
import kotlinx.coroutines.launch


class SubscriptionViewModel(
    application: Application,
    apiRepository: ApiRepository,
    private val localStoreRepository: LocalStoreRepository,
    private val walletManager: AbstractWalletManager
): WalletViewModel(application, localStoreRepository, apiRepository, walletManager) {

    private val _purchaseProductUpdated = SingleLiveData<Unit>()
    val purchaseProductUpdated: LiveData<Unit> = _purchaseProductUpdated

    private var purchaseProduct: StoreProduct? = null

    fun setPurchaseProduct(product: StoreProduct) {
        purchaseProduct = product
        _purchaseProductUpdated.postValue(Unit)
    }

    fun getPurchaseProduct() = purchaseProduct
}