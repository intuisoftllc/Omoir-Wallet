package com.intuisoft.plaid.features.homescreen.viewmodel

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


class HomeScreenViewModel(
    application: Application,
    private val userPreferences: UserPreferences
): BaseViewModel(application, userPreferences) {

    private val _homeScreenGreeting = MutableLiveData<Pair<String, String>>()
    val homeScreenGreeting: LiveData<Pair<String, String>> = _homeScreenGreeting

    fun updateGreeting() {
        _homeScreenGreeting.postValue(getGreetingPrefix() to "${userPreferences.alias}")
    }

    fun getGreetingPrefix(): String {
        val c = Calendar.getInstance()
        val timeOfDay = c.get(Calendar.HOUR_OF_DAY)

        return when (timeOfDay) {
            in 0..11 -> "Good Morning"
            in 12..15 -> "Good Afternoon"
            in 16..23 -> "Good Evening"
            else -> "Hello"
        }
    }


    fun foo() {
        viewModelScope.launch {
            val params: NetworkParameters = TestNet3Params.get()
            val wallet: Wallet = Wallet.createDeterministic(params, ScriptType.P2PKH)

            val seed: DeterministicSeed = wallet.getKeyChainSeed()
            println("seed: $seed")

            println("creation time: " + seed.creationTimeSeconds)
            System.out.println("mnemonicCode: " + Utils.SPACE_JOINER.join(seed.mnemonicCode))
        }
    }
}