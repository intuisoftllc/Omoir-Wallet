package com.intuisoft.plaid.common.analytics.events

import com.intuisoft.plaid.common.analytics.Event

class EventHomescreenView: Event("homescreen_view", listOf())

class EventHomescreenOpenSettings: Event("homescreen_open_settings", listOf())

class EventHomescreenCreateWallet: Event("homescreen_create_wallet", listOf())