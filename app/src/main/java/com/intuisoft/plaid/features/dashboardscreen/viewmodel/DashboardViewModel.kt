package com.intuisoft.plaid.features.dashboardscreen.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.intuisoft.plaid.androidwrappers.BaseViewModel
import com.intuisoft.plaid.androidwrappers.SingleLiveData
import com.intuisoft.plaid.model.LocalWalletModel
import com.intuisoft.plaid.repositories.LocalStoreRepository
import com.intuisoft.plaid.walletmanager.WalletManager
import io.horizontalsystems.bitcoincore.models.Transaction
import io.horizontalsystems.bitcoincore.models.TransactionInfo
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.launch
import java.util.*


class DashboardViewModel(
    application: Application,
    private val localStoreRepository: LocalStoreRepository,
    private val walletManager: WalletManager
): BaseViewModel(application, localStoreRepository, walletManager) {

    private val _displayWallet = SingleLiveData<LocalWalletModel>()
    val displayWallet: LiveData<LocalWalletModel> = _displayWallet

    private val _transactions = SingleLiveData<List<TransactionInfo>>()
    val transactions: LiveData<List<TransactionInfo>> = _transactions

    private var wallet: LocalWalletModel? = null
    private val disposables = CompositeDisposable()

    fun getWallet(walletName: String) {
        wallet = walletManager.findWallet(walletName)

        wallet?.let {
            _displayWallet.postValue(it)
        }
    }

    fun getTransactions() {
        wallet!!.walletKit!!.transactions(type = null).subscribe { txList: List<TransactionInfo> ->
            _transactions.postValue(txList)
        }.let {
            disposables.add(it)
        }
    }


    override fun onCleared() {
        super.onCleared()
        disposables.dispose()
    }

}