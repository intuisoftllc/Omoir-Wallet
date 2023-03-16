package com.intuisoft.plaid.common.analytics.events

import com.intuisoft.plaid.common.analytics.Event

class EventCreateWallet: Event("create_wallet", listOf())

class EventCreateWalletAdvancedOptions: Event("create_wallet_advanced_options", listOf())

class EventRecoveryPhraseImport: Event("recovery_phrase_import", listOf())

class EventPublicKeyImport: Event("public_key_import", listOf())

class EventTestNetWallet: Event("test_net_wallet_option", listOf())