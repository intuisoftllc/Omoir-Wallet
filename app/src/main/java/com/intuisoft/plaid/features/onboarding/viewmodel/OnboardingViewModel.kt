package com.intuisoft.plaid.features.onboarding.viewmodel

import android.app.Application
import android.hardware.biometrics.BiometricManager
import androidx.lifecycle.LiveData
import com.intuisoft.plaid.androidwrappers.BaseViewModel
import com.intuisoft.plaid.androidwrappers.SingleLiveData
import com.intuisoft.plaid.repositories.LocalStoreRepository
import com.intuisoft.plaid.walletmanager.WalletManager


class OnboardingViewModel(
    application: Application,
    private val localStoreRepository: LocalStoreRepository,
    private val walletManager: WalletManager
): BaseViewModel(application, localStoreRepository, walletManager) {

    private val _advanceAllowed = SingleLiveData<Boolean>()
    val advanceAllowed: LiveData<Boolean> = _advanceAllowed

    private val _onBiometricRegisterSuccess = SingleLiveData<Unit>()
    val onBiometricRegisterSuccess: LiveData<Unit> = _onBiometricRegisterSuccess

    val fingerprintEnrolled : Boolean get() = localStoreRepository.isFingerprintEnabled()

    fun updateAlias(alias: String) {
        localStoreRepository.updateUserAlias(alias)
    }

    fun enableNextButton(enable: Boolean) {
        _advanceAllowed.postValue(enable)
    }

    fun savePin(pin: String) {
        localStoreRepository.updateUserPin(pin)
    }

    fun biometricAuthenticationSuccessful() {
        localStoreRepository.setFingerprintEnabled(true)
        _onBiometricRegisterSuccess.postValue(Unit)
    }
}