package com.intuisoft.plaid.features.pin.viewmodel

import android.app.Application
import com.intuisoft.plaid.androidwrappers.BaseViewModel
import com.intuisoft.plaid.local.UserPreferences
import com.intuisoft.plaid.repositories.LocalStoreRepository
import com.intuisoft.plaid.walletmanager.WalletManager

class PinViewModel(
    application: Application,
    private val localStoreRepository: LocalStoreRepository,
    private val walletManager: WalletManager
): BaseViewModel(application, localStoreRepository, walletManager) {

    val pin: String
        get() = localStoreRepository.getUserPin() ?: ""

    fun checkPinStatus(onShowPinScreen: () -> Unit) {
        val time = System.currentTimeMillis() / 1000

        if(localStoreRepository.hasPinTimedOut()) {
            // TODO: I hate entering passwords, uncomment this for production
//            onShowPinScreen()
        }
    }

    fun updatePinCheckedTime() {
        localStoreRepository.updatePinCheckedTime()
    }

    fun onMaxAttempts(onDataWiped: () -> Unit) {
        eraseAllData(onDataWiped)
    }

    fun updatePin(pin: String) {
        localStoreRepository.updateUserPin(pin)
    }

}