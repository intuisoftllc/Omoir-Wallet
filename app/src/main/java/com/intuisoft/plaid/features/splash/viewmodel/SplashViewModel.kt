package com.intuisoft.plaid.features.splash.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavDirections
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.BaseViewModel
import com.intuisoft.plaid.features.splash.ui.SplashFragment
import com.intuisoft.plaid.features.splash.ui.SplashFragmentDirections
import com.intuisoft.plaid.common.repositories.LocalStoreRepository
import com.intuisoft.plaid.walletmanager.AbstractWalletManager
import com.intuisoft.plaid.walletmanager.WalletManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashViewModel(
    application: Application,
    private val localStoreRepository: LocalStoreRepository,
    private val walletManager: AbstractWalletManager
): BaseViewModel(application, localStoreRepository, walletManager) {

    private val _nextDestination = MutableLiveData<NavDirections>()
    val nextDestination: LiveData<NavDirections> = _nextDestination

    fun nextScreen() {
        viewModelScope.launch {
            delay(SPLASH_DURATION.toLong())

            if(localStoreRepository.getUserPin() != null && localStoreRepository.getUserAlias() != null) {
                _nextDestination.postValue(SplashFragmentDirections.actionSplashFragmentToHomescreenFragment())
            } else {
                _nextDestination.postValue(SplashFragmentDirections.actionSplashFragmentToOnboardingFragment())
            }
        }
    }

    fun startWalletManager() {
        walletManager.start()
    }

    fun resetPinCheckedTime() {
        localStoreRepository.resetPinCheckedTime()
    }

    companion object {
        val SPLASH_DURATION = 2000
    }
}