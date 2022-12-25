package com.intuisoft.plaid.features.dashboardflow.shared.adapters.detail

import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.BindingViewHolder
import com.intuisoft.plaid.androidwrappers.ListItem
import com.intuisoft.plaid.common.model.SavedAccountModel
import com.intuisoft.plaid.common.model.SavedAddressModel
import io.horizontalsystems.hdwalletkit.HDWallet
import kotlinx.android.synthetic.main.list_item_saved_address.view.*


class SavedAccountItemDetail(
    val savedAccount: SavedAccountModel,
    val onClick: (SavedAccountModel) -> Unit
) : ListItem {
    override val layoutId: Int
        get() = R.layout.list_item_saved_address

    override fun bind(holder: BindingViewHolder) {
        holder.itemView.apply {
            saved_address.setTitleText(savedAccount.accountName)
            saved_address.setSubTitleText("${savedAccount.account}")

            saved_address.onClick {
                onClick(savedAccount)
            }
        }
    }
}