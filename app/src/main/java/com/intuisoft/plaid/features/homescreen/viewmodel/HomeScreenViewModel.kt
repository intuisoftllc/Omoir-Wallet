package com.intuisoft.plaid.features.homescreen.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.intuisoft.emojiigame.framework.db.LocalWallet
import com.intuisoft.plaid.androidwrappers.BaseViewModel
import com.intuisoft.plaid.androidwrappers.SingleLiveData
import com.intuisoft.plaid.local.UserPreferences
import com.intuisoft.plaid.model.LocalWalletModel
import com.intuisoft.plaid.repositories.LocalStoreRepository
import com.intuisoft.plaid.util.AesEncryptor
import com.intuisoft.plaid.walletmanager.WalletManager
import kotlinx.coroutines.launch
import java.util.*


class HomeScreenViewModel(
    application: Application,
    private val localStoreRepository: LocalStoreRepository,
    private val walletManager: WalletManager,
    private val aesEncryptor: AesEncryptor
): BaseViewModel(application, localStoreRepository, aesEncryptor) {

    private val _homeScreenGreeting = MutableLiveData<Pair<String, String>>()
    val homeScreenGreeting: LiveData<Pair<String, String>> = _homeScreenGreeting

    private val _wallets = SingleLiveData<List<LocalWalletModel>>()
    val wallets: LiveData<List<LocalWalletModel>> = _wallets

    fun updateGreeting() {
        _homeScreenGreeting.postValue(getGreetingPrefix() to "${localStoreRepository.getUserAlias()}")
    }

    fun showWallets() {
        viewModelScope.launch {
            _wallets.postValue(walletManager.getWallets())
        }
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
}