package com.intuisoft.plaid.features.settings.viewmodel

import android.app.Application
import android.os.Build
import androidx.lifecycle.LiveData
import com.intuisoft.plaid.BuildConfig
import com.intuisoft.plaid.androidwrappers.BaseViewModel
import com.intuisoft.plaid.androidwrappers.SingleLiveData
import com.intuisoft.plaid.local.UserPreferences
import com.intuisoft.plaid.model.AppTheme
import com.intuisoft.plaid.model.BitcoinDisplayUnit
import com.intuisoft.plaid.util.Constants

class SettingsViewModel(
    application: Application,
    private val userPreferences: UserPreferences
): BaseViewModel(application, userPreferences) {

    private val _bitcoinDisplayUnitSetting = SingleLiveData<BitcoinDisplayUnit>()
    val bitcoinDisplayUnitSetting: LiveData<BitcoinDisplayUnit> = _bitcoinDisplayUnitSetting

    private val _appThemeSetting = SingleLiveData<AppTheme>()
    val appThemeSetting: LiveData<AppTheme> = _appThemeSetting

    private val _pinTimeoutSetting = SingleLiveData<Int>()
    val pinTimeoutSetting: LiveData<Int> = _pinTimeoutSetting

    private val _fingerprintRegistered = SingleLiveData<Boolean>()
    val fingerprintRegistered: LiveData<Boolean> = _fingerprintRegistered

    private val _appVersionSetting = SingleLiveData<String>()
    val appVersionSetting: LiveData<String> = _appVersionSetting

    fun getMaxPinAttempts() = userPreferences.maxPinAttempts
    fun getPinTimeout() = userPreferences.pinTimeout
    fun isFingerprintEnabled() = userPreferences.fingerprintSecurity

    fun updateSettingsScreen() {
        updateDisplayUnitSetting()
        updateAppThemeSetting()
        updatePinTimeoutSetting()
        updateFingerprintRegisteredSetting()
        updateAppVersionSetting()
    }

    fun pinTimeoutToString(timeout: Int) : String =
        when(timeout) {
            Constants.Time.ONE_MINUTE -> {
                Constants.Strings.STRING_1_MINUTE_TIMEOUT
            }
            Constants.Time.TWO_MINUTES -> {
                Constants.Strings.STRING_2_MINUTE_TIMEOUT
            }
            Constants.Time.FIVE_MINUTES -> {
                Constants.Strings.STRING_5_MINUTE_TIMEOUT
            }
            Constants.Time.TEN_MINUTES -> {
                Constants.Strings.STRING_10_MINUTE_TIMEOUT
            }
            else -> {
                Constants.Strings.STRING_IMMEDIATE_TIMEOUT
            }
        }

    fun updateDisplayUnitSetting() {
        _bitcoinDisplayUnitSetting.postValue(userPreferences.bitcoinDisplayUnit)
    }

    fun updateFingerprintRegisteredSetting() {
        _fingerprintRegistered.postValue(userPreferences.fingerprintSecurity)
    }

    fun updateAppThemeSetting() {
        _appThemeSetting.postValue(userPreferences.appTheme)
    }

    fun updatePinTimeoutSetting() {
        _pinTimeoutSetting.postValue(userPreferences.pinTimeout)
    }

    fun updateAppVersionSetting() {
        _appVersionSetting.postValue("v${BuildConfig.VERSION_NAME}")
    }

    fun saveAppTheme(theme: AppTheme) {
        userPreferences.appTheme = theme
        updateAppThemeSetting()
    }

    fun savePinTimeout(timeout: Int) {
        if(timeout == Constants.Time.INSTANT) {
            userPreferences.pinTimeout = Constants.Time.INSTANT_TIME_OFFSET
        } else {
            userPreferences.pinTimeout = timeout
        }

        updatePinTimeoutSetting()
    }

    fun saveDisplayUnit(unit: BitcoinDisplayUnit) {
        userPreferences.bitcoinDisplayUnit = unit
        updateDisplayUnitSetting()
    }

    fun saveMaxPinAttempts(limit: Int) {
        userPreferences.maxPinAttempts = limit
    }

    fun saveFingerprintRegistered(registered: Boolean) {
        userPreferences.fingerprintSecurity = registered
    }
}