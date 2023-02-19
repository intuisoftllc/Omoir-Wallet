package com.intuisoft.plaid.features.dashboardflow.shared.adapters.detail

import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.BindingViewHolder
import com.intuisoft.plaid.androidwrappers.ListItem
import kotlinx.android.synthetic.main.list_item_section_info.view.*


class ListSectionInfoDetail(
    val info: String
) : ListItem {
    override val layoutId: Int
        get() = R.layout.list_item_section_info

    override fun bind(holder: BindingViewHolder) {
        holder.itemView.apply {
            item_info.text = info
        }
    }
}