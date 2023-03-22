package com.intuisoft.plaid.features.settings.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.LiveData
import com.intuisoft.plaid.BuildConfig
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.BaseViewModel
import com.intuisoft.plaid.androidwrappers.SingleLiveData
import com.intuisoft.plaid.common.analytics.EventTracker
import com.intuisoft.plaid.common.analytics.events.EventDisableUsageData
import com.intuisoft.plaid.common.analytics.events.EventEnableUsageData
import com.intuisoft.plaid.common.coroutines.OmoirScope
import com.intuisoft.plaid.common.model.AppTheme
import com.intuisoft.plaid.common.model.BitcoinDisplayUnit
import com.intuisoft.plaid.common.repositories.LocalStoreRepository
import com.intuisoft.plaid.common.util.Constants
import com.intuisoft.plaid.common.util.extensions.safeWalletScope
import com.intuisoft.plaid.walletmanager.AbstractWalletManager
import kotlinx.coroutines.launch

class SettingsViewModel(
    application: Application,
    private val localStoreRepository: LocalStoreRepository,
    private val walletManager: AbstractWalletManager,
    private val eventTracker: EventTracker
): BaseViewModel(application, localStoreRepository, walletManager) {

    private val _bitcoinDisplayUnitSetting = SingleLiveData<BitcoinDisplayUnit>()
    val bitcoinDisplayUnitSetting: LiveData<BitcoinDisplayUnit> = _bitcoinDisplayUnitSetting

    private val _appThemeSetting = SingleLiveData<AppTheme>()
    val appThemeSetting: LiveData<AppTheme> = _appThemeSetting

    private val _pinTimeoutSetting = SingleLiveData<Int>()
    val pinTimeoutSetting: LiveData<Int> = _pinTimeoutSetting

    private val _fingerprintRegistered = SingleLiveData<Boolean>()
    val fingerprintRegistered: LiveData<Boolean> = _fingerprintRegistered

    private val _sendUsageData = SingleLiveData<Boolean>()
    val sendUsageData: LiveData<Boolean> = _sendUsageData

    private val _hideHiddenWallets = SingleLiveData<Boolean>()
    val hideHiddenWallets: LiveData<Boolean> = _hideHiddenWallets

    private val _appVersionSetting = SingleLiveData<String>()
    val appVersionSetting: LiveData<String> = _appVersionSetting

    private val _localCurrencySetting = SingleLiveData<String>()
    val localCurrencySetting: LiveData<String> = _localCurrencySetting

    private val _nameSetting = SingleLiveData<String>()
    val nameSetting: LiveData<String> = _nameSetting

    private val _showDeveloperSetting = SingleLiveData<Unit>()
    val showDeveloperSetting: LiveData<Unit> = _showDeveloperSetting

    private val _showStepsLeftToDeveloper = SingleLiveData<Int>()
    val showStepsLeftToDeveloper: LiveData<Int> = _showStepsLeftToDeveloper

    private val _showMemeFragment = SingleLiveData<Unit>()
    val showMemeFragment: LiveData<Unit> = _showMemeFragment

    private val _showDeveloperOptions = SingleLiveData<Unit>()
    val showDeveloperOptions: LiveData<Unit> = _showDeveloperOptions

    fun getMaxPinAttempts() = localStoreRepository.getPinEntryLimit()
    fun getPinTimeout() = localStoreRepository.getPinTimeout()
    fun getDisplayUnit() = localStoreRepository.getBitcoinDisplayUnit()
    fun getName() = localStoreRepository.getUserAlias()
    fun getMinConfirmations() = localStoreRepository.getMinimumConfirmations()
    fun versionTapLimitReached() = localStoreRepository.isDeveloper()

    var appRestartNeeded = false

    fun saveMinimumConfirmation(min: Int) {
        localStoreRepository.setMinConfirmations(min)
    }

    fun updateSettingsScreen() {
        OmoirScope.MainScope.launch {
            safeWalletScope {
                updateDisplayUnitSetting()
                updateAppThemeSetting()
                updatePinTimeoutSetting()
                updateFingerprintRegisteredSetting()
                updateAppVersionSetting()
                updateNameSetting()
                updateLocalCurrencySetting()
                updateHideHiddenWalletsSetting()
                updateSendUsageDataSetting()
                if(localStoreRepository.isDeveloper()) {
                    _showDeveloperSetting.postValue(Unit)
                }
            }
        }
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
        if(localStoreRepository.isDeveloper()) return
        localStoreRepository.updateStepsLeftToDeveloper()

        if(localStoreRepository.isDeveloper()) {
            _showStepsLeftToDeveloper.postValue(0)
            _showDeveloperSetting.postValue(Unit)
        } else {
            if(localStoreRepository.stepsLeftToDeveloper() <= 3) {
                _showStepsLeftToDeveloper.postValue(localStoreRepository.stepsLeftToDeveloper())
            }
        }
    }

    fun onDeveloperOptionsClicked() {
        if(localStoreRepository.hasDeveloperAccess()) {
            _showDeveloperOptions.postValue(Unit)
        } else {
            _showMemeFragment.postValue(Unit)
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

    fun updateHideHiddenWalletsSetting() {
        _hideHiddenWallets.postValue(localStoreRepository.isHidingHiddenWalletsCount())
    }

    fun updateSendUsageDataSetting() {
        _sendUsageData.postValue(localStoreRepository.isTrackingUsageData())
    }

    fun updatePinTimeoutSetting() {
        _pinTimeoutSetting.postValue(localStoreRepository.getPinTimeout())
    }

    fun updateNameSetting() {
        _nameSetting.postValue(localStoreRepository.getUserAlias() ?: "")
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
        appRestartNeeded = true
        updateAppThemeSetting()
    }

    fun getAppTheme(): AppTheme {
        return localStoreRepository.getAppTheme()
    }

    fun savePinTimeout(timeout: Int) {
        localStoreRepository.updatePinTimeout(timeout)
        updatePinTimeoutSetting()
    }

    fun saveUsageDataTracking(track: Boolean) {
        if(track != localStoreRepository.isTrackingUsageData()) {
            if(track) {
                localStoreRepository.setUsageDataTrackingEnabled(track)
                eventTracker.applyDataTrackingConsent()
                eventTracker.log(EventEnableUsageData())
            } else {
                eventTracker.log(EventDisableUsageData())
                localStoreRepository.setUsageDataTrackingEnabled(track)
                eventTracker.applyDataTrackingConsent()
            }
        }
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

    fun hideHiddenWalletsCount(hide: Boolean) {
        localStoreRepository.hideHiddenWalletsCount(hide)
    }

    fun isHidingHiddenWalletsCount() = localStoreRepository.isHidingHiddenWalletsCount()
}