package com.intuisoft.plaid.common.analytics.events

import com.intuisoft.plaid.common.analytics.Event
import com.intuisoft.plaid.common.analytics.EventParamId
import com.intuisoft.plaid.common.model.ReportHistoryTimeFilter

class PurchaseSubscriptionView: Event("purchase_sub_view", listOf())

class CurrentSubscriptionView: Event("current_sub_view", listOf())
