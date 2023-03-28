package com.intuisoft.plaid.features.dashboardflow.free.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.intuisoft.plaid.androidwrappers.SingleLiveData
import com.intuisoft.plaid.androidwrappers.WalletViewModel
import com.intuisoft.plaid.common.delegates.DelegateManager
import com.intuisoft.plaid.model.ExportDataType
import com.intuisoft.plaid.common.repositories.ApiRepository
import com.intuisoft.plaid.common.repositories.LocalStoreRepository
import com.intuisoft.plaid.common.util.Constants
import com.intuisoft.plaid.common.util.SimpleTimeFormat
import com.intuisoft.plaid.model.ValueFilter
import com.intuisoft.plaid.util.CsvExporter
import com.intuisoft.plaid.walletmanager.AbstractWalletManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime


class ExportOptionsViewModel(
    application: Application,
    apiRepository: ApiRepository,
    private val localStoreRepository: LocalStoreRepository,
    private val walletManager: AbstractWalletManager,
    private val delegateManager: DelegateManager
): WalletViewModel(application, localStoreRepository, apiRepository, walletManager, delegateManager) {

    protected val _exportFinished = SingleLiveData<String?>()
    val exportFinished: LiveData<String?> = _exportFinished

    var dataType = ExportDataType.RAW
    var valueFilter = ValueFilter.GREATER_THAN_EQ
    var dataValueLimit: Double = 0.0 // in BTC
    var advancedExport = false
    lateinit var startPeriod: Instant
    lateinit var endPeriod: Instant

    fun setStartAndEndPeriod() {
        startPeriod = Instant.ofEpochMilli(getWalletBirthday())
        endPeriod = Instant.now()
    }

    fun exportToCsv() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val timeRange = (SimpleTimeFormat.startOfDay(ZonedDateTime.ofInstant(startPeriod, ZoneId.systemDefault())).toEpochSecond()
                    ..(SimpleTimeFormat.endOfDay(ZonedDateTime.ofInstant(endPeriod, ZoneId.systemDefault())).toEpochSecond()))
                var transactions = getWallet()!!.walletKit!!.getAllTransactions().filter {
                    it.timestamp in timeRange
                }
                val exportDataType = if(advancedExport) dataType else ExportDataType.RAW

                if(advancedExport && dataValueLimit != 0.0) {
                    val valueInSats = (dataValueLimit * Constants.Limit.SATS_PER_BTC).toLong()

                    when(valueFilter) {
                        ValueFilter.LESS_THAN -> {
                            transactions = transactions.filter {
                                it.amount < valueInSats
                            }
                        }

                        ValueFilter.GREATER_THAN -> {
                            transactions = transactions.filter {
                                it.amount > valueInSats
                            }
                        }

                        ValueFilter.LESS_THAN_EQ -> {
                            transactions = transactions.filter {
                                it.amount <= valueInSats
                            }
                        }

                        ValueFilter.GREATER_THAN_EQ -> {
                            transactions = transactions.filter {
                                it.amount >= valueInSats
                            }
                        }

                        ValueFilter.EQUAL_TO -> {
                            transactions = transactions.filter {
                                it.amount == valueInSats
                            }
                        }

                        ValueFilter.NOT_EQUAL_TO -> {
                            transactions = transactions.filter {
                                it.amount != valueInSats
                            }
                        }
                    }
                }

                val exporter = CsvExporter(
                    getApplication(),
                    localStoreRepository,
                    delegateManager,
                    getWalletName(),
                    transactions,
                    exportDataType
                )

                _exportFinished.postValue(exporter.export())
            }
        }
    }
}