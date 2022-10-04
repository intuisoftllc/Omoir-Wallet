package com.intuisoft.plaid.features.settings.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import com.intuisoft.plaid.androidwrappers.BaseViewModel
import com.intuisoft.plaid.androidwrappers.SingleLiveData
import com.intuisoft.plaid.local.UserPreferences
import com.intuisoft.plaid.model.BitcoinDisplayUnit

class SettingsViewModel(
    application: Application,
    private val userPreferences: UserPreferences
): BaseViewModel(application) {

    private val _bitcoinDisplayUnitSetting = SingleLiveData<BitcoinDisplayUnit>()
    val bitcoinDisplayUnitSetting: LiveData<BitcoinDisplayUnit> = _bitcoinDisplayUnitSetting

    fun updateSettingsScreen() {
        _bitcoinDisplayUnitSetting.postValue(userPreferences.bitcoinDisplayUnit)
    }

    fun saveDisplayUnit(unit: BitcoinDisplayUnit) {
        userPreferences.bitcoinDisplayUnit = unit
    }
}