package com.intuisoft.plaid.features.dashboardflow.shared.adapters.detail

import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.BindingViewHolder
import com.intuisoft.plaid.androidwrappers.ListItem
import com.intuisoft.plaid.util.SimpleTimeFormat
import kotlinx.android.synthetic.main.list_item_basic_transaction_detail.view.*
import kotlinx.android.synthetic.main.list_item_section_info.view.*
import java.time.Instant
import java.util.*


class ExchangeDateDetail(
    val date: Instant
) : ListItem {
    override val layoutId: Int
        get() = R.layout.list_item_section_info

    override fun bind(holder: BindingViewHolder) {
        holder.itemView.apply {
            when {
                SimpleTimeFormat.isToday(date.toEpochMilli()) -> {
                    item_info.text = context.getString(R.string.today)
                }

                SimpleTimeFormat.isYesterday(date.toEpochMilli()) -> {
                    item_info.text = context.getString(R.string.yesterday)
                }

                SimpleTimeFormat.isSameYear(date.toEpochMilli()) -> {
                    item_info.text = SimpleTimeFormat.getShortHistoryDateByLocale(date.toEpochMilli(), Locale.US.language)
                }

                else -> {
                    item_info.text = SimpleTimeFormat.getLongHistoryDateByLocale(date.toEpochMilli(), Locale.US.language)
                }
            }
        }
    }
}