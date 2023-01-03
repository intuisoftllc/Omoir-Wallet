package com.intuisoft.plaid.features.dashboardflow.shared.adapters.detail

import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.BindingViewHolder
import com.intuisoft.plaid.androidwrappers.ListItem
import com.intuisoft.plaid.androidwrappers.setOnSingleClickListener
import com.intuisoft.plaid.common.model.ExchangeInfoDataModel
import com.intuisoft.plaid.common.util.Constants
import com.intuisoft.plaid.common.util.SimpleCoinNumberFormat
import com.intuisoft.plaid.model.ExchangeStatus
import kotlinx.android.synthetic.main.list_item_basic_exchange_details.view.*


class BasicExchangeDetail(
    val info: ExchangeInfoDataModel,
    val onClick: (ExchangeInfoDataModel) -> Unit
) : ListItem {
    override val layoutId: Int
        get() = R.layout.list_item_basic_exchange_details

    override fun bind(holder: BindingViewHolder) {
        holder.itemView.apply {

            this.setOnSingleClickListener(Constants.Time.MIN_CLICK_INTERVAL_SHORT) {
                onClick(info)
            }

            val exchangeStatus = ExchangeStatus.from(info.status)
            if(exchangeStatus.isFinalState()) {
                from.text = formatValue(info.sendAmount, info.fromShort)
                to.text = formatValue(info.expectedReceiveAmount, info.toShort)
            } else {
                from.text = formatValue(info.expectedSendAmount, info.fromShort)
                to.text = formatValue(info.expectedReceiveAmount, info.toShort)
            }

            exchange.text = context.getString(R.string.exchange_history_id, info.id)
            status.text = info.status
            status.setTextColor(context.getColor(exchangeStatus.color))
        }
    }

    private fun formatValue(value: Double, currency: String) : String {
        if(value < 100000) {
            return SimpleCoinNumberFormat.format(value) + " $currency"
        } else {
            return SimpleCoinNumberFormat.formatSatsShort(value.toLong()) + " $currency"
        }
    }
}