package com.intuisoft.plaid.repositories

import com.intuisoft.emojiigame.framework.db.LocalWallet
import com.intuisoft.plaid.local.db.DatabaseListener
import com.intuisoft.plaid.model.AppTheme
import com.intuisoft.plaid.model.BitcoinDisplayUnit
import com.intuisoft.plaid.model.WalletType
import com.intuisoft.plaid.util.Constants

interface LocalStoreRepository {

    fun increaseIncorrectPinAttempts()

    fun maxPinEntryLimitReached(): Boolean

    fun getPinEntryLimit(): Int

    fun setMaxPinEntryLimit(limit: Int)

    fun resetPinEntries()

    fun getWalletSyncTime(): Int

    fun setDatabaseListener(databaseListener: DatabaseListener)

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

    fun updateUserSalt(salt: String)

    fun getUserSalt(): String?

    fun setFingerprintEnabled(enabled: Boolean)

    fun isFingerprintEnabled(): Boolean

    suspend fun doesWalletExist(name: String) : Boolean

    suspend fun createWallet(name: String, type: WalletType, testnetWallet: Boolean)

    suspend fun getWallet(name: String): LocalWallet?

    suspend fun getAllWallets(): List<LocalWallet>?

    suspend fun wipeAllData(onWipeFinished: suspend () -> Unit)
}