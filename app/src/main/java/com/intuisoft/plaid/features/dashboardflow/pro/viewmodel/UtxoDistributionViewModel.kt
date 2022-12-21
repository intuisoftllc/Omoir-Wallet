package com.intuisoft.plaid.features.dashboardflow.pro.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.intuisoft.plaid.PlaidApp
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.SingleLiveData
import com.intuisoft.plaid.androidwrappers.WalletViewModel
import com.intuisoft.plaid.common.repositories.ApiRepository
import com.intuisoft.plaid.common.repositories.LocalStoreRepository
import com.intuisoft.plaid.common.util.RateConverter
import com.intuisoft.plaid.common.util.extensions.median
import com.intuisoft.plaid.walletmanager.AbstractWalletManager
import io.horizontalsystems.bitcoincore.storage.UnspentOutput
import kotlinx.coroutines.launch


class UtxoDistributionViewModel(
    application: Application,
    private val apiRepository: ApiRepository,
    private val localStoreRepository: LocalStoreRepository,
    private val walletManager: AbstractWalletManager
): WalletViewModel(application, localStoreRepository, apiRepository, walletManager) {

    protected val _noData = SingleLiveData<Boolean>()
    val noData: LiveData<Boolean> = _noData

    protected val _barData = SingleLiveData<BarData?>()
    val barData: LiveData<BarData?> = _barData

    protected val _unspentOutputs = SingleLiveData<List<UnspentOutput>>()
    val unspentOutputs: LiveData<List<UnspentOutput>> = _unspentOutputs

    protected val _median = SingleLiveData<String>()
    val median: LiveData<String> = _median

    private var utxos: List<UnspentOutput> = listOf()
    private var data: BarData? = null

    fun onBarSelected(selected: Boolean, index: Int) {
        data?.apply {
            val bar = items[index]

            if(selected) {
                val rate = RateConverter(
                    0.0
                )
                rate.setLocalRate(RateConverter.RateType.SATOSHI_RATE,
                    bar.utxos.map { it.output.value }.median()
                )

                setMedian(
                    rate.from(
                        RateConverter.RateType.SATOSHI_RATE,
                        ""
                    ).second
                )

                _unspentOutputs.postValue(bar.utxos)
            } else {
                setMedian(
                    median
                )

                _unspentOutputs.postValue(utxos)
            }
        }
    }

    fun getCoins() {
        viewModelScope.launch {
            utxos = getUnspentOutputs().sortedByDescending { it.output.value }
            val rate = RateConverter(
                0.0
            )

            if(utxos.isNotEmpty()) {
                _noData.postValue(false)
                val _0_1k = utxos.filter { it.output.value in 0..999 }
                val _1k_10k = utxos.filter { it.output.value in 1000..9_999 }
                val _10k_100k = utxos.filter { it.output.value in 10_000..99_999 }
                val _100k_1m = utxos.filter { it.output.value in 100_000..999_999 }
                val _1m_10m = utxos.filter { it.output.value in 1_000_000..9_999_999 }
                val _10m_100m = utxos.filter { it.output.value in 10_000_000..99_999_999 }
                val _1btc = utxos.filter { it.output.value > 100_000_000 }

                rate.setLocalRate(RateConverter.RateType.SATOSHI_RATE,
                    (_0_1k.map { it.output.value } +
                        _1k_10k.map { it.output.value } +
                        _10k_100k.map { it.output.value } +
                        _100k_1m.map { it.output.value } +
                        _1m_10m.map { it.output.value } +
                        _10m_100m.map { it.output.value } +
                        _1btc.map { it.output.value }).median()
                )

                data = BarData(
                    items = listOf(
                        BarItem(
                            barName = getApplication<PlaidApp>().getString(R.string.report_details_utxo_range_1),
                            utxos = _0_1k
                        ),
                        BarItem(
                            barName = getApplication<PlaidApp>().getString(R.string.report_details_utxo_range_2),
                            utxos = _1k_10k
                        ),
                        BarItem(
                            barName = getApplication<PlaidApp>().getString(R.string.report_details_utxo_range_3),
                            utxos = _10k_100k
                        ),
                        BarItem(
                            barName = getApplication<PlaidApp>().getString(R.string.report_details_utxo_range_4),
                            utxos = _100k_1m
                        ),
                        BarItem(
                            barName = getApplication<PlaidApp>().getString(R.string.report_details_utxo_range_5),
                            utxos = _1m_10m
                        ),
                        BarItem(
                            barName = getApplication<PlaidApp>().getString(R.string.report_details_utxo_range_6),
                            utxos = _10m_100m
                        ),
                        BarItem(
                            barName = getApplication<PlaidApp>().getString(R.string.report_details_utxo_range_7),
                            utxos = _1btc
                        )
                    ),
                    median = rate.from(RateConverter.RateType.SATOSHI_RATE, "").second
                )

                _barData.postValue(
                    data
                )

                _unspentOutputs.postValue(utxos)
            } else {
                _noData.postValue(true)
                data = null
                _unspentOutputs.postValue(listOf())
            }


            setMedian(
                rate.from(
                    RateConverter.RateType.SATOSHI_RATE,
                    ""
                ).second
            )
        }
    }

    private fun setMedian(total: String) {
        _median.postValue(total)
    }

    data class BarData(
        val items: List<BarItem>,
        val median: String,
    )

    data class BarItem(
        val barName: String,
        val utxos: List<UnspentOutput>
    )
}