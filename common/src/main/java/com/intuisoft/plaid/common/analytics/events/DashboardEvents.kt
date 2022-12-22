package com.intuisoft.plaid.common.analytics.events

import com.intuisoft.plaid.common.analytics.Event

class EventDashboardView: Event("dashboard_view", listOf())

class EventDashboardOpenSettingsOpen: Event("dashboard_open_settings", listOf())

class EventDashboardDeposit: Event("dashboard_deposit_funds", listOf())

class EventDashboardWithdrawal: Event("dashboard_withdraw_funds", listOf())

class EventDashboardViewTransaction: Event("dashboard_view_transaction", listOf())