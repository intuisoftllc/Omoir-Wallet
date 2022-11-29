package com.intuisoft.plaid.androidwrappers

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.hardware.biometrics.BiometricManager
import android.os.Build
import android.provider.Settings
import androidx.fragment.app.Fragment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.intuisoft.plaid.PlaidApp
import com.intuisoft.plaid.R
import com.intuisoft.plaid.common.repositories.LocalStoreRepository
import com.intuisoft.plaid.walletmanager.AbstractWalletManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

open class BaseViewModel(
    application: Application,
    private val localStoreRepository: LocalStoreRepository,
    private val walletManager: AbstractWalletManager
) : AndroidViewModel(application) {

    private val _fingerprintSupported = SingleLiveData<Boolean>()
    val fingerprintSupported: LiveData<Boolean> = _fingerprintSupported

    private val _loadng = MutableLiveData<Boolean>()
    val loadng: LiveData<Boolean> = _loadng

    var currentConfig: FragmentConfiguration? = null

    fun hasConfiguration(fragmentConfiguration: FragmentConfigurationType): Boolean {
        return currentConfig != null && currentConfig!!.configurationType == fragmentConfiguration
    }

    fun <T> execute(call: suspend () -> T,  onFinish: suspend (Result<T>) -> Unit) {
        _loadng.postValue(true)
        viewModelScope.launch {
            var result : Result<T>

            withContext(Dispatchers.IO) {
                try {
                    result = Result.success(call())
                } catch (e: Throwable) {
                    result = Result.failure(e)
                }

                _loadng.postValue(false)

                onFinish(result)
            }
        }
    }

    fun isFingerprintEnabled() = localStoreRepository.isFingerprintEnabled()

    fun checkFingerprintSupport(onEnroll: () -> Unit) {
        validateOrRegisterFingerprintSupport(
            onCheck = {
                _fingerprintSupported.postValue(it)
            },
            onEnroll = {
                onEnroll()
            }
        )
    }

    fun validateOrRegisterFingerprintSupport(onCheck: (Boolean) ->Unit, onEnroll: () -> Unit) {
        val biometricManager: androidx.biometric.BiometricManager = androidx.biometric.BiometricManager.from(getApplication())
        when (biometricManager.canAuthenticate()) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                onCheck(true)
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE,
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                onCheck(false)
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                onEnroll()
            }
        }
    }

    fun navigateToFingerprintSettings(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            activity.startActivity(Intent(Settings.ACTION_BIOMETRIC_ENROLL));
        }
        else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                activity.startActivity(Intent(Settings.ACTION_FINGERPRINT_ENROLL));
            }
            else {
                activity.startActivity(Intent(Settings.ACTION_SECURITY_SETTINGS));
            }
        }
    }

    fun eraseAllData(onWipeFinished: () -> Unit) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                localStoreRepository.wipeAllData {
                    withContext(Dispatchers.Main) {
                        onWipeFinished()
                    }
                }
            }
        }
    }

    fun softRestart(fragment: Fragment) {
        walletManager.stop()
        localStoreRepository.resetPinCheckedTime()
        fragment.navigate(R.id.splashFragment)
    }
}