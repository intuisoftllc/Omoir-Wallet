package com.intuisoft.plaid.di

import com.intuisoft.plaid.androidwrappers.SettingsItemView
import com.intuisoft.plaid.androidwrappers.WalletViewModel
import com.intuisoft.plaid.features.createwallet.viewmodel.CreateWalletViewModel
import com.intuisoft.plaid.features.dashboardscreen.viewmodel.WalletExportViewModel
import com.intuisoft.plaid.features.dashboardscreen.viewmodel.WalletSettingsViewModel
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
        WalletViewModel(get(), get(), get())
    }

    viewModel {
        WalletSettingsViewModel(get(), get(), get())
    }

    viewModel {
        WalletExportViewModel(get(), get(), get())
    }
}
