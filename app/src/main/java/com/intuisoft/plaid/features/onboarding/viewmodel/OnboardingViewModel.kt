package com.intuisoft.plaid.features.onboarding.viewmodel

import android.app.Application
import android.hardware.biometrics.BiometricManager
import androidx.lifecycle.LiveData
import com.intuisoft.plaid.androidwrappers.BaseViewModel
import com.intuisoft.plaid.androidwrappers.SingleLiveData
import com.intuisoft.plaid.local.UserPreferences


class OnboardingViewModel(
    application: Application,
    private val userPreferences: UserPreferences
): BaseViewModel(application) {

    private val _advanceAllowed = SingleLiveData<Boolean>()
    val advanceAllowed: LiveData<Boolean> = _advanceAllowed

    private val _fingerprintSupported = SingleLiveData<Boolean>()
    val fingerprintSupported: LiveData<Boolean> = _fingerprintSupported

    private val _onBiometricRegisterSuccess = SingleLiveData<Unit>()
    val onBiometricRegisterSuccess: LiveData<Unit> = _onBiometricRegisterSuccess

    var fingerprintEnrollRequired = false
    val fingerprintEnrolled : Boolean get() = userPreferences.fingerprintSecurity

    fun updateAlias(alias: String?) {
        userPreferences.alias = alias
    }

    fun enableNextButton(enable: Boolean) {
        _advanceAllowed.postValue(enable)
    }

    fun savePin(pin: String?) {
        userPreferences.pin = pin
    }

    fun biometricAuthenticationSuccessful() {
        userPreferences.fingerprintSecurity = true
        _onBiometricRegisterSuccess.postValue(Unit)
    }

    fun checkFingerprintSupport() {
        val biometricManager: androidx.biometric.BiometricManager = androidx.biometric.BiometricManager.from(getApplication())
        when (biometricManager.canAuthenticate()) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                _fingerprintSupported.postValue(true)
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE,
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                _fingerprintSupported.postValue(false)
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                _fingerprintSupported.postValue(true)
                fingerprintEnrollRequired = true
            }
        }
    }
}