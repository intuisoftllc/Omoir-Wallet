package com.intuisoft.plaid.features.pin.viewmodel

import android.app.Application
import com.intuisoft.plaid.androidwrappers.BaseViewModel
import com.intuisoft.plaid.common.repositories.LocalStoreRepository
import com.intuisoft.plaid.walletmanager.AbstractWalletManager
import com.intuisoft.plaid.walletmanager.WalletManager

class PinViewModel(
    application: Application,
    private val localStoreRepository: LocalStoreRepository,
    private val walletManager: AbstractWalletManager
): BaseViewModel(application, localStoreRepository, walletManager) {

    fun checkPinStatus(onShowPinScreen: () -> Unit) {
        if(localStoreRepository.hasPinTimedOut()) {
            // TODO: I hate entering passwords, uncomment this for production
            onShowPinScreen()
        }
    }

    fun updatePinCheckedTime() {
        localStoreRepository.updatePinCheckedTime()
    }

    fun onMaxAttempts(onDataWiped: () -> Unit) {
        eraseAllData(onDataWiped)
    }

    fun startWalletManager() {
        walletManager.start()
    }
}