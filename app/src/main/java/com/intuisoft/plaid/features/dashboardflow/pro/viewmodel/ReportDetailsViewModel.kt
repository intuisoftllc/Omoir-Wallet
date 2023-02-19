package com.intuisoft.plaid.features.dashboardflow.pro.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.intuisoft.plaid.androidwrappers.SingleLiveData
import com.intuisoft.plaid.androidwrappers.WalletViewModel
import com.intuisoft.plaid.common.model.BitcoinDisplayUnit
import com.intuisoft.plaid.common.model.ReportHistoryTimeFilter
import com.intuisoft.plaid.common.model.ReportType
import com.intuisoft.plaid.common.repositories.ApiRepository
import com.intuisoft.plaid.common.repositories.LocalStoreRepository
import com.intuisoft.plaid.common.util.RateConverter
import com.intuisoft.plaid.common.util.SimpleTimeFormat
import com.intuisoft.plaid.walletmanager.AbstractWalletManager
import io.horizontalsystems.bitcoincore.models.TransactionInfo
import io.horizontalsystems.bitcoincore.models.TransactionStatus
import io.horizontalsystems.bitcoincore.models.TransactionType
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime


class ReportDetailsViewModel(
    application: Application,
    private val apiRepository: ApiRepository,
    private val localStoreRepository: LocalStoreRepository,
    private val walletManager: AbstractWalletManager
): WalletViewModel(application, localStoreRepository, apiRepository, walletManager) {

    protected val _currentTimePeriod = SingleLiveData<Pair<Instant, Instant>>()
    val currentTimePeriod: LiveData<Pair<Instant, Instant>> = _currentTimePeriod

    protected val _onFilterUpdate = SingleLiveData<ReportHistoryTimeFilter>()
    val onFilterUpdate: LiveData<ReportHistoryTimeFilter> = _onFilterUpdate

    protected val _noData = SingleLiveData<Boolean>()
    val noData: LiveData<Boolean> = _noData

    protected val _gettingData = SingleLiveData<Boolean>()
    val gettingData: LiveData<Boolean> = _gettingData

    protected val _total = SingleLiveData<String>()
    val total: LiveData<String> = _total

    protected val _barData = SingleLiveData<BarData?>()
    val barData: LiveData<BarData?> = _barData

    protected val _contentEanbled = SingleLiveData<Boolean>()
    val contentEanbled: LiveData<Boolean> = _contentEanbled

    private var filter: ReportHistoryTimeFilter = ReportHistoryTimeFilter.LAST_WEEK
    private var walletTransactions: List<TransactionInfo> = listOf()
    private val disposables = CompositeDisposable()
    private var data: BarData? = null
    var type: ReportType = ReportType.FEE_REPORT

    fun getFilter() = filter

    fun setFilter(filter: ReportHistoryTimeFilter) {
        viewModelScope.launch {
            this@ReportDetailsViewModel.filter = filter
            data = null
            _contentEanbled.postValue(false)
            _noData.postValue(false)
            _barData.postValue(null)
            _gettingData.postValue(true)
            val tx = walletTransactions.toMutableList()

            val txStartTime: Instant
            val filtered: List<TransactionInfo>

            val rate = RateConverter(
                localStoreRepository.getRateFor(localStoreRepository.getLocalCurrency())?.currentPrice ?: 0.0
            )

            when(filter) {
                ReportHistoryTimeFilter.LAST_WEEK -> {
                    txStartTime = ZonedDateTime.now().minusWeeks(1).toInstant()
                }
                ReportHistoryTimeFilter.LAST_MONTH -> {
                    txStartTime = ZonedDateTime.now().minusMonths(1).toInstant()
                }
                ReportHistoryTimeFilter.LAST_6MONTHS -> {
                    txStartTime = ZonedDateTime.now().minusMonths(6).toInstant()
                }
                ReportHistoryTimeFilter.LAST_YEAR -> {
                   txStartTime = ZonedDateTime.now().minusYears(1).toInstant()
                }
                ReportHistoryTimeFilter.ALL_TIME -> {
                    txStartTime = Instant.ofEpochSecond(tx.lastOrNull()?.timestamp ?: 0)
                }
            }

            when(type) {
                ReportType.INFLOW_REPORT -> {
                    filtered = tx.filter {
                        Instant.ofEpochSecond(it.timestamp).epochSecond >= txStartTime.epochSecond && it.type == TransactionType.Incoming
                    }
                }
                ReportType.OUTFLOW_REPORT -> {
                    filtered = tx.filter {
                        Instant.ofEpochSecond(it.timestamp).epochSecond >= txStartTime.epochSecond && it.type == TransactionType.Outgoing
                    }
                }
                ReportType.FEE_REPORT -> {
                    filtered = tx.filter {
                        Instant.ofEpochSecond(it.timestamp).epochSecond >= txStartTime.epochSecond && (it.type == TransactionType.Outgoing || it.type == TransactionType.SentToSelf)
                    }
                }
            }

            if(filtered.isNotEmpty()) {
                val periods = SimpleTimeFormat.getTimePeriodsFor(
                    filter,
                    ZonedDateTime.ofInstant(
                        Instant.ofEpochSecond(tx.last().timestamp),
                        ZoneId.systemDefault()
                    )
                )

                val total = rate.clone()

                data = BarData(
                    items = periods.map { (barName, timePeriod) ->
                        val txs = filtered.filter {
                            it.timestamp >= timePeriod.first.epochSecond && it.timestamp <= timePeriod.second.epochSecond
                        }

                        val value = txs.map {
                            if(type != ReportType.FEE_REPORT) it.amount
                            else it.fee ?: 0
                        }.sum().toDouble()

                        total.setLocalRate(RateConverter.RateType.SATOSHI_RATE, total.getRawRate() + value)

                        BarItem(
                            barName = barName,
                            transactions = txs,
                            value =
                                rate.setLocalRate(
                                    RateConverter.RateType.SATOSHI_RATE,
                                    value
                                ).clone(),
                            timePeriod = timePeriod
                        )
                    },
                    total = total.from(localStoreRepository.getBitcoinDisplayUnit().toRateType(), localStoreRepository.getLocalCurrency()).second,
                    timePeriod = periods.first().second.first to periods.last().second.second
                )

                setTimePeriod(periods.first().second.first, periods.last().second.second)
                setTotal(data!!.total)
                _gettingData.postValue(false)
                _noData.postValue(false)
                _barData.postValue(data)
                _transactions.postValue(filtered)
            } else {
                setTimePeriod(txStartTime, Instant.now())
                _gettingData.postValue(false)
                setTotal(rate.from(localStoreRepository.getBitcoinDisplayUnit().toRateType(), localStoreRepository.getLocalCurrency()).second)
                _noData.postValue(true)
                enableContent()
                _transactions.postValue(listOf())
            }

            _onFilterUpdate.postValue(filter)
        }

    }

    fun changeDisplayUnit() {
        when(localStoreRepository.getBitcoinDisplayUnit()) {
            BitcoinDisplayUnit.BTC -> {
                localStoreRepository.updateBitcoinDisplayUnit(BitcoinDisplayUnit.SATS)
            }

            BitcoinDisplayUnit.SATS -> {
                localStoreRepository.updateBitcoinDisplayUnit(BitcoinDisplayUnit.FIAT)
            }

            BitcoinDisplayUnit.FIAT -> {
                localStoreRepository.updateBitcoinDisplayUnit(BitcoinDisplayUnit.BTC)
            }
        }

        setFilter(filter)
    }

    fun enableContent() {
        _contentEanbled.postValue(true)
    }

    fun onBarSelected(selected: Boolean, index: Int) {
        data?.apply {
            val bar = items[index]

            if(selected) {
                setTotal(
                    bar.value.from(
                        localStoreRepository.getBitcoinDisplayUnit().toRateType(),
                        localStoreRepository.getLocalCurrency()
                    ).second
                )

                setTimePeriod(bar.timePeriod.first, bar.timePeriod.second)
                _transactions.postValue(bar.transactions)
            } else {
                setTotal(
                    total
                )

                setTimePeriod(timePeriod.first, timePeriod.second)
                _transactions.postValue(items.flatMap { it.transactions })
            }
        }
    }

    fun changeTimePeriod() {
        when(filter) {
            ReportHistoryTimeFilter.LAST_WEEK -> {
                setFilter(ReportHistoryTimeFilter.LAST_MONTH)
            }
            ReportHistoryTimeFilter.LAST_MONTH -> {
                setFilter(ReportHistoryTimeFilter.LAST_6MONTHS)
            }
            ReportHistoryTimeFilter.LAST_6MONTHS -> {
                setFilter(ReportHistoryTimeFilter.LAST_YEAR)
            }
            ReportHistoryTimeFilter.LAST_YEAR -> {
                setFilter(ReportHistoryTimeFilter.ALL_TIME)
            }
            ReportHistoryTimeFilter.ALL_TIME -> {
                setFilter(ReportHistoryTimeFilter.LAST_WEEK)
            }
        }
    }

    fun setTimePeriod(start: Instant, end: Instant) {
        _currentTimePeriod.postValue(start to end)
    }

    fun setTotal(total: String) {
        _total.postValue(total)
    }

    fun setupSubscriptions() {
        localWallet!!.walletKit!!.transactions(type = null).subscribe { txList: List<TransactionInfo> ->
            val blacklist = localStoreRepository.getAllBlacklistedTransactions(getWalletId())
            walletTransactions = txList.filter { tx ->
                tx.status != TransactionStatus.INVALID
                        && blacklist.find { tx.transactionHash == it.txId } == null
            }
            setFilter(filter)
        }.let {
            disposables.add(it)
        }
    }

    data class BarData(
        val items: List<BarItem>,
        val total: String,
        val timePeriod: Pair<Instant, Instant>
    )

    data class BarItem(
        val barName: String,
        val transactions: List<TransactionInfo>,
        val value: RateConverter,
        val timePeriod: Pair<Instant, Instant>
    )

    override fun onCleared() {
        super.onCleared()
        disposables.dispose()
    }
}