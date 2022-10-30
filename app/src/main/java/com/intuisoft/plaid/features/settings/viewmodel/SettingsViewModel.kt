package com.intuisoft.plaid.features.settings.viewmodel

import android.app.Application
import android.content.Context
import android.os.Build
import androidx.lifecycle.LiveData
import com.intuisoft.plaid.BuildConfig
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.BaseViewModel
import com.intuisoft.plaid.androidwrappers.SingleLiveData
import com.intuisoft.plaid.local.UserPreferences
import com.intuisoft.plaid.model.AppTheme
import com.intuisoft.plaid.model.BitcoinDisplayUnit
import com.intuisoft.plaid.repositories.LocalStoreRepository
import com.intuisoft.plaid.util.Constants
import com.intuisoft.plaid.walletmanager.AbstractWalletManager
import com.intuisoft.plaid.walletmanager.WalletManager

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

    private val _nameSetting = SingleLiveData<String>()
    val nameSetting: LiveData<String> = _nameSetting

    private val _showEasterEgg = SingleLiveData<Unit>()
    val showEasterEgg: LiveData<Unit> = _showEasterEgg

    fun getMaxPinAttempts() = localStoreRepository.getPinEntryLimit()
    fun getPinTimeout() = localStoreRepository.getPinTimeout()
    fun getDisplayUnit() = localStoreRepository.getBitcoinDisplayUnit()
    fun getName() = localStoreRepository.getUserAlias()

    fun updateSettingsScreen() {
        updateDisplayUnitSetting()
        updateAppThemeSetting()
        updatePinTimeoutSetting()
        updateFingerprintRegisteredSetting()
        updateAppVersionSetting()
        updateNameSetting()
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