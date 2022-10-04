package com.intuisoft.plaid.features.pin.viewmodel

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
import com.intuisoft.plaid.util.Constants
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PinViewModel(
    application: Application,
    private val userPreferences: UserPreferences
): BaseViewModel(application) {

    val pin: String
        get() = userPreferences.pin ?: ""

    fun checkPinStatus(onShowPinScreen: () -> Unit) {
        val time = System.currentTimeMillis() / 1000

        if(userPreferences.lastCheckPin == 0 ||
            (time - userPreferences.lastCheckPin) > Constants.Limit.PIN_CHECK_TIME) {
            onShowPinScreen()
        }
    }

    fun updatePinCheckedTime() {
        userPreferences.lastCheckPin =
            (System.currentTimeMillis() / 1000).toInt()
    }

    fun onMaxAttempts() {
        userPreferences.wipeData()
    }

}