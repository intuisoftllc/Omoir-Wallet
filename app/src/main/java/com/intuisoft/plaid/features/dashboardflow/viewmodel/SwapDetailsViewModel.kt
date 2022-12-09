package com.intuisoft.plaid.features.dashboardflow.viewmodel

import android.app.Application
import android.widget.ImageView
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.intuisoft.plaid.PlaidApp
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.SingleLiveData
import com.intuisoft.plaid.androidwrappers.WalletViewModel
import com.intuisoft.plaid.common.repositories.ApiRepository
import com.intuisoft.plaid.common.repositories.LocalStoreRepository
import com.intuisoft.plaid.common.util.Constants
import com.intuisoft.plaid.common.util.RateConverter
import com.intuisoft.plaid.walletmanager.AbstractWalletManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class SwapDetailsViewModel(
    application: Application,
    private val apiRepository: ApiRepository,
    private val localStoreRepository: LocalStoreRepository,
    private val walletManager: AbstractWalletManager
): WalletViewModel(application, localStoreRepository, apiRepository, walletManager) {

    protected val _priceConversion = SingleLiveData<String>()
    val priceConversion: LiveData<String> = _priceConversion

    protected val _copyData = SingleLiveData<Pair<ImageView, String?>>()
    val copyData: LiveData<Pair<ImageView, String?>> = _copyData

    fun updatePriceConversion(receiveValue: Double) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val rate = apiRepository.getRateFor(localStoreRepository.getLocalCurrency())
                val rateConverter = RateConverter(rate.currentPrice)
                rateConverter.setLocalRate(RateConverter.RateType.BTC_RATE, receiveValue)


                if(rate.currentPrice != 0.0) {
                    _priceConversion.postValue(rateConverter.from(RateConverter.RateType.FIAT_RATE, localStoreRepository.getLocalCurrency()).second!!)
                } else {
                    _priceConversion.postValue(getApplication<PlaidApp>().getString(R.string.not_applicable))
                }
            }
        }
    }

    fun copyDataItemClicked(copyIcon: ImageView, data: String) {
        viewModelScope.launch {
            _copyData.postValue(copyIcon to data)
            delay(Constants.Time.ITEM_COPY_DELAY.toLong())
            _copyData.postValue(copyIcon to null)
        }
    }
}