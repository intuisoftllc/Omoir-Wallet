package com.intuisoft.plaid.features.splash.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavDirections
import com.intuisoft.plaid.androidwrappers.BaseViewModel
import com.intuisoft.plaid.common.CommonService
import com.intuisoft.plaid.features.splash.ui.SplashFragmentDirections
import com.intuisoft.plaid.common.repositories.LocalStoreRepository
import com.intuisoft.plaid.walletmanager.AbstractWalletManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashViewModel(
    application: Application,
    private val localStoreRepository: LocalStoreRepository,
    private val walletManager: AbstractWalletManager
): BaseViewModel(application, localStoreRepository, walletManager) {

    private val _nextDestination = MutableLiveData<NavDirections>()
    val nextDestination: LiveData<NavDirections> = _nextDestination

    private val _goHome = MutableLiveData<Unit>()
    val goHome: LiveData<Unit> = _goHome

    fun nextScreen() {
        viewModelScope.launch {
            delay(SPLASH_DURATION.toLong())

            if(localStoreRepository.hasCompletedOnboarding()) {
                _goHome.postValue(Unit)
            } else {
                _nextDestination.postValue(SplashFragmentDirections.actionSplashFragmentToOnboardingFragment())
            }
        }
    }

    companion object {
        val SPLASH_DURATION = 2000
    }
}