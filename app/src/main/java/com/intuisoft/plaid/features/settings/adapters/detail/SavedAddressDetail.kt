package com.intuisoft.plaid.features.settings.adapters.detail

import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.BindingViewHolder
import com.intuisoft.plaid.androidwrappers.ListItem
import com.intuisoft.plaid.common.model.SavedAddressModel
import kotlinx.android.synthetic.main.list_item_saved_address.view.*


class SavedAddressDetail(
    val savedAddress: SavedAddressModel,
    val onClick: (SavedAddressModel) -> Unit,
    val onCopy: (SavedAddressModel) -> Unit
) : ListItem {
    override val layoutId: Int
        get() = R.layout.list_item_saved_address

    override fun bind(holder: BindingViewHolder) {
        holder.itemView.apply {
            this.setOnClickListener {
                onClick(savedAddress)
            }

            this.setOnLongClickListener {
                onCopy(savedAddress)
                true
            }

            saved_address.setTitleText(savedAddress.addressName)
            saved_address.setSubTitleText(savedAddress.address)
        }
    }
}