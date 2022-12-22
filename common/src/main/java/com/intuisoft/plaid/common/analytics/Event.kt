package com.intuisoft.plaid.common.analytics

open class Event(
    val event_name: String,
    val event_params: List<Pair<String, String>>
)