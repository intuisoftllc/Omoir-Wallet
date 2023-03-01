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

    private val _userAlias = SingleLiveData<String>()
    val userAlias: LiveData<String> = _userAlias

    private val _onBiometricRegisterSuccess = SingleLiveData<Unit>()
    val onBiometricRegisterSuccess: LiveData<Unit> = _onBiometricRegisterSuccess

    private var alias = ""

    private var termsAccepted = false
    private var validName = false

    fun updateAlias(alias: String) {
        this.alias = alias
    }

    fun updateTermsAccepted(accepted: Boolean) {
        this.termsAccepted = accepted
    }

    fun setSavedAlias() {
        alias = localStoreRepository.getUserAlias() ?: ""
        _userAlias.postValue(alias)
    }

    fun setNameValid(valid: Boolean) {
        validName = valid
    }

    fun enableButton() {
        _advanceAllowed.postValue(validName && termsAccepted)
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