package com.intuisoft.plaid.features.dashboardflow.shared.viewModel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.intuisoft.plaid.PlaidApp
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.SingleLiveData
import com.intuisoft.plaid.androidwrappers.WalletViewModel
import com.intuisoft.plaid.common.model.*
import com.intuisoft.plaid.common.repositories.ApiRepository
import com.intuisoft.plaid.common.repositories.LocalStoreRepository
import com.intuisoft.plaid.common.util.Constants
import com.intuisoft.plaid.common.util.RateConverter
import com.intuisoft.plaid.common.util.SimpleCurrencyFormat
import com.intuisoft.plaid.common.util.extensions.safeWalletScope
import com.intuisoft.plaid.util.NetworkUtil
import com.intuisoft.plaid.util.SimpleTimeFormat
import com.intuisoft.plaid.walletmanager.AbstractWalletManager
import io.horizontalsystems.bitcoincore.models.TransactionInfo
import io.horizontalsystems.bitcoincore.models.TransactionStatus
import io.horizontalsystems.bitcoincore.models.TransactionType
import kotlinx.coroutines.*
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*


class DashboardViewModel(
    application: Application,
    private val apiRepository: ApiRepository,
    private val localStoreRepository: LocalStoreRepository,
    private val walletManager: AbstractWalletManager
): WalletViewModel(application, localStoreRepository, apiRepository, walletManager) {

    protected val _network = SingleLiveData<String>()
    val network: LiveData<String> = _network

    protected val _percentageGain = SingleLiveData<Pair<Double, Double>>()
    val percentageGain: LiveData<Pair<Double, Double>> = _percentageGain

    protected val _showChartError = SingleLiveData<String?>()
    val showChartError: LiveData<String?> = _showChartError

    protected val _chartDataLoading = SingleLiveData<Boolean>()
    val chartDataLoading: LiveData<Boolean> = _chartDataLoading

    protected val _noChartData = SingleLiveData<Unit>()
    val noChartData: LiveData<Unit> = _noChartData

    protected val _chartData = SingleLiveData<List<ChartDataModel>>()
    val chartData: LiveData<List<ChartDataModel>> = _chartData

    protected val _totalSent = SingleLiveData<String>()
    val totalSent: LiveData<String> = _totalSent

    protected val _totalReceived = SingleLiveData<String>()
    val totalReceived: LiveData<String> = _totalReceived

    protected val _averagePrice = SingleLiveData<String>()
    val averagePrice: LiveData<String> = _averagePrice

    protected val _highestBalance = SingleLiveData<String>()
    val highestBalance: LiveData<String> = _highestBalance

    protected val _walletAge = SingleLiveData<String>()
    val walletAge: LiveData<String> = _walletAge

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
            _showChartError.postValue(null)
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
                rateConverter.setLocalRate(RateConverter.RateType.BTC_RATE, data.value.toDouble())
                rateConverter.from(RateConverter.RateType.SATOSHI_RATE, localStoreRepository.getLocalCurrency(), false).second
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
               safeWalletScope {
                    if(localStoreRepository.isProEnabled()) {
                        if (balanceHistory.isEmpty()) {
                            _noChartData.postValue(Unit)

                            val rate = RateConverter(localStoreRepository.getRateFor(localStoreRepository.getLocalCurrency())?.currentPrice ?: 0.0)
                            rate.setLocalRate(RateConverter.RateType.FIAT_RATE, 0.0)
                            _totalSent.postValue(rate.from(RateConverter.RateType.FIAT_RATE, localStoreRepository.getLocalCurrency()).second!!)
                            _totalReceived.postValue(rate.from(RateConverter.RateType.FIAT_RATE, localStoreRepository.getLocalCurrency()).second!!)
                            _averagePrice.postValue(rate.from(RateConverter.RateType.FIAT_RATE, localStoreRepository.getLocalCurrency()).second!!)
                            _highestBalance.postValue(rate.from(RateConverter.RateType.FIAT_RATE, localStoreRepository.getLocalCurrency()).second!!)

                            walletManager.findStoredWallet(getWalletId())?.createdAt?.let {
                                if(it > 0) {
                                    _walletAge.postValue(
                                        SimpleTimeFormat.timeToString(
                                            it,
                                            "(${SimpleTimeFormat.getDateByLocale(it, Locale.US)})"
                                        )
                                    )
                                } else {
                                    _walletAge.postValue(getApplication<PlaidApp>().getString(R.string.pro_wallet_dashboard_new_wallet))
                                }
                            }
                        } else {
                            _chartDataLoading.postValue(true)
                            _chartData.postValue(listOf())
                            val history = getBalanceHistoryForInterval(intervalType)
                            val rateConverter = RateConverter(
                                localStoreRepository.getRateFor(localStoreRepository.getLocalCurrency())?.currentPrice
                                    ?: 0.0
                            )

                            if(localStoreRepository.isProEnabled()) {
                                val time = getMaxMarketInterval(ChartIntervalType.INTERVAL_ALL_TIME, Instant.ofEpochSecond(balanceHistory.first().second-1))
                                val allTimeMarketData =
                                    apiRepository.getMarketHistoryData(localStoreRepository.getLocalCurrency(), time.first, time.second)
                                val createdTime =
                                    Math.min(walletManager.findStoredWallet(getWalletId())!!.createdAt, balanceHistory.first().second * Constants.Time.MILLS_PER_SEC)
                                val incomingTxs =
                                    walletTransactions.filter { it.type == TransactionType.Incoming }
                                val outgoingTxs =
                                    walletTransactions.filter { it.type == TransactionType.Outgoing || it.type == TransactionType.SentToSelf }

                                val totalSentCost = outgoingTxs
                                    .map { tx ->
                                        if(tx.type == TransactionType.Outgoing) {
                                            (tx.amount.toDouble() + (tx.fee?.toDouble() ?: 0.0)) / Constants.Limit.SATS_PER_BTC
                                        } else {
                                            (tx.fee?.toDouble() ?: 0.0) / Constants.Limit.SATS_PER_BTC
                                        }
                                    }.sum()

                                val totalReceivedCost = incomingTxs
                                    .map { tx ->
                                        (tx.amount.toDouble()) / Constants.Limit.SATS_PER_BTC
                                    }.sum()

                                val averagePrice = incomingTxs
                                    .map { tx ->
                                        allTimeMarketData?.find { it.time >= tx.timestamp }?.price?.toFloat() ?: 0f
                                    }.average()

                                val highestBalance = balanceHistory
                                    .map { balance ->
                                        balance.first
                                    }.maxOrNull()

                                val rate = RateConverter(
                                    localStoreRepository.getRateFor(localStoreRepository.getLocalCurrency())?.currentPrice
                                        ?: 0.0
                                )
                                rate.setLocalRate(
                                    RateConverter.RateType.BTC_RATE,
                                    totalSentCost
                                )
                                _totalSent.postValue(
                                    rate.from(
                                        if(rate.getRawBtcRate() >= 1.0 && localStoreRepository.getBitcoinDisplayUnit() != BitcoinDisplayUnit.FIAT)
                                            RateConverter.RateType.BTC_RATE
                                        else localStoreRepository.getBitcoinDisplayUnit().toRateType(),
                                        localStoreRepository.getLocalCurrency()
                                    ).second!!
                                )

                                rate.setLocalRate(
                                    RateConverter.RateType.BTC_RATE,
                                    totalReceivedCost
                                )
                                _totalReceived.postValue(
                                    rate.from(
                                        if(rate.getRawBtcRate() >= 1.0 && localStoreRepository.getBitcoinDisplayUnit() != BitcoinDisplayUnit.FIAT)
                                            RateConverter.RateType.BTC_RATE
                                        else localStoreRepository.getBitcoinDisplayUnit().toRateType(),
                                        localStoreRepository.getLocalCurrency()
                                    ).second!!
                                )

                                rate.setLocalRate(RateConverter.RateType.FIAT_RATE, averagePrice)
                                _averagePrice.postValue(
                                    rate.from(
                                        RateConverter.RateType.FIAT_RATE,
                                        localStoreRepository.getLocalCurrency()
                                    ).second!!
                                )

                                rate.setLocalRate(RateConverter.RateType.SATOSHI_RATE, highestBalance?.toDouble() ?: 0.0)
                                _highestBalance.postValue(
                                    rate.from(
                                        if(rate.getRawBtcRate() >= 1.0 && localStoreRepository.getBitcoinDisplayUnit() != BitcoinDisplayUnit.FIAT)
                                            RateConverter.RateType.BTC_RATE
                                        else localStoreRepository.getBitcoinDisplayUnit().toRateType(),
                                        localStoreRepository.getLocalCurrency()
                                    ).second!!
                                )

                                _walletAge.postValue(
                                    SimpleTimeFormat.timeToString(
                                        createdTime,
                                        "(${SimpleTimeFormat.getDateByLocale(createdTime, Locale.US)})"
                                    )
                                )
                            }

                            when (localStoreRepository.getBitcoinDisplayUnit()) {
                                BitcoinDisplayUnit.SATS -> {
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

                                    _showChartError.postValue(null)
                                    var beginValue = history.first { it.first > 0 }?.first ?: 0L
                                    val endValue = history.lastOrNull()?.first ?: 0L
                                    _percentageGain.postValue(100 * ((endValue.toDouble() - beginValue) / beginValue.toDouble()) to (endValue - beginValue).toDouble())
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

                                    _showChartError.postValue(null)
                                    var beginValue = history.first { it.first > 0 }?.first ?: 0L
                                    val endValue = history.lastOrNull()?.first ?: 0L
                                    _percentageGain.postValue(100 * ((endValue.toDouble() - beginValue) / beginValue.toDouble()) to (endValue - beginValue).toDouble())
                                }

                                BitcoinDisplayUnit.FIAT -> {
                                    val max = getMaxMarketInterval(intervalType, Instant.ofEpochSecond(balanceHistory.first().second-1))
                                    val data = apiRepository.getMarketHistoryData(localStoreRepository.getLocalCurrency(), max.first, max.second)

                                    if(data != null && data.isNotEmpty()) {
                                        val cData = data.map {
                                            ChartDataModel(
                                                time = it.time / Constants.Time.MILLS_PER_SEC,
                                                value = getBalanceAtTime(it.time / Constants.Time.MILLS_PER_SEC, balanceHistory) * it.price.toFloat()
                                            )
                                        }

                                        _chartData.postValue(
                                            cData
                                        )

                                        _showChartError.postValue(null)
                                        var beginValue = cData.first { it.value > 0 }.value
                                        val endValue =  cData.last().value
                                        _percentageGain.postValue(100 * ((endValue.toDouble() - beginValue) / beginValue.toDouble()) to (endValue - beginValue).toDouble())
                                    } else {
                                        _showChartError.postValue(getApplication<PlaidApp>().getString(R.string.failed_to_load_chart_data))
                                    }
                                }
                            }

                            _chartDataLoading.postValue(false)
                        }
                    }
                }
            }
        }
    }

    private fun getBalanceAtTime(time: Long, history: List<Pair<Long, Long>>): Float {
        val closestBalance = history.find { it.second >= time }?.first?.toFloat()
            ?: history.lastOrNull()?.first?.toFloat()
        return (closestBalance ?: 0f) / Constants.Limit.SATS_PER_BTC
    }

    private fun getMaxMarketInterval(interval: ChartIntervalType, birthdate: Instant): Pair<Long, Long> {
        val nowTime = SimpleTimeFormat.endOfDay(ZonedDateTime.ofInstant(Instant.now(), ZoneId.systemDefault()))
        val startOfBDay  = SimpleTimeFormat.endOfDay(ZonedDateTime.ofInstant(birthdate, ZoneId.systemDefault())).toEpochSecond()
        var startDay: Long

        when {
            interval == ChartIntervalType.INTERVAL_1DAY -> {
                startDay = nowTime.minusDays(1).toEpochSecond()
            }
            interval == ChartIntervalType.INTERVAL_1WEEK -> {
                startDay = nowTime.minusWeeks(1).toEpochSecond()
            }
            interval == ChartIntervalType.INTERVAL_1MONTH -> {
                startDay = nowTime.minusMonths(1).toEpochSecond()
            }
            interval == ChartIntervalType.INTERVAL_3MONTHS  -> {
                startDay = nowTime.minusMonths(3).toEpochSecond()
            }
            interval == ChartIntervalType.INTERVAL_6MONTHS -> {
                startDay = nowTime.minusMonths(6).toEpochSecond()
            }
            interval == ChartIntervalType.INTERVAL_1YEAR -> {
                startDay = nowTime.minusYears(1).toEpochSecond()
            }
            else -> {
                startDay = startOfBDay
            }
        }

        return startDay to nowTime.toEpochSecond()
    }

    private fun getBalanceHistoryForInterval(interval: ChartIntervalType): List<Pair<Long, Long>> {
        val txStartTime: Instant
        val nowTime: ZonedDateTime = ZonedDateTime.now()
        var splitTime = 0

        when(interval) {
            ChartIntervalType.INTERVAL_1DAY -> {
                txStartTime = nowTime.minusDays(1).toInstant()
                splitTime = 30 * (Constants.Time.ONE_MINUTE * Constants.Time.MILLS_PER_SEC) // 30 minute intervals
            }
            ChartIntervalType.INTERVAL_1WEEK -> {
                txStartTime = nowTime.minusWeeks(1).toInstant()
                splitTime = 30 * (Constants.Time.ONE_MINUTE * Constants.Time.MILLS_PER_SEC) // 30 minute intervals
            }
            ChartIntervalType.INTERVAL_1MONTH -> {
                txStartTime = nowTime.minusMonths(1).toInstant()
                splitTime = 60 * (Constants.Time.ONE_MINUTE * Constants.Time.MILLS_PER_SEC) // 60 minute intervals
            }
            ChartIntervalType.INTERVAL_3MONTHS -> {
                txStartTime = nowTime.minusMonths(3).toInstant()
                splitTime = (Constants.Time.SECONDS_PER_DAY * Constants.Time.MILLS_PER_SEC) // 1 day intervals
            }
            ChartIntervalType.INTERVAL_6MONTHS -> {
                txStartTime = nowTime.minusMonths(6).toInstant()
                splitTime = (Constants.Time.SECONDS_PER_DAY * Constants.Time.MILLS_PER_SEC) // 1 day intervals
            }
            ChartIntervalType.INTERVAL_1YEAR -> {
                txStartTime = nowTime.minusYears(1).toInstant()
                splitTime = (Constants.Time.SECONDS_PER_DAY * Constants.Time.MILLS_PER_SEC) // 1 day intervals
            }
            ChartIntervalType.INTERVAL_ALL_TIME -> {
                txStartTime = Instant.ofEpochSecond(balanceHistory.firstOrNull()?.second ?: 0)
                splitTime = -1 // just show full balance history
            }
        }

        var history = mutableListOf<Pair<Long, Long>>()
        if(splitTime == -1) {
            history = balanceHistory.toMutableList()
        } else {
            var currentTime = txStartTime.toEpochMilli()
            var endTime = nowTime.toInstant().toEpochMilli()

            while(currentTime <= endTime) {
                history.add(
                    (getBalanceAtTime(currentTime / Constants.Time.MILLS_PER_SEC, balanceHistory) * Constants.Limit.SATS_PER_BTC).toLong()
                            to (currentTime / Constants.Time.MILLS_PER_SEC)
                )

                currentTime += splitTime
            }
        }

        if (history.size == 1) {
            history += history
        } else if (history.isEmpty() && balanceHistory.isNotEmpty()) {
            history = mutableListOf(
                balanceHistory.last().first to nowTime.toInstant().epochSecond,
                balanceHistory.last().first to nowTime.toInstant().epochSecond
            )
        }

        return history
    }

    private fun generateBalanceHistory(): List<Pair<Long, Long>> {
        val balanceHistory = mutableListOf<Pair<Long, Long>>()
        var balance = 0L
        var incomeFound = false

        walletTransactions.reversed().filter {
            if(it.type == TransactionType.Incoming)
                incomeFound = true
            incomeFound
        }.forEachIndexed { index, it ->
            if(index == 0) {
                balanceHistory.add(0L to it.timestamp)
            }

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
                _showChartError.postValue(null)
                updateWalletBalanceHistory()
            } else {
                _showChartError.postValue(getApplication<PlaidApp>().getString(R.string.no_internet_connection))

                if (hasInternet) {
                    updateWalletBalanceHistory()
                }
            }
        }
    }
}