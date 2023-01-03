package com.intuisoft.plaid.features.dashboardflow.shared.viewModel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.intuisoft.plaid.androidwrappers.SingleLiveData
import com.intuisoft.plaid.androidwrappers.WalletViewModel
import com.intuisoft.plaid.common.model.ExchangeHistoryFilter
import com.intuisoft.plaid.common.model.ExchangeInfoDataModel
import com.intuisoft.plaid.common.repositories.ApiRepository
import com.intuisoft.plaid.common.repositories.LocalStoreRepository
import com.intuisoft.plaid.common.model.ExchangeStatus
import com.intuisoft.plaid.walletmanager.AbstractWalletManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class ExchangeHistoryViewModel(
    application: Application,
    private val apiRepository: ApiRepository,
    private val localStoreRepository: LocalStoreRepository,
    private val walletManager: AbstractWalletManager
): WalletViewModel(application, localStoreRepository, apiRepository, walletManager) {

    protected val _historyFilter = SingleLiveData<Pair<ExchangeHistoryFilter, List<ExchangeInfoDataModel>>>()
    val historyFilter: LiveData<Pair<ExchangeHistoryFilter, List<ExchangeInfoDataModel>>> = _historyFilter

    protected val _refreshingExchanges = SingleLiveData<Boolean>()
    val refreshingExchanges: LiveData<Boolean> = _refreshingExchanges

    var currentFilter: ExchangeHistoryFilter = ExchangeHistoryFilter.ALL
    private var refreshing = false

    fun refreshExchanges() {
        CoroutineScope(Dispatchers.IO).launch {
            if(!refreshing) {
                refreshing = true
                _refreshingExchanges.postValue(refreshing)
                localStoreRepository.getAllExchanges(getWalletId())
                    .forEach {
                        apiRepository.updateExchange(it.id, getWalletId())
                    }

                refreshing = false
                _refreshingExchanges.postValue(refreshing)
                setFilter(currentFilter, true)
            }
        }
    }

    fun setFilter(filter: ExchangeHistoryFilter, force: Boolean = false) {
        viewModelScope.launch {
            if(!force && currentFilter != null && filter == currentFilter)
                return@launch

            currentFilter = filter
            when (filter) {
                ExchangeHistoryFilter.ALL -> {
                    _historyFilter.postValue(
                        filter to localStoreRepository.getAllExchanges(getWalletId())
                            .sortedByDescending { it.timestamp.epochSecond }
                    )
                }

                ExchangeHistoryFilter.FINISHED -> {
                    _historyFilter.postValue(
                        filter to localStoreRepository.getAllExchanges(getWalletId())
                            .filter {
                                it.status == ExchangeStatus.FINISHED.type
                            }.sortedByDescending { it.timestamp.epochSecond }
                    )
                }

                ExchangeHistoryFilter.FAILED -> {
                    _historyFilter.postValue(
                        filter to localStoreRepository.getAllExchanges(getWalletId())
                            .filter {
                                it.status == ExchangeStatus.FAILED.type
                            }.sortedByDescending { it.timestamp.epochSecond }
                    )
                }
            }
        }
    }

}