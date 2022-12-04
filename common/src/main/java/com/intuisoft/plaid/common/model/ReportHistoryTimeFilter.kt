package com.intuisoft.plaid.common.model

enum class ReportHistoryTimeFilter(val typeId: Int) {
    LAST_WEEK(0),
    LAST_MONTH(1),
    LAST_6MONTHS(2),
    LAST_YEAR(3),
    ALL_TIME(4)
}