package com.intuisoft.plaid.features.dashboardscreen.adapters.detail

import android.view.View
import androidx.lifecycle.LifecycleOwner
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.BindingViewHolder
import com.intuisoft.plaid.androidwrappers.ListItem
import com.intuisoft.plaid.model.LocalWalletModel
import com.intuisoft.plaid.common.repositories.LocalStoreRepository
import kotlinx.android.synthetic.main.list_item_basic_wallet_detail.view.*


class TransferToWalletDetail(
    val wallet: LocalWalletModel,
    val onClick: (LocalWalletModel) -> Unit,
    val localStoreRepository: LocalStoreRepository
) : ListItem {
    override val layoutId: Int
        get() = R.layout.list_item_basic_wallet_detail

    override fun bind(holder: BindingViewHolder) {
        holder.itemView.apply {

            this.setOnClickListener {
                onClick(wallet)
            }

            name.text = wallet.name
            view_details.text = context.getString(R.string.withdraw_confirmation_transfer_list_item_message)
            wallet.walletType(stateOrType)
        }
    }
}