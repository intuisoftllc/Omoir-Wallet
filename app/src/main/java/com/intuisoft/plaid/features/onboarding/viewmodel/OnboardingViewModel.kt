package com.intuisoft.plaid.features.onboarding.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.intuisoft.plaid.androidwrappers.BaseViewModel
import com.intuisoft.plaid.androidwrappers.SingleLiveData
import com.intuisoft.plaid.common.repositories.LocalStoreRepository
import com.intuisoft.plaid.walletmanager.AbstractWalletManager
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

    private var alias = ""

    fun updateAlias(alias: String) {
        this.alias = alias
    }

    fun enableNextButton(enable: Boolean) {
        _advanceAllowed.postValue(enable)
    }

    fun saveUserAlias() {
        localStoreRepository.updateUserAlias(alias)
    }

    fun biometricAuthenticationSuccessful() {
        localStoreRepository.setFingerprintEnabled(true)

        viewModelScope.launch {
            delay(400)
            _onBiometricRegisterSuccess.postValue(Unit)
        }
    }
}