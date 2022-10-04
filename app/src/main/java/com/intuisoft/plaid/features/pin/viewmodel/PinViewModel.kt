package com.intuisoft.plaid.features.pin.viewmodel

import android.app.Application
import com.intuisoft.plaid.androidwrappers.BaseViewModel
import com.intuisoft.plaid.local.UserPreferences
import com.intuisoft.plaid.util.Constants

class PinViewModel(
    application: Application,
    private val userPreferences: UserPreferences
): BaseViewModel(application, userPreferences) {

    val pin: String
        get() = userPreferences.pin ?: ""

    fun checkPinStatus(onShowPinScreen: () -> Unit) {
        val time = System.currentTimeMillis() / 1000

        if(userPreferences.lastCheckPin == 0 ||
            (time - userPreferences.lastCheckPin) > userPreferences.pinTimeout) {
            onShowPinScreen()
        }
    }

    fun updatePinCheckedTime() {
        userPreferences.lastCheckPin =
            (System.currentTimeMillis() / 1000).toInt()
    }

    fun onMaxAttempts() {
        eraseAllData()
    }

    fun updatePin(pin: String) {
        userPreferences.pin = pin
    }

}