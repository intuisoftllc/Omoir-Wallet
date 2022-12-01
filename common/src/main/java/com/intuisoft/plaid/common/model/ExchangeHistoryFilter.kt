package com.intuisoft.plaid.common.model

enum class ExchangeHistoryFilter(val typeId: Int) {
    ALL(0),
    FINISHED(1),
    FAILED(2)
}