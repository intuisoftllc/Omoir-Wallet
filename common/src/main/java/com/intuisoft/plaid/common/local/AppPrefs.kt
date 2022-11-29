package com.intuisoft.plaid.common.local

import android.content.SharedPreferences
import com.google.gson.Gson
import com.intuisoft.plaid.common.model.*
import com.intuisoft.plaid.common.util.Constants
import com.intuisoft.plaid.common.util.Constants.Limit.DEFAULT_MAX_PIN_ATTEMPTS
import com.intuisoft.plaid.common.util.Constants.Limit.MIN_CONFIRMATIONS

class AppPrefs(
    private val securePrefs: SharedPreferences
) {
    companion object {
        const val SHARED_PREFS_NAME = "com.intuisoft.plaid.app.prefs"
        const val ONBOARDING_FINISHED = "ONBOARDING_FINISHED"
        const val MAX_PIN_ATTEMPTS_KEY = "MAX_PIN_ATTEMPTS_KEY"
        const val FINGERPRINT_SECURITY_KEY = "FINGERPRINT_SECURITY_KEY"
        const val PIN_ATTEMPTS_KEY = "PIN_ATTEMPTS_KEY"
        const val ALIAS_KEY = "ALIAS_KEY"
    }

    var onboardingFinished: Boolean
        get() {
            return getBool(ONBOARDING_FINISHED)
        }
        set(finished) {
            putBool(ONBOARDING_FINISHED, finished)
        }

    var maxPinAttempts: Int
        get() {
            return getInt(MAX_PIN_ATTEMPTS_KEY, DEFAULT_MAX_PIN_ATTEMPTS)
        }
        set(attempts) {
            putInt(MAX_PIN_ATTEMPTS_KEY, attempts)
        }

    var fingerprintSecurity: Boolean
        get() {
            return getBool(FINGERPRINT_SECURITY_KEY, false)
        }
        set(enable) {
            putBool(FINGERPRINT_SECURITY_KEY, enable)
        }

    var incorrectPinAttempts: Int
        get() {
            return getInt(PIN_ATTEMPTS_KEY)
        }
        set(attempts) {
            putInt(PIN_ATTEMPTS_KEY, attempts)
        }

    var alias: String?
        get() {
            return getString(ALIAS_KEY)
        }
        set(alias) {
            putString(ALIAS_KEY, alias)
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
