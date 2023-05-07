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
import io.horizontalsystems.bitcoincore.models.TransactionInfo


class AtpDetailsViewModel(
    application: Application,
    apiRepository: ApiRepository,
    private val localStoreRepository: LocalStoreRepository,
    private val walletManager: WalletDelegate,
    private val delegateManager: DelegateManager
): WalletViewModel(application, localStoreRepository, apiRepository, walletManager, delegateManager) {

    protected val _transfer = SingleLiveData<AssetTransferModel>()
    val transfer: LiveData<AssetTransferModel> = _transfer

    private var transferId = ""

    fun getTransferData(showError: Boolean = true) {
        val transfer =
            localStoreRepository.getAllAssetTransfers(getWalletId())
                .find {
                    it.id == transferId
                }

        if(transfer != null) {
            _transfer.postValue(
                transfer!!
            )
        } else {
            if(showError) {
                // error
            }
        }
    }

    fun getTransaction(id: String): TransactionInfo? {
        return getWallet()!!.walletKit!!.getTransaction(id)
    }

    fun setTransferId(transferId: String) {
        this.transferId = transferId
    }
}