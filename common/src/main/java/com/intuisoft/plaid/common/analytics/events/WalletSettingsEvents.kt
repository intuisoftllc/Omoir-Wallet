package com.intuisoft.plaid.common.analytics.events

import com.intuisoft.plaid.common.analytics.Event

class EventWalletSettingsView: Event("wallet_settings_view", listOf())

class EventWalletSettingsRenameWallet: Event("wallet_settings_rename_wallet", listOf())

class EventWalletSettingsViewPassphrase: Event("wallet_settings_view_passphrase", listOf())

class EventWalletSettingsSetPassphrase: Event("wallet_settings_set_passphrase", listOf())

class EventWalletSettingsExportTransactions: Event("wallet_settings_export_transactions", listOf())

class EventWalletSettingsExportCsv: Event("wallet_settings_export_csv", listOf())

class EventWalletSettingsDeleteWallet: Event("wallet_settings_delete_wallet", listOf())