package com.intuisoft.plaid.features.homescreen.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavDirections
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.BaseViewModel
import com.intuisoft.plaid.features.splash.ui.SplashFragment
import com.intuisoft.plaid.features.splash.ui.SplashFragmentDirections
import com.intuisoft.plaid.local.UserPreferences
import com.intuisoft.plaid.util.Constants
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

class HomeScreenViewModel(
    application: Application,
    private val userPreferences: UserPreferences
): BaseViewModel(application) {

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

}