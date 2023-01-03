package com.intuisoft.plaid.model

import com.intuisoft.plaid.R
import com.intuisoft.plaid.common.model.ExchangeStatus


enum class ExchangeStatusColors(val status: ExchangeStatus, val color: Int) {
    STATUS_1(ExchangeStatus.NEW, R.color.subtitle_text_color),
    STATUS_2(ExchangeStatus.WAITING, R.color.subtitle_text_color),
    STATUS_3(ExchangeStatus.CONFIRMING, R.color.subtitle_text_color),
    STATUS_4(ExchangeStatus.EXCHANGING, R.color.subtitle_text_color),
    STATUS_5(ExchangeStatus.SENDING, R.color.subtitle_text_color),
    STATUS_6(ExchangeStatus.FINISHED, R.color.success_color),
    STATUS_7(ExchangeStatus.FAILED, R.color.error_color),
    STATUS_8(ExchangeStatus.REFUNDED, R.color.warning_color),
    STATUS_9(ExchangeStatus.VERIFYING, R.color.subtitle_text_color);

    companion object {
        fun getColor(status: ExchangeStatus): Int {
            return values().first { it.status == status }.color
        }
    }
}