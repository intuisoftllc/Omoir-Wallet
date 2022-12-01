package com.intuisoft.plaid.model

import com.intuisoft.plaid.R


enum class ExchangeStatus(val type: String, val color: Int) {
    CLOSED("closed", R.color.alt_error_color),
    CONFIRMING("confirming", R.color.text_grey),
    EXCHANGING("exchanging", R.color.text_grey),
    EXPIRED("expired", R.color.warning_color),
    FAILED("failed", R.color.error_color),
    FINISHED("finished", R.color.success_color),
    REFUNDED("refunded", R.color.warning_color),
    SENDING("sending", R.color.text_grey),
    VERIFYING("verifying", R.color.text_grey),
    WAITING("waiting", R.color.text_grey)
}