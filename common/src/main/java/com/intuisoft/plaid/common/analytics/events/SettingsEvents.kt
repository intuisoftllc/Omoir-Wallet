package com.intuisoft.plaid.common.analytics.events

import com.intuisoft.plaid.common.analytics.Event
import com.intuisoft.plaid.common.analytics.EventParamId
import com.intuisoft.plaid.common.model.AppTheme

class EventSettingsView: Event("settings_view", listOf())

class EventSettingsChangeName: Event("settings_name", listOf())

class EventSettingsChangeAppearance(
    val uiMode: AppTheme
): Event("settings_appearance", listOf(EventParamId.TYPE to uiMode.name.lowercase()))

class EventSettingsSetLocalCurrency(
    val currencyCode: String
): Event("settings_local_currency", listOf(EventParamId.TYPE to currencyCode))

class EventSettingsMaxPinAttempts(
    val max: Int
): Event("settings_max_attempts", listOf(EventParamId.AMOUNT to max.toString()))

class EventSettingsPinTimeout(
    val timeout: String
): Event("settings_pin_timeout", listOf(EventParamId.AMOUNT to timeout))

class EventSettingsEnableFingerprint: Event("settings_enable_fingerprint", listOf())

class EventSettingsDisableFingerprint: Event("settings_disable_fingerprint", listOf())

class EventSettingsViewAddressBook: Event("settings_view_address_book", listOf())

class EventSettingsViewAccounts: Event("settings_view_accounts", listOf())

class EventSettingsSaveAddress: Event("settings_save_address", listOf())

class EventSettingsSaveAccount: Event("settings_save_account", listOf())

class EventSettingsDeleteAddress: Event("settings_delete_address", listOf())

class EventSettingsDeleteAccount: Event("settings_delete_account", listOf())

class EventSettingsUpdateAddress: Event("settings_update_address", listOf())

class EventSettingsUpdateAccount: Event("settings_update_account", listOf())

class EventSettingsMinimumConfirmations(
    val min: Int
): Event("settings_min_confirmations", listOf(EventParamId.AMOUNT to min.toString()))

class EventSettingsViewCredits: Event("settings_view_credits", listOf())

class EventSettingsWipeData: Event("settings_wipe_data", listOf())
