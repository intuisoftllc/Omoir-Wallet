package com.intuisoft.plaid.di

import WalletSettingsViewModel
import com.intuisoft.plaid.androidwrappers.WalletViewModel
import com.intuisoft.plaid.features.createwallet.viewmodel.CreateWalletViewModel
import com.intuisoft.plaid.features.dashboardflow.free.viewmodel.ExportOptionsViewModel
import com.intuisoft.plaid.features.dashboardflow.pro.viewmodel.*
import com.intuisoft.plaid.features.dashboardflow.shared.viewModel.*
import com.intuisoft.plaid.features.homescreen.shared.viewmodel.HomeScreenViewModel
import com.intuisoft.plaid.features.onboarding.viewmodel.OnboardingViewModel
import com.intuisoft.plaid.features.settings.viewmodel.AccountsViewModel
import com.intuisoft.plaid.features.settings.viewmodel.AddressBookViewModel
import com.intuisoft.plaid.features.settings.viewmodel.SettingsViewModel
import com.intuisoft.plaid.features.settings.viewmodel.SubscriptionViewModel
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
        HomeScreenViewModel(get(), get(), get())
    }

    viewModel {
        SettingsViewModel(get(), get(), get(), get())
    }

    viewModel {
        CreateWalletViewModel(get(), get(), get(), get(), get())
    }

    viewModel {
        WalletViewModel(get(), get(), get(), get(), get())
    }

    viewModel {
        WalletSettingsViewModel(get(), get(), get(), get(), get())
    }

    viewModel {
        WalletExportViewModel(get(), get(), get(), get(), get())
    }

    viewModel {
        WithdrawalViewModel(get(), get(), get(), get(), get())
    }

    viewModel {
        AddressBookViewModel(get(), get(), get(), get(), get())
    }

    viewModel {
        WithdrawConfirmationViewModel(get(), get(), get(), get(), get())
    }

    viewModel {
        MarketViewModel(get(), get(), get(), get(), get())
    }

    viewModel {
        ExchangeViewModel(get(), get(), get(), get(), get())
    }

    viewModel {
        InvoiceViewModel(get(), get(), get(), get(), get())
    }

    viewModel {
        ExchangeDetailsViewModel(get(), get(), get(), get(), get())
    }

    viewModel {
        ExchangeHistoryViewModel(get(), get(), get(), get(), get())
    }

    viewModel {
        ReportDetailsViewModel(get(), get(), get(), get(), get())
    }

    viewModel {
        DashboardViewModel(get(), get(), get(), get(), get())
    }

    viewModel {
        UtxoDistributionViewModel(get(), get(), get(), get(), get())
    }

    viewModel {
        AtpViewModel(get(), get(), get(), get(), get())
    }

    viewModel {
        AtpHistoryViewModel(get(), get(), get(), get(), get())
    }

    viewModel {
        AtpDetailsViewModel(get(), get(), get(), get(), get())
    }

    viewModel {
        ExportOptionsViewModel(get(), get(), get(), get(), get())
    }

    viewModel {
        AccountsViewModel(get(), get(), get(), get(), get())
    }

    viewModel {
        SubscriptionViewModel(get(), get(), get(), get(), get())
    }
}
