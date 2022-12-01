package com.intuisoft.plaid.features.dashboardflow.adapters.detail

import android.view.View
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.BindingViewHolder
import com.intuisoft.plaid.androidwrappers.ListItem
import com.intuisoft.plaid.common.model.ExchangeInfoDataModel
import com.intuisoft.plaid.common.repositories.LocalStoreRepository
import com.intuisoft.plaid.common.util.SimpleCoinNumberFormat
import com.intuisoft.plaid.model.ExchangeStatus
import com.intuisoft.plaid.util.SimpleTimeFormat
import io.horizontalsystems.bitcoincore.models.TransactionInfo
import io.horizontalsystems.bitcoincore.models.TransactionStatus
import io.horizontalsystems.bitcoincore.models.TransactionType
import kotlinx.android.synthetic.main.list_item_basic_exchange_details.view.*
import kotlinx.android.synthetic.main.list_item_basic_transaction_detail.view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class BasicExchangeDetail(
    val info: ExchangeInfoDataModel,
    val onClick: (ExchangeInfoDataModel) -> Unit
) : ListItem {
    override val layoutId: Int
        get() = R.layout.list_item_basic_exchange_details

    override fun bind(holder: BindingViewHolder) {
        holder.itemView.apply {

            this.setOnClickListener {
                onClick(info)
            }

            from.text = formatValue(info.sendAmount, info.fromShort)
            to.text = formatValue(info.receiveAmount, info.toShort)
            exchange.text = context.getString(R.string.exchange_history_id, info.id)
            status.text = info.status
            status.setTextColor(context.getColor(ExchangeStatus.values().find { it.type == info.status }?.color ?: R.color.text_grey))
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