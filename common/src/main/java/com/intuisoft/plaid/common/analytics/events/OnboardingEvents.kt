package com.intuisoft.plaid.common.analytics.events

import com.intuisoft.plaid.common.analytics.Event

class EventOnboardingStart: Event("onboarding_start", listOf())

class EventOnboardingFingerprintRegister: Event("onboarding_register_fingerprint", listOf())

class EventOnboardingSkipFingerprintRegister: Event("onboarding_skip_fingerprint_register", listOf())

class EventOnboardingFinish: Event("onboarding_finish", listOf())

class EventOnboardingCreateWallet: Event("onboarding_create_wallet", listOf())

class EventOnboardingGotoHomescreen: Event("onboarding_homescreen", listOf())