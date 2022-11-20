package com.intuisoft.plaid.features.dashboardflow.adapters.detail

import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.BindingViewHolder
import com.intuisoft.plaid.androidwrappers.ListItem
import com.intuisoft.plaid.common.model.SavedAddressModel
import kotlinx.android.synthetic.main.list_item_saved_address.view.*


class SavedAddressItemDetail(
    val savedAddress: SavedAddressModel,
    val onClick: (String) -> Unit
) : ListItem {
    override val layoutId: Int
        get() = R.layout.list_item_saved_address

    override fun bind(holder: BindingViewHolder) {
        holder.itemView.apply {
            saved_address.setTitleText(savedAddress.addressName)
            saved_address.setSubTitleText(savedAddress.address)

            saved_address.onClick {
                onClick(savedAddress.address)
            }
        }
    }
}