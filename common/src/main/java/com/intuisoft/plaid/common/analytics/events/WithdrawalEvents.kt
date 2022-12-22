package com.intuisoft.plaid.common.analytics.events

import com.intuisoft.plaid.common.analytics.Event

class EventWithdrawTypeInvoice: Event("withdraw_type_invoice", listOf())

class EventWithdrawTypeStandard: Event("withdraw_type_standard", listOf())

class EventWithdrawMax: Event("withdraw_max", listOf())

class EventWithdrawAddMemo: Event("withdraw_add_memo", listOf())

class EventWithdrawViewAdvancedOptions: Event("withdraw_view_advance_options", listOf())

class EventWithdrawUseSavedAddress: Event("withdraw_use_saved_address", listOf())

class EventWithdrawWalletTransfer: Event("withdraw_wallet_transfer", listOf())

