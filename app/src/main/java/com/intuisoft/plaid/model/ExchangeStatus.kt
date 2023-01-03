package com.intuisoft.plaid.model

import com.intuisoft.plaid.R


enum class ExchangeStatus(val type: String, val color: Int) { // todo: replace these with string res ids
    NEW("new", R.color.subtitle_text_color),
    CONFIRMING("confirming", R.color.subtitle_text_color),
    EXCHANGING("exchanging", R.color.subtitle_text_color),
    SENDING("sending", R.color.subtitle_text_color),
    FAILED("failed", R.color.error_color),
    FINISHED("finished", R.color.success_color),
    REFUNDED("refunded", R.color.warning_color),
    VERIFYING("verifying", R.color.subtitle_text_color),
    WAITING("waiting", R.color.subtitle_text_color);

    fun isFinalState(): Boolean {
        when {
            this == FAILED
                || this == FINISHED
                || this == REFUNDED -> {
                return true
            }

            else -> {
                return false
            }
        }
    }

    companion object {
        fun from(status: String): ExchangeStatus {
            return ExchangeStatus.values().first { it.type == status }
        }
    }
}