package com.intuisoft.plaid.features.homescreen.shared.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.intuisoft.plaid.OmoirApp
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.BaseViewModel
import com.intuisoft.plaid.common.repositories.LocalStoreRepository
import com.intuisoft.plaid.walletmanager.AbstractWalletManager
import java.util.*


class HomeScreenViewModel(
    application: Application,
    private val localStoreRepository: LocalStoreRepository,
    private val walletManager: AbstractWalletManager
): BaseViewModel(application, localStoreRepository, walletManager) {

    private val _homeScreenGreeting = MutableLiveData<Pair<String, String>>()
    val homeScreenGreeting: LiveData<Pair<String, String>> = _homeScreenGreeting

    fun updateGreeting() {
        _homeScreenGreeting.postValue(getGreetingPrefix() to "${localStoreRepository.getUserAlias()}")
    }

    fun getGreetingPrefix(): String {
        val c = Calendar.getInstance()
        val timeOfDay = c.get(Calendar.HOUR_OF_DAY)

        return when (timeOfDay) {
            in 0..11 -> getApplication<OmoirApp>().getString(R.string.homescreen_greeting_1)
            in 12..15 -> getApplication<OmoirApp>().getString(R.string.homescreen_greeting_2)
            in 16..23 -> getApplication<OmoirApp>().getString(R.string.homescreen_greeting_3)
            else -> getApplication<OmoirApp>().getString(R.string.homescreen_greeting_4)
        }
    }
}