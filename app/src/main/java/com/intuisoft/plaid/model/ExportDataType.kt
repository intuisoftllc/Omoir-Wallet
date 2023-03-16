package com.intuisoft.plaid.model

import com.intuisoft.plaid.R

enum class ExportDataType(val displayName: Int) {
    RAW(R.string.export_wallet_options_transaction_type_subtitle_1),
    INCOMING(R.string.export_wallet_options_transaction_type_subtitle_2),
    OUTGOING(R.string.export_wallet_options_transaction_type_subtitle_3)
}