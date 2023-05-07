package com.intuisoft.plaid.features.dashboardflow.pro.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import com.intuisoft.plaid.androidwrappers.SingleLiveData
import com.intuisoft.plaid.androidwrappers.WalletViewModel
import com.intuisoft.plaid.delegates.DelegateManager
import com.intuisoft.plaid.common.model.AssetTransferModel
import com.intuisoft.plaid.common.repositories.ApiRepository
import com.intuisoft.plaid.common.repositories.LocalStoreRepository
import com.intuisoft.plaid.common.delegates.wallet.WalletDelegate


class AtpHistoryViewModel(
    application: Application,
    apiRepository: ApiRepository,
    private val localStoreRepository: LocalStoreRepository,
    private val walletManager: WalletDelegate,
    private val delegateManager: DelegateManager
): WalletViewModel(application, localStoreRepository, apiRepository, walletManager, delegateManager) {

    protected val _transfers = SingleLiveData<List<AssetTransferModel>>()
    val transfers: LiveData<List<AssetTransferModel>> = _transfers

    fun getTransfers() {
        _transfers.postValue(
            localStoreRepository.getAllAssetTransfers(getWalletId())
        )
    }
}