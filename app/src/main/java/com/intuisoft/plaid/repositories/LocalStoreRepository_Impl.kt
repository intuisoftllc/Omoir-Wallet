package com.intuisoft.plaid.repositories

import com.docformative.docformative.remove
import com.intuisoft.plaid.local.UserPreferences
import com.intuisoft.plaid.local.WipeDataListener
import com.intuisoft.plaid.model.*
import com.intuisoft.plaid.util.Constants
import com.intuisoft.plaid.walletmanager.StoredWalletInfo

class LocalStoreRepository_Impl(
    private val userPreferences: UserPreferences,
): LocalStoreRepository {

    private var wipeDataListener: WipeDataListener? = null
    private var cachedStoredWalletInfo: StoredWalletInfo? = null

    override fun increaseIncorrectPinAttempts() {
        userPreferences.incorrectPinAttempts = userPreferences.incorrectPinAttempts + 1
    }

    override fun maxPinEntryLimitReached(): Boolean {
        return userPreferences.incorrectPinAttempts >= userPreferences.maxPinAttempts
    }

    override fun getPinEntryLimit(): Int {
        return userPreferences.maxPinAttempts
    }

    override fun getMinimumConfirmations(): Int {
        return userPreferences.minConfirmations
    }

    override fun setMinConfirmations(minConfirmations: Int) {
        userPreferences.minConfirmations = minConfirmations
    }

    override fun setMaxPinEntryLimit(limit: Int) {
        userPreferences.maxPinAttempts = limit
    }

    override fun resetPinEntries() {
        userPreferences.incorrectPinAttempts = 0
    }

    override fun getDefaultFeeType(): FeeType {
        return userPreferences.defaultFeeType
    }

    override fun setDefaultFeeType(type: FeeType) {
        userPreferences.defaultFeeType = type
    }

    override fun getWalletSyncTime(): Int {
        return userPreferences.walletSyncTime
    }

    override fun setOnWipeDataListener(listener: WipeDataListener) {
        wipeDataListener = listener
    }

    override fun updateBitcoinDisplayUnit(displayUnit: BitcoinDisplayUnit) {
        userPreferences.bitcoinDisplayUnit = displayUnit
    }

    override fun getSavedAddresses(): List<SavedAddressModel> {
        return userPreferences.savedAddressInfo.savedAddresses
    }

    override fun deleteSavedAddress(name: String) {
        val addressess = userPreferences.savedAddressInfo.savedAddresses
        addressess.remove { it.addressName == name }

        userPreferences.savedAddressInfo = SavedAddressInfo(addressess)
    }

    override fun updateSavedAddress(oldName: String, name: String, address: String) {
        val addressess = userPreferences.savedAddressInfo.savedAddresses
        addressess.find { it.addressName == oldName }?.let {
            it.addressName = name
            it.address = address
        }

        userPreferences.savedAddressInfo = SavedAddressInfo(addressess)
    }

    override fun saveBaseWalletSeed(words: List<String>) {
        userPreferences.baseWalletSeed = words.joinToString(" ")
    }

    override fun getBaseWalletSeed(): List<String> {
        return userPreferences.baseWalletSeed?.split(" ") ?: listOf()
    }

    override fun saveAddress(name: String, address: String) {
        val addressess = userPreferences.savedAddressInfo.savedAddresses
        addressess.add(SavedAddressModel(name, address))

        userPreferences.savedAddressInfo = SavedAddressInfo(addressess)
    }

    override fun getBitcoinDisplayUnit(): BitcoinDisplayUnit {
        return userPreferences.bitcoinDisplayUnit
    }

    override fun updateAppTheme(theme: AppTheme) {
        userPreferences.appTheme = theme
    }

    override fun getAppTheme(): AppTheme {
        return userPreferences.appTheme
    }

    override fun updatePinCheckedTime() {
        userPreferences.lastCheckPin =
            (System.currentTimeMillis() / 1000).toInt()
    }

    override fun resetPinCheckedTime() {
        userPreferences.lastCheckPin = 0
    }

    override fun hasPinTimedOut(): Boolean {
        val time = System.currentTimeMillis() / 1000

        return userPreferences.lastCheckPin == 0 ||
                (time - userPreferences.lastCheckPin) > userPreferences.pinTimeout
    }

    override fun updatePinTimeout(timeout: Int) {
        if(timeout == Constants.Time.INSTANT) {
            userPreferences.pinTimeout = Constants.Time.INSTANT_TIME_OFFSET
        } else {
            userPreferences.pinTimeout = timeout
        }
    }

    override fun getPinTimeout(): Int {
        return userPreferences.pinTimeout
    }

    override fun updateVersionTappedCount() {
        if(userPreferences.versionTappedCount < Constants.Limit.VERSION_CODE_TAPPED_LIMIT) {
            userPreferences.versionTappedCount = userPreferences.versionTappedCount + 1
        }
    }

    override fun versionTapLimitReached(): Boolean {
        return userPreferences.versionTappedCount == Constants.Limit.VERSION_CODE_TAPPED_LIMIT
    }

    override fun updateUserAlias(alias: String) {
        userPreferences.alias = alias
    }

    override fun getUserAlias(): String? {
        return userPreferences.alias
    }

    override fun updateUserPin(pin: String) {
        userPreferences.pin = pin
    }

    override fun getUserPin(): String? {
        return userPreferences.pin
    }

    override fun getStoredWalletInfo(): StoredWalletInfo {
        if(cachedStoredWalletInfo != null)
            return cachedStoredWalletInfo!!

        cachedStoredWalletInfo = userPreferences.storedWalletInfo
        return cachedStoredWalletInfo!!
    }

    override fun setStoredWalletInfo(storedWalletInfo: StoredWalletInfo?) {
        cachedStoredWalletInfo = null
        userPreferences.storedWalletInfo =
            if(storedWalletInfo == null)
                StoredWalletInfo(mutableListOf())
            else storedWalletInfo
    }

    override fun setFingerprintEnabled(enabled: Boolean) {
        userPreferences.fingerprintSecurity = enabled
    }

    override fun isFingerprintEnabled(): Boolean {
        return userPreferences.fingerprintSecurity
    }

    override suspend fun wipeAllData(onWipeFinished: suspend () -> Unit) {
        userPreferences.wipeData()
        wipeDataListener?.onWipeData()
        onWipeFinished()
    }
}