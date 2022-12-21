package com.intuisoft.plaid.features.dashboardflow.pro.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.intuisoft.plaid.androidwrappers.SingleLiveData
import com.intuisoft.plaid.androidwrappers.WalletViewModel
import com.intuisoft.plaid.common.model.AssetTransferModel
import com.intuisoft.plaid.common.repositories.ApiRepository
import com.intuisoft.plaid.common.repositories.LocalStoreRepository
import com.intuisoft.plaid.util.CsvExporter
import com.intuisoft.plaid.walletmanager.AbstractWalletManager
import io.horizontalsystems.bitcoincore.models.TransactionInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class ExportOptionsViewModel(
    application: Application,
    apiRepository: ApiRepository,
    private val localStoreRepository: LocalStoreRepository,
    private val walletManager: AbstractWalletManager
): WalletViewModel(application, localStoreRepository, apiRepository, walletManager) {

    protected val _exportFinished = SingleLiveData<String>()
    val exportFinished: LiveData<String> = _exportFinished


    fun exportToCsv() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val transactions = getWallet()!!.walletKit!!.getAllTransactions()
                val exporter = CsvExporter(getApplication(), localStoreRepository, getWalletName(), transactions)
                _exportFinished.postValue(exporter.export())
            }
        }
    }
}