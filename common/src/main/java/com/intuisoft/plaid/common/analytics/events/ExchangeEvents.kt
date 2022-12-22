package com.intuisoft.plaid.common.analytics.events

import com.intuisoft.plaid.common.analytics.Event

class EventExchangeView: Event("exchange_view", listOf())

class EventExchangeCreate: Event("exchange_create", listOf())

class EventExchangeHistoryView: Event("exchange_history_view", listOf())

class EventExchangeDetailsView: Event("exchange_details_view", listOf())

class EventExchangeDetailsSmartPay: Event("exchange_details_smart_pay", listOf())
