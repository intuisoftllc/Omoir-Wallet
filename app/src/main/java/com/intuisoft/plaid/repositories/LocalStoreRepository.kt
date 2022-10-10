package com.intuisoft.plaid.repositories

import com.intuisoft.plaid.local.WipeDataListener
import com.intuisoft.plaid.model.AppTheme
import com.intuisoft.plaid.model.BitcoinDisplayUnit
import com.intuisoft.plaid.model.WalletType
import com.intuisoft.plaid.util.Constants
import com.intuisoft.plaid.walletmanager.StoredWalletInfo

interface LocalStoreRepository {

    fun increaseIncorrectPinAttempts()

    fun maxPinEntryLimitReached(): Boolean

    fun getPinEntryLimit(): Int

    fun setMaxPinEntryLimit(limit: Int)

    fun resetPinEntries()

    fun getWalletSyncTime(): Int

    fun setOnWipeDataListener(databaseListener: WipeDataListener)

    fun updateBitcoinDisplayUnit(displayUnit: BitcoinDisplayUnit)

    fun getBitcoinDisplayUnit(): BitcoinDisplayUnit

    fun updateAppTheme(theme: AppTheme)

    fun getAppTheme(): AppTheme

    fun updatePinCheckedTime()

     fun resetPinCheckedTime()

    fun hasPinTimedOut(): Boolean

    fun updatePinTimeout(timeout: Int)

    fun getPinTimeout(): Int

    fun updateVersionTappedCount()

    fun versionTapLimitReached(): Boolean

    fun updateUserAlias(alias: String)

    fun getUserAlias(): String?

    fun updateUserPin(pin: String)

    fun getUserPin(): String?

    fun setFingerprintEnabled(enabled: Boolean)

    fun getStoredWalletInfo(): StoredWalletInfo

    fun setStoredWalletInfo(storedWalletInfo: StoredWalletInfo?)

    fun isFingerprintEnabled(): Boolean

    suspend fun wipeAllData(onWipeFinished: suspend () -> Unit)
}