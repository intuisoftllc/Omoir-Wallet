package com.intuisoft.plaid.features.onboarding.viewmodel

import android.app.Application
import android.hardware.biometrics.BiometricManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.intuisoft.plaid.androidwrappers.BaseViewModel
import com.intuisoft.plaid.androidwrappers.SingleLiveData
import com.intuisoft.plaid.common.repositories.LocalStoreRepository
import com.intuisoft.plaid.walletmanager.AbstractWalletManager
import com.intuisoft.plaid.walletmanager.WalletManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class OnboardingViewModel(
    application: Application,
    private val localStoreRepository: LocalStoreRepository,
    private val walletManager: AbstractWalletManager
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

        viewModelScope.launch {
            delay(500)
            _onBiometricRegisterSuccess.postValue(Unit)
        }
    }
}