package com.intuisoft.plaid.di

import com.intuisoft.plaid.androidwrappers.SettingsItemView
import com.intuisoft.plaid.features.createwallet.viewmodel.CreateWalletViewModel
import com.intuisoft.plaid.features.dashboardscreen.viewmodel.DashboardViewModel
import com.intuisoft.plaid.features.homescreen.viewmodel.HomeScreenViewModel
import com.intuisoft.plaid.features.onboarding.viewmodel.OnboardingViewModel
import com.intuisoft.plaid.features.pin.viewmodel.PinViewModel
import com.intuisoft.plaid.features.settings.viewmodel.SettingsViewModel
import com.intuisoft.plaid.features.splash.viewmodel.SplashViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module


val viewModelModule = module {
    viewModel {
        OnboardingViewModel(get(), get(), get())
    }

    viewModel {
        SplashViewModel(get(), get(), get())
    }

    viewModel {
        PinViewModel(get(), get(), get())
    }

    viewModel {
        HomeScreenViewModel(get(), get(), get())
    }

    viewModel {
        SettingsViewModel(get(), get(), get())
    }

    viewModel {
        CreateWalletViewModel(get(), get(), get())
    }

    viewModel {
        DashboardViewModel(get(), get(), get())
    }
}
