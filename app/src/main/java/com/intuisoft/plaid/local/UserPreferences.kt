package com.intuisoft.plaid.local

import android.content.SharedPreferences
import com.google.gson.Gson
import com.intuisoft.plaid.model.AppTheme
import com.intuisoft.plaid.model.BitcoinDisplayUnit
import com.intuisoft.plaid.util.Constants
import com.intuisoft.plaid.util.Constants.Limit.DEFAULT_MAX_PIN_ATTEMPTS
import com.intuisoft.plaid.walletmanager.StoredWalletInfo

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
        const val LAST_SYNC_TIME_KEY = "LAST_SYNC_TIME_KEY"
        const val WALLET_INFO = "WALLET_INFO"
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

    var bitcoinDisplayUnit: BitcoinDisplayUnit
        get() {
            val unit = getInt(BITCOIN_UNIT_KEY, BitcoinDisplayUnit.BTC.typeId)
            return BitcoinDisplayUnit.values().find { it.typeId == unit } ?: BitcoinDisplayUnit.BTC
        }
        set(unit) {
            putInt(BITCOIN_UNIT_KEY, unit.typeId)
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

    var walletSyncTime: Int
        get() {
            return getInt(LAST_SYNC_TIME_KEY, 0)
        }
        set(time) {
            putInt(LAST_SYNC_TIME_KEY, time)
        }

    var pinTimeout: Int
        get() {
            return getInt(PIN_TIMEOUT_KEY, Constants.Limit.DEFAULT_PIN_TIMEOUT)
        }
        set(timeout) {
            putInt(PIN_TIMEOUT_KEY, timeout)
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
            val walletInfo = getString(WALLET_INFO, null)

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
                    WALLET_INFO,
                    Gson().toJson(StoredWalletInfo(mutableListOf()))
                )
            } else {
                putString(
                    WALLET_INFO,
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

    fun putBool(key: String, value: Boolean) {
        securePrefs.edit().putBoolean(key, value).apply()
    }

    fun getBool(key: String, defVal: Boolean = false) = securePrefs.getBoolean(key, defVal)

    fun putString(key: String, value: String?) {
        securePrefs.edit().putString(key, value).apply()
    }

    fun putInt(key: String, value: Int) {
        securePrefs.edit().putInt(key, value).apply()
    }

    fun getString(key: String, defVal: String? = null) = securePrefs.getString(key, defVal)

    fun getInt(key: String, defVal: Int = 0) = securePrefs.getInt(key, defVal)

    fun wipeData() {
        securePrefs.edit().clear().apply()
    }
}
