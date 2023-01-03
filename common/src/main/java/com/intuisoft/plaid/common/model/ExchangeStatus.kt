package com.intuisoft.plaid.common.model


enum class ExchangeStatus(val type: String) { // todo: replace these with string res ids
    NEW("new"),
    WAITING("waiting"),
    CONFIRMING("confirming"),
    EXCHANGING("exchanging"),
    SENDING("sending"),
    FINISHED("finished"),
    FAILED("failed"),
    REFUNDED("refunded"),
    VERIFYING("verifying");

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
            return values().first { it.type == status }
        }
    }
}