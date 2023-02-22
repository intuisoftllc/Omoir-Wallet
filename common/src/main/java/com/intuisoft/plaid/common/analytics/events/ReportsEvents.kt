package com.intuisoft.plaid.common.analytics.events

import com.intuisoft.plaid.common.analytics.Event
import com.intuisoft.plaid.common.analytics.EventParamId
import com.intuisoft.plaid.common.model.ReportHistoryTimeFilter

class EventReportsView: Event("reports_view", listOf())

class EventReportSelected(
    val type: ReportType
): Event("report_selected", listOf(EventParamId.TYPE to type.name.lowercase()))

enum class ReportType {
    INFLOW_REPORT,
    OUTFLOW_REPORT,
    NET_INFLOW_REPORT,
    TXFEE_REPORT,
    UTXODISTRO_REPORT,
}
