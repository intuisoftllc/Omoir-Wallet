package com.intuisoft.plaid.features.settings.viewmodel

import android.app.Application
import android.content.Context
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import com.intuisoft.plaid.BuildConfig
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.BaseViewModel
import com.intuisoft.plaid.androidwrappers.SingleLiveData
import com.intuisoft.plaid.androidwrappers.navigate
import com.intuisoft.plaid.common.model.AppMode
import com.intuisoft.plaid.common.model.AppTheme
import com.intuisoft.plaid.common.model.BitcoinDisplayUnit
import com.intuisoft.plaid.common.repositories.LocalStoreRepository
import com.intuisoft.plaid.common.util.Constants
import com.intuisoft.plaid.walletmanager.AbstractWalletManager

class SettingsViewModel(
    application: Application,
    private val localStoreRepository: LocalStoreRepository,
    private val walletManager: AbstractWalletManager
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

    private val _localCurrencySetting = SingleLiveData<String>()
    val localCurrencySetting: LiveData<String> = _localCurrencySetting

    private val _nameSetting = SingleLiveData<String>()
    val nameSetting: LiveData<String> = _nameSetting

    private val _showEasterEgg = SingleLiveData<Unit>()
    val showEasterEgg: LiveData<Unit> = _showEasterEgg

    fun getMaxPinAttempts() = localStoreRepository.getPinEntryLimit()
    fun getPinTimeout() = localStoreRepository.getPinTimeout()
    fun getDisplayUnit() = localStoreRepository.getBitcoinDisplayUnit()
    fun getName() = localStoreRepository.getUserAlias()
    fun getMinConfirmations() = localStoreRepository.getMinimumConfirmations()
    fun versionTapLimitReached() = localStoreRepository.versionTapLimitReached()

    var appRestartNeeded = false

    fun restartApp(fragment: Fragment) {
        appRestartNeeded = false
        softRestart(fragment)
    }

    fun saveMinimumConfirmation(min: Int) {
        localStoreRepository.setMinConfirmations(min)
    }

    fun updateSettingsScreen() {
        updateDisplayUnitSetting()
        updateAppThemeSetting()
        updatePinTimeoutSetting()
        updateFingerprintRegisteredSetting()
        updateAppVersionSetting()
        updateNameSetting()
        updateLocalCurrencySetting()
    }

    fun pinTimeoutToString(context: Context, timeout: Int) : String =
        when(timeout) {
            Constants.Time.ONE_MINUTE -> {
                context.getString(R.string.settings_option_max_pin_timeout_variant_2)
            }
            Constants.Time.TWO_MINUTES -> {
                context.getString(R.string.settings_option_max_pin_timeout_variant_3)
            }
            Constants.Time.FIVE_MINUTES -> {
                context.getString(R.string.settings_option_max_pin_timeout_variant_4)
            }
            Constants.Time.TEN_MINUTES -> {
                context.getString(R.string.settings_option_max_pin_timeout_variant_5)
            }
            else -> {
                context.getString(R.string.settings_option_max_pin_timeout_variant_1)
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

    fun updateLocalCurrencySetting() {
        _localCurrencySetting.postValue(localStoreRepository.getLocalCurrency())
    }

    fun updatePinTimeoutSetting() {
        _pinTimeoutSetting.postValue(localStoreRepository.getPinTimeout())
    }

    fun updateNameSetting() {
        _nameSetting.postValue(localStoreRepository.getUserAlias())
    }

    fun updateAppVersionSetting() {
        _appVersionSetting.postValue("v${BuildConfig.VERSION_NAME}")
    }

    fun saveName(name: String) {
        localStoreRepository.updateUserAlias(name)
        updateNameSetting()
    }

    fun saveLocalCurrency(localCurrency: String) {
        localStoreRepository.setLocalCurrency(localCurrency)
        updateLocalCurrencySetting()
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