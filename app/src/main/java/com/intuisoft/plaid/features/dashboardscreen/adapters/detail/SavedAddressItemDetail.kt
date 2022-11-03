package com.intuisoft.plaid.features.dashboardscreen.adapters.detail

import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.BindingViewHolder
import com.intuisoft.plaid.androidwrappers.ListItem
import com.intuisoft.plaid.androidwrappers.SettingsItemView
import com.intuisoft.plaid.model.SavedAddressInfo
import com.intuisoft.plaid.model.SavedAddressModel
import com.intuisoft.plaid.repositories.LocalStoreRepository
import com.intuisoft.plaid.util.SimpleCoinNumberFormat
import io.horizontalsystems.bitcoincore.storage.UnspentOutput
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