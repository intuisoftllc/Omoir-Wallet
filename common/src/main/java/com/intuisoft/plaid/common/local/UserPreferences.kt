package com.intuisoft.plaid.common.local

import android.content.SharedPreferences
import com.google.gson.Gson
import com.intuisoft.plaid.common.model.*
import com.intuisoft.plaid.common.util.Constants
import com.intuisoft.plaid.common.util.Constants.Limit.DEFAULT_MAX_PIN_ATTEMPTS
import com.intuisoft.plaid.common.util.Constants.Limit.MIN_CONFIRMATIONS

class UserPreferences(
    private val securePrefs: SharedPreferences,
    private val gson: Gson
) {
    companion object {
        const val SHARED_PREFS_NAME = "com.intuisoft.plaid.user"
        const val ALIAS_KEY = "ALIAS_KEY"
        const val USER_PIN_KEY = "USER_PIN_KEY"
        const val PIN_ATTEMPTS_KEY = "PIN_ATTEMPTS_KEY"
        const val MAX_PIN_ATTEMPTS_KEY = "MAX_PIN_ATTEMPTS_KEY"
        const val FINGERPRINT_SECURITY_KEY = "FINGERPRINT_SECURITY_KEY"
        const val LAST_CHECKED_PIN_KEY = "LAST_CHECKED_PIN_KEY"
        const val BITCOIN_UNIT_KEY = "BITCOIN_UNIT_KEY"
        const val APP_THEME_KEY = "APP_THEME_KEY"
        const val PIN_TIMEOUT_KEY = "PIN_TIMEOUT_KEY"
        const val VERSION_TAPPED_COUNT_KEY = "VERSION_TAPPED_COUNT_KEY"
        const val WALLET_INFO_KEY = "WALLET_INFO_KEY"
        const val DEFAULT_FEE_TYPE_KEY = "DEFAULT_FEE_TYPE_KEY"
        const val SAVED_ADDRESSES_KEY = "SAVED_ADDRESSES_KEY"
        const val MIN_CONFIRMATIONS_KEY = "MIN_CONFIRMATIONS_KEY"
        const val BASE_WALLET = "BASE_WALLET"
        const val FEE_RATE_UPDATE_TIME = "FEE_RATE_UPDATE_TIME"
        const val DEVICE_PERFORMANCE = "DEVICE_PERFORMANCE"
        const val PRO_ENABLED = "PRO_ENABLED"
    }

    var incorrectPinAttempts: Int
        get() {
            return getInt(PIN_ATTEMPTS_KEY)
        }
        set(attempts) {
            putInt(PIN_ATTEMPTS_KEY, attempts)
        }

    var maxPinAttempts: Int
        get() {
            return getInt(MAX_PIN_ATTEMPTS_KEY, DEFAULT_MAX_PIN_ATTEMPTS)
        }
        set(attempts) {
            putInt(MAX_PIN_ATTEMPTS_KEY, attempts)
        }

    var minConfirmations: Int
        get() {
            return getInt(MIN_CONFIRMATIONS_KEY, MIN_CONFIRMATIONS)
        }
        set(min) {
            putInt(MIN_CONFIRMATIONS_KEY, min)
        }

    var devicePerformanceLevel: DevicePerformanceLevel?
        get() {
            val level = getInt(DEVICE_PERFORMANCE, -1)
            return DevicePerformanceLevel.values().find { it.ordinal == level }
        }
        set(level) {
            putInt(DEVICE_PERFORMANCE, level?.ordinal ?: -1)
        }

    var bitcoinDisplayUnit: BitcoinDisplayUnit
        get() {
            val unit = getInt(BITCOIN_UNIT_KEY, BitcoinDisplayUnit.BTC.typeId)
            return BitcoinDisplayUnit.values().find { it.typeId == unit } ?: BitcoinDisplayUnit.SATS
        }
        set(unit) {
            putInt(BITCOIN_UNIT_KEY, unit.typeId)
        }

    var baseWalletSeed: String?
        get() {
            return getString(BASE_WALLET)
        }
        set(alias) {
            putString(BASE_WALLET, alias)
        }

    var savedAddressInfo: SavedAddressInfo
        get() {
            val addresses = getString(SAVED_ADDRESSES_KEY, null)

            if(addresses != null) {
                return Gson().fromJson(
                    addresses,
                    SavedAddressInfo::class.java
                )
            }

            return SavedAddressInfo(mutableListOf())
        }
        set(info) {
            if(info == null) {
                putString(
                    SAVED_ADDRESSES_KEY,
                    Gson().toJson(SavedAddressInfo(mutableListOf()))
                )
            } else {
                putString(
                    SAVED_ADDRESSES_KEY,
                    Gson().toJson(info)
                )
            }
        }

    var appTheme: AppTheme
        get() {
            val unit = getInt(APP_THEME_KEY, 0)
            return AppTheme.values().find { it.typeId == unit } ?: AppTheme.AUTO
        }
        set(theme) {
            putInt(APP_THEME_KEY, theme.typeId)
        }

    var lastCheckPin: Int
        get() {
            return getInt(LAST_CHECKED_PIN_KEY, 0)
        }
        set(attempts) {
            putInt(LAST_CHECKED_PIN_KEY, attempts)
        }

    var defaultFeeType: FeeType
        get() {
            val type = getInt(DEFAULT_FEE_TYPE_KEY, FeeType.MED.ordinal)
            return FeeType.values().find { it.ordinal == type } ?: FeeType.MED
        }
        set(type) {
            putInt(DEFAULT_FEE_TYPE_KEY, type.ordinal)
        }

    var pinTimeout: Int
        get() {
            return getInt(PIN_TIMEOUT_KEY, Constants.Limit.DEFAULT_PIN_TIMEOUT)
        }
        set(timeout) {
            putInt(PIN_TIMEOUT_KEY, timeout)
        }

    var lastFeeRateUpdateTime: Long
        get() {
            return getLong(FEE_RATE_UPDATE_TIME, 0)
        }
        set(time) {
            putLong(FEE_RATE_UPDATE_TIME, time)
        }

    var versionTappedCount: Int
        get() {
            return getInt(VERSION_TAPPED_COUNT_KEY, 0)
        }
        set(count) {
            putInt(VERSION_TAPPED_COUNT_KEY, count)
        }

    var alias: String?
        get() {
            return getString(ALIAS_KEY)
        }
        set(alias) {
            putString(ALIAS_KEY, alias)
        }

    var pin: String?
        get() {
            return getString(USER_PIN_KEY)
        }
        set(pin) {
            putString(USER_PIN_KEY, pin)
        }

    var storedWalletInfo: StoredWalletInfo
        get() {
            val walletInfo = getString(WALLET_INFO_KEY, null)

            if(walletInfo != null) {
                return Gson().fromJson(
                    walletInfo,
                    StoredWalletInfo::class.java
                )
            }

            return StoredWalletInfo(mutableListOf())
        }
        set(info) {
            if(info == null) {
                putString(
                    WALLET_INFO_KEY,
                    Gson().toJson(StoredWalletInfo(mutableListOf()))
                )
            } else {
                putString(
                    WALLET_INFO_KEY,
                    Gson().toJson(info)
                )
            }
        }

    var fingerprintSecurity: Boolean
        get() {
            return getBool(FINGERPRINT_SECURITY_KEY, false)
        }
        set(enable) {
            putBool(FINGERPRINT_SECURITY_KEY, enable)
        }

    var isProEnabled: Boolean
        get() {
            return getBool(PRO_ENABLED, false)
        }
        set(enable) {
            putBool(PRO_ENABLED, enable)
        }

    fun putBool(key: String, value: Boolean) {
        synchronized(this::class.java) {
            securePrefs.edit().putBoolean(key, value).apply()
        }
    }

    fun getBool(key: String, defVal: Boolean = false): Boolean {
        synchronized(this::class.java) {
            return securePrefs.getBoolean(key, defVal)
        }
    }

    fun putString(key: String, value: String?) {
        synchronized(this::class.java) {
            securePrefs.edit().putString(key, value).apply()
        }
    }

    fun putInt(key: String, value: Int) {
        synchronized(this::class.java) {
            securePrefs.edit().putInt(key, value).apply()
        }
    }

    fun putLong(key: String, value: Long) {
        synchronized(this::class.java) {
            securePrefs.edit().putLong(key, value).apply()
        }
    }

    fun getString(key: String, defVal: String? = null): String? {
        synchronized(this::class.java) {
            return securePrefs.getString(key, defVal)
        }
    }

    fun getInt(key: String, defVal: Int = 0): Int {
        synchronized(this::class.java) {
            return securePrefs.getInt(key, defVal)
        }
    }

    fun getLong(key: String, defVal: Long = 0): Long {
        synchronized(this::class.java) {
            return securePrefs.getLong(key, defVal)
        }
    }

    fun wipeData() {
        securePrefs.edit().clear().apply()
    }
}
