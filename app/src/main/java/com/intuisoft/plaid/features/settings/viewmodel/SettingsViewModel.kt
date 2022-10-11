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
import com.intuisoft.plaid.repositories.LocalStoreRepository
import com.intuisoft.plaid.util.Constants
import com.intuisoft.plaid.walletmanager.WalletManager

class SettingsViewModel(
    application: Application,
    private val localStoreRepository: LocalStoreRepository,
    private val walletManager: WalletManager
): BaseViewModel(application, localStoreRepository, walletManager) {

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

    private val _showEasterEgg = SingleLiveData<Unit>()
    val showEasterEgg: LiveData<Unit> = _showEasterEgg

    fun getMaxPinAttempts() = localStoreRepository.getPinEntryLimit()
    fun getPinTimeout() = localStoreRepository.getPinTimeout()

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

    fun onVersionTapped() {
        if(localStoreRepository.versionTapLimitReached()) {
            _showEasterEgg.postValue(Unit)
        } else {
            localStoreRepository.updateVersionTappedCount()
        }
    }

    fun updateDisplayUnitSetting() {
        _bitcoinDisplayUnitSetting.postValue(localStoreRepository.getBitcoinDisplayUnit())
    }

    fun updateFingerprintRegisteredSetting() {
        _fingerprintRegistered.postValue(localStoreRepository.isFingerprintEnabled())
    }

    fun updateAppThemeSetting() {
        _appThemeSetting.postValue(localStoreRepository.getAppTheme())
    }

    fun updatePinTimeoutSetting() {
        _pinTimeoutSetting.postValue(localStoreRepository.getPinTimeout())
    }

    fun updateAppVersionSetting() {
        _appVersionSetting.postValue("v${BuildConfig.VERSION_NAME}")
    }

    fun saveAppTheme(theme: AppTheme) {
        localStoreRepository.updateAppTheme(theme)
        updateAppThemeSetting()
    }

    fun savePinTimeout(timeout: Int) {
        localStoreRepository.updatePinTimeout(timeout)
        updatePinTimeoutSetting()
    }

    fun saveDisplayUnit(unit: BitcoinDisplayUnit) {
        localStoreRepository.updateBitcoinDisplayUnit(unit)
        updateDisplayUnitSetting()
    }

    fun saveMaxPinAttempts(limit: Int) {
        localStoreRepository.setMaxPinEntryLimit(limit)
    }

    fun saveFingerprintRegistered(registered: Boolean) {
        localStoreRepository.setFingerprintEnabled(registered)
    }
}