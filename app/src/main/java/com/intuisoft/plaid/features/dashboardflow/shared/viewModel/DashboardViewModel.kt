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
import com.intuisoft.plaid.common.util.extensions.ignoreNan
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

    protected val _showChartContent = SingleLiveData<Boolean>()
    val showChartContent: LiveData<Boolean> = _showChartContent

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

    protected val _netReturn = SingleLiveData<String>()
    val netReturn: LiveData<String> = _netReturn

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

    private fun getNetProfit(
        satsReceived: Long,
        satsSent: Long,
        totalReceiveCost: Double,
        totalSendCost: Double
    ): Double {
        return totalSendCost - ((satsSent.toDouble() / satsReceived) * totalReceiveCost)
    }

    private fun updateWalletBalanceHistory() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                if(localStoreRepository.isProEnabled()) {
                    if (balanceHistory.isEmpty()) {
                        _noChartData.postValue(Unit)

                        val rate = RateConverter(localStoreRepository.getRateFor(localStoreRepository.getLocalCurrency())?.currentPrice ?: 0.0)
                        rate.setLocalRate(RateConverter.RateType.FIAT_RATE, 0.0)
                        _totalSent.postValue(rate.from(RateConverter.RateType.FIAT_RATE, localStoreRepository.getLocalCurrency()).second!!)
                        _totalReceived.postValue(rate.from(RateConverter.RateType.FIAT_RATE, localStoreRepository.getLocalCurrency()).second!!)
                        _averagePrice.postValue(rate.from(RateConverter.RateType.FIAT_RATE, localStoreRepository.getLocalCurrency()).second!!)
                        _netReturn.postValue(rate.from(RateConverter.RateType.FIAT_RATE, localStoreRepository.getLocalCurrency()).second!!)
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
                        val history = getBalanceHistoryForInterval(intervalType)
                        val rateConverter = RateConverter(
                            localStoreRepository.getRateFor(localStoreRepository.getLocalCurrency())?.currentPrice
                                ?: 0.0
                        )

                        if(localStoreRepository.isProEnabled()) {
                            val fullHistory =
                                getBalanceHistoryForInterval(ChartIntervalType.INTERVAL_ALL_TIME)

                            val allTimeMarketData = apiRepository.getTickerPriceChartData(
                                getMaxMarketInterval(
                                    ChartIntervalType.INTERVAL_ALL_TIME,
                                    Instant.ofEpochSecond(balanceHistory.first().second)
                                )
                            )?.filter {
                                (it.time / Constants.Time.MILLS_PER_SEC) >= fullHistory.first().second
                            }

                            val createdTime =
                                Math.min(walletManager.findStoredWallet(getWalletId())!!.createdAt, balanceHistory.first().second * Constants.Time.MILLS_PER_SEC)
                            val incomingTxs =
                                walletTransactions.filter { it.type == TransactionType.Incoming }
                            val outgoingTxs =
                                walletTransactions.filter { it.type == TransactionType.Outgoing }

                            val totalSent = outgoingTxs
                                .map { tx ->
                                    tx.amount + (tx.fee ?: 0)
                                }.sum()

                            val totalSentCost = outgoingTxs
                                .map { tx ->
                                    (allTimeMarketData?.find { it.time >= tx.timestamp }?.value
                                        ?: 0f) * ((tx.amount.toFloat() + (tx.fee ?: 0)) / Constants.Limit.SATS_PER_BTC)
                                }.sum()

                            val totalReceived = incomingTxs
                                .map { tx ->
                                    tx.amount
                                }.sum()

                            val totalReceivedCost = incomingTxs
                                .map { tx ->
                                    (allTimeMarketData?.find { it.time >= tx.timestamp }?.value
                                        ?: 0f) * (tx.amount.toFloat() / Constants.Limit.SATS_PER_BTC)
                                }.sum()

                            val averagePrice = incomingTxs
                                .map { tx ->
                                    allTimeMarketData?.find { it.time >= tx.timestamp }?.value ?: 0f
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
                                RateConverter.RateType.FIAT_RATE,
                                totalSentCost.toDouble()
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
                                RateConverter.RateType.FIAT_RATE,
                                totalReceivedCost.toDouble()
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

                            rate.setLocalRate(
                                RateConverter.RateType.FIAT_RATE,
                                getNetProfit(
                                    satsReceived = totalReceived,
                                    satsSent = totalSent,
                                    totalReceiveCost = totalReceivedCost.toDouble(),
                                    totalSendCost = totalSentCost.toDouble()
                                )
                            )
                            _netReturn.postValue(
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

                        var gain: Double

                        if (localStoreRepository.getBitcoinDisplayUnit() != BitcoinDisplayUnit.FIAT) {
                            val start = history.first().first
                            val end = history.last().first
                            gain = 100 * ((end.toDouble() - start) / start.toDouble())
                        } else {
                            val data = apiRepository.getTickerPriceChartData(
                                getMaxMarketInterval(
                                    intervalType,
                                    Instant.ofEpochSecond(history.first().second)
                                )
                            )
                                ?.filter {
                                    (it.time / Constants.Time.MILLS_PER_SEC) >= history.first().second
                                }

                            if (data != null && data.isNotEmpty()) {
                                val start =
                                    getBalanceAtTime(data.first().time / Constants.Time.MILLS_PER_SEC) * data.first().value
                                val end =
                                    getBalanceAtTime(data.last().time / Constants.Time.MILLS_PER_SEC) * data.last().value
                                gain = 100 * ((end.toDouble() - start) / start.toDouble())
                            } else {
                                gain = 0.0
                            }
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
                                
                                _showChartContent.postValue(true)
                                val beginValue = history.firstOrNull()?.first ?: 0L
                                val endValue = history.lastOrNull()?.first ?: 0L
                                _percentageGain.postValue(gain.ignoreNan() to (endValue - beginValue).toDouble())
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
                                val beginValue = history.firstOrNull()?.first ?: 0L
                                val endValue = history.lastOrNull()?.first ?: 0L
                                _percentageGain.postValue(gain.ignoreNan() to (endValue - beginValue).toDouble())
                            }

                            BitcoinDisplayUnit.FIAT -> {
                                val data = apiRepository.getTickerPriceChartData(getMaxMarketInterval(intervalType, Instant.ofEpochSecond(history.first().second)))
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
                                    val beginValue = getBalanceAtTime(data.first().time / Constants.Time.MILLS_PER_SEC) * data.first().value
                                    val endValue =  getBalanceAtTime(data.last().time / Constants.Time.MILLS_PER_SEC) * data.last().value
                                    _percentageGain.postValue(gain.ignoreNan() to (endValue - beginValue).toDouble())
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

    private fun getMaxMarketInterval(interval: ChartIntervalType, birthdate: Instant): ChartIntervalType {
        val nowTime = SimpleTimeFormat.startOfDay(ZonedDateTime.ofInstant(Instant.now(), ZoneId.systemDefault()))
        val startOfBDay  = SimpleTimeFormat.startOfDay(ZonedDateTime.ofInstant(birthdate, ZoneId.systemDefault())).toEpochSecond()

        when {
            interval == ChartIntervalType.INTERVAL_1DAY ||
                    startOfBDay >= nowTime.minusDays(1).toEpochSecond() -> {
                return ChartIntervalType.INTERVAL_1DAY
            }
            interval == ChartIntervalType.INTERVAL_1WEEK ||
                    startOfBDay  >= nowTime.minusWeeks(1).toEpochSecond() -> {
                return ChartIntervalType.INTERVAL_1WEEK
            }
            interval == ChartIntervalType.INTERVAL_1MONTH ||
                    startOfBDay >= nowTime.minusMonths(1).toEpochSecond() -> {
                return ChartIntervalType.INTERVAL_1MONTH
            }
            interval == ChartIntervalType.INTERVAL_3MONTHS ||
                    startOfBDay >= nowTime.minusMonths(3).toEpochSecond() -> {
                return ChartIntervalType.INTERVAL_3MONTHS
            }
            interval == ChartIntervalType.INTERVAL_6MONTHS ||
                    startOfBDay >= nowTime.minusMonths(6).toEpochSecond() -> {
                return ChartIntervalType.INTERVAL_6MONTHS
            }
            interval == ChartIntervalType.INTERVAL_1YEAR ||
                    startOfBDay >= nowTime.minusYears(1).toEpochSecond() -> {
                return ChartIntervalType.INTERVAL_1YEAR
            }
            else -> {
                return interval
            }
        }

    }

    private fun getBalanceHistoryForInterval(interval: ChartIntervalType): List<Pair<Long, Long>> {
        val txStartTime: Instant

        when(interval) {
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