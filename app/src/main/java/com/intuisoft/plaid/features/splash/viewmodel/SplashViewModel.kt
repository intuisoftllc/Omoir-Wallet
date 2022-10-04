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
import com.intuisoft.plaid.local.UserPreferences
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashViewModel(
    application: Application,
    private val userPreferences: UserPreferences
): BaseViewModel(application, userPreferences) {

    private val _nextDestination = MutableLiveData<NavDirections>()
    val nextDestination: LiveData<NavDirections> = _nextDestination

    // todo: place logic here to navigate to the next screen
    fun nextScreen() {
        viewModelScope.launch {
            delay(SPLASH_DURATION.toLong())

            if(userPreferences.pin != null && userPreferences.alias != null) {
                _nextDestination.postValue(SplashFragmentDirections.actionSplashFragmentToHomescreenFragment())
            } else {
                _nextDestination.postValue(SplashFragmentDirections.actionSplashFragmentToOnboardingFragment())
            }
        }
    }

    fun resetPinCheckedTime() {
        userPreferences.lastCheckPin = 0
    }

    companion object {
        val SPLASH_DURATION = 2000
    }
}