package com.intuisoft.plaid.features.createwallet.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.intuisoft.plaid.androidwrappers.BaseViewModel
import com.intuisoft.plaid.local.UserPreferences
import kotlinx.coroutines.launch
import org.bitcoinj.core.NetworkParameters
import org.bitcoinj.core.Utils
import org.bitcoinj.params.TestNet3Params
import org.bitcoinj.script.Script.ScriptType
import org.bitcoinj.wallet.DeterministicSeed
import org.bitcoinj.wallet.Wallet
import java.util.*


class CreateWalletViewModel(
    application: Application,
    private val userPreferences: UserPreferences
): BaseViewModel(application, userPreferences) {

    var useTestNet = false
        private set

    private val _testNetEnabled = MutableLiveData<Boolean>()
    val testNetEnabled: LiveData<Boolean> = _testNetEnabled

    private val _mainNetEnabled = MutableLiveData<Boolean>()
    val mainNetEnabled: LiveData<Boolean> = _mainNetEnabled

    fun setUseTestNet(use: Boolean) {
        useTestNet = use
        _testNetEnabled.postValue(useTestNet)
        _mainNetEnabled.postValue(!useTestNet)
    }
}