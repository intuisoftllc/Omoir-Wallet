package com.intuisoft.plaid.features.dashboardflow.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.intuisoft.plaid.PlaidApp
import com.intuisoft.plaid.androidwrappers.SingleLiveData
import com.intuisoft.plaid.androidwrappers.WalletViewModel
import com.intuisoft.plaid.common.model.*
import com.intuisoft.plaid.common.repositories.ApiRepository
import com.intuisoft.plaid.common.repositories.LocalStoreRepository
import com.intuisoft.plaid.common.util.Constants
import com.intuisoft.plaid.common.util.RateConverter
import com.intuisoft.plaid.common.util.SimpleCurrencyFormat
import com.intuisoft.plaid.util.NetworkUtil
import com.intuisoft.plaid.walletmanager.AbstractWalletManager
import io.horizontalsystems.bitcoincore.models.TransactionInfo
import io.horizontalsystems.bitcoincore.models.TransactionStatus
import io.horizontalsystems.bitcoincore.models.TransactionType
import kotlinx.coroutines.*
import java.time.Instant
import java.time.ZonedDateTime


class DashboardViewModel(
    application: Application,
    private val apiRepository: ApiRepository,
    private val localStoreRepository: LocalStoreRepository,
    private val walletManager: AbstractWalletManager
): WalletViewModel(application, localStoreRepository, apiRepository, walletManager) {

    protected val _network = SingleLiveData<String>()
    val network: LiveData<String> = _network

    protected val _percentageGain = SingleLiveData<Double>()
    val percentageGain: LiveData<Double> = _percentageGain

    protected val _showChartContent = SingleLiveData<Boolean>()
    val showChartContent: LiveData<Boolean> = _showChartContent

    protected val _noChartData = SingleLiveData<Unit>()
    val noChartData: LiveData<Unit> = _noChartData

    protected val _chartData = SingleLiveData<List<ChartDataModel>>()
    val chartData: LiveData<List<ChartDataModel>> = _chartData

    private var intervalType = ChartIntervalType.INTERVAL_1DAY
    private var walletTransactions: List<TransactionInfo> = listOf()
        set(value) {
            field = value
            balanceHistory = generateBalanceHistory()
        }

    private var balanceHistory: List<Pair<Long, Long>> = listOf()

    fun changeChartInterval(intervalType: ChartIntervalType) {
        if(intervalType != this.intervalType) {
            this.intervalType = intervalType
            updateBalanceHistory()
        }
    }

    fun onDisplayUnitChanged() {
        updateBalanceHistory()
    }

    private fun updateBalanceHistory() {
        if(localStoreRepository.getBitcoinDisplayUnit() != BitcoinDisplayUnit.FIAT || NetworkUtil.hasInternet(getApplication<PlaidApp>())) {
            _showChartContent.postValue(true)
            updateWalletBalanceHistory()
        } else {
            onNoInternet(false)
        }
    }

    fun onTransactionsUpdated(txList: List<TransactionInfo>) {
        walletTransactions = txList.filter { it.status != TransactionStatus.INVALID }
        updateBalanceHistory()
    }

    fun transformScrubValue(data: ChartDataModel): String {
        val rateConverter = RateConverter(
            0.0
        )

        return when(localStoreRepository.getBitcoinDisplayUnit()) {
            BitcoinDisplayUnit.SATS -> {
                rateConverter.setLocalRate(RateConverter.RateType.SATOSHI_RATE, data.value.toDouble())
                rateConverter.from(RateConverter.RateType.SATOSHI_RATE, localStoreRepository.getLocalCurrency()).second
            }

            BitcoinDisplayUnit.BTC -> {
                rateConverter.setLocalRate(RateConverter.RateType.BTC_RATE, data.value.toDouble())
                rateConverter.from(RateConverter.RateType.BTC_RATE, localStoreRepository.getLocalCurrency()).second
            }

            BitcoinDisplayUnit.FIAT -> {
                SimpleCurrencyFormat.formatValue(
                    localStoreRepository.getLocalCurrency(),
                    data.value.toDouble()
                )
            }
        }
    }

    private fun updateWalletBalanceHistory() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                if(localStoreRepository.isProEnabled()) {
                    if (balanceHistory.isEmpty()) {
                        _noChartData.postValue(Unit)
                    } else {
                        val history = getBalanceHistoryForInterval()
                        val rateConverter = RateConverter(
                            localStoreRepository.getRateFor(localStoreRepository.getLocalCurrency())?.currentPrice
                                ?: 0.0
                        )

                        val gain: Double

                        if(localStoreRepository.getBitcoinDisplayUnit() != BitcoinDisplayUnit.FIAT) {
                            val start = history.first().first
                            val end = history.last().first
                            gain = 100 * ((end.toDouble() - start) / start.toDouble())
                        } else {
                            val data = apiRepository.getTickerPriceChartData(intervalType)
                                ?.filter {
                                    (it.time / Constants.Time.MILLS_PER_SEC) >= history.first().second
                                }

                            if(data != null && data.isNotEmpty()) {
                                val start = getBalanceAtTime(data.first().time / Constants.Time.MILLS_PER_SEC) * data.first().value
                                val end = getBalanceAtTime(data.last().time / Constants.Time.MILLS_PER_SEC) * data.last().value
                                gain = 100 * ((end.toDouble() - start) / start.toDouble())
                            } else {
                                gain = 0.0
                            }
                        }
                        _percentageGain.postValue(gain)

                        when (localStoreRepository.getBitcoinDisplayUnit()) {
                            BitcoinDisplayUnit.SATS -> {
                                _chartData.postValue(
                                    history.map {
                                        ChartDataModel(
                                            time = it.second,
                                            value = it.first.toFloat()
                                        )
                                    }
                                )
                                
                                _showChartContent.postValue(true)
                            }

                            BitcoinDisplayUnit.BTC -> {
                                _chartData.postValue(
                                    history.map {
                                        rateConverter.setLocalRate(
                                            RateConverter.RateType.SATOSHI_RATE,
                                            it.first.toDouble()
                                        )

                                        ChartDataModel(
                                            time = it.second,
                                            value = rateConverter.getRawBtcRate().toFloat()
                                        )
                                    }
                                )
                                _showChartContent.postValue(true)
                            }

                            BitcoinDisplayUnit.FIAT -> {
                                val data = apiRepository.getTickerPriceChartData(intervalType)
                                    ?.filter {
                                        (it.time / Constants.Time.MILLS_PER_SEC) >= history.first().second
                                    }

                                if(data != null && data.isNotEmpty()) {
                                    _chartData.postValue(
                                        data.map {
                                            ChartDataModel(
                                                time = it.time / Constants.Time.MILLS_PER_SEC,
                                                value = getBalanceAtTime(it.time / Constants.Time.MILLS_PER_SEC) * it.value
                                            )
                                        }
                                    )

                                    _showChartContent.postValue(true)
                                } else {
                                    _showChartContent.postValue(false)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun getBalanceAtTime(time: Long): Float {
        val closestBalance = balanceHistory.reversed().first { it.second <= time }
        return closestBalance.first.toFloat() / Constants.Limit.SATS_PER_BTC
    }

    private fun getBalanceHistoryForInterval(): List<Pair<Long, Long>> {
        val txStartTime: Instant

        when(intervalType) {
            ChartIntervalType.INTERVAL_1DAY -> {
                txStartTime = ZonedDateTime.now().minusDays(1).toInstant()
            }
            ChartIntervalType.INTERVAL_1WEEK -> {
                txStartTime = ZonedDateTime.now().minusWeeks(1).toInstant()
            }
            ChartIntervalType.INTERVAL_1MONTH -> {
                txStartTime = ZonedDateTime.now().minusMonths(1).toInstant()
            }
            ChartIntervalType.INTERVAL_3MONTHS -> {
                txStartTime = ZonedDateTime.now().minusMonths(3).toInstant()
            }
            ChartIntervalType.INTERVAL_6MONTHS -> {
                txStartTime = ZonedDateTime.now().minusMonths(6).toInstant()
            }
            ChartIntervalType.INTERVAL_1YEAR -> {
                txStartTime = ZonedDateTime.now().minusYears(1).toInstant()
            }
            ChartIntervalType.INTERVAL_ALL_TIME -> {
                txStartTime = Instant.ofEpochSecond(balanceHistory.firstOrNull()?.second ?: 0)
            }
        }

        var filtered = balanceHistory.filter {
            it.second >= txStartTime.epochSecond
        }

        if(filtered.size == 1) {
            filtered = filtered + filtered
        } else if(filtered.isEmpty() && balanceHistory.isNotEmpty()) {
            filtered = listOf(balanceHistory.last(), balanceHistory.last())
        }

        return filtered
    }

    private fun generateBalanceHistory(): List<Pair<Long, Long>> {
        val balanceHistory = mutableListOf<Pair<Long, Long>>()
        var balance = 0L
        var incomeFound = false

        walletTransactions.reversed().filter {
            if(it.type == TransactionType.Incoming)
                incomeFound = true
            incomeFound
        }.forEach {
            when(it.type) {
                TransactionType.Incoming -> {
                    balance += it.amount
                }

                TransactionType.Outgoing -> {
                    balance -= it.amount + (it.fee?: 0)
                }

                TransactionType.SentToSelf -> {
                    balance -= it.fee ?: 0
                }
            }

            balanceHistory.add(balance to it.timestamp)
        }

        return balanceHistory
    }


    fun onNoInternet(hasInternet: Boolean) {
        CoroutineScope(Dispatchers.IO).launch {

            val data = apiRepository.getTickerPriceChartData(intervalType)

            if(data != null) {
                _showChartContent.postValue(true)
                updateWalletBalanceHistory()
            } else {
                _showChartContent.postValue(hasInternet)

                if (hasInternet) {
                    updateWalletBalanceHistory()
                }
            }
        }
    }
}