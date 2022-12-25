package com.intuisoft.plaid.common.analytics.events

import com.intuisoft.plaid.common.analytics.Event
import com.intuisoft.plaid.common.analytics.EventParamId

class EventAtpView: Event("atp_view", listOf())

class EventAtpBatchGap(
    val gap: Int
): Event("atp_batch_gap", listOf(EventParamId.AMOUNT to gap.toString()))

class EventAtpBatchSize(
    val size: Int
): Event("atp_batch_size", listOf(EventParamId.AMOUNT to size.toString()))

class EventAtpFeeSpread(
    val low: Int,
    val high: Int
): Event("atp_fee_spread", listOf(EventParamId.AMOUNT to "$low - $high"))

class EventAtpHistoryView: Event("atp_history_view", listOf())

class EventAtpCancel: Event("atp_cancel", listOf())

class EventAtpInfoView: Event("atp_info_view", listOf())
