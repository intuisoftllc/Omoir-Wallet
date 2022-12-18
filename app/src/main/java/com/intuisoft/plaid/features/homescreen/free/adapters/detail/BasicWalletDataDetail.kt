package com.intuisoft.plaid.features.homescreen.free.adapters.detail

import android.view.View
import androidx.lifecycle.LifecycleOwner
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.BindingViewHolder
import com.intuisoft.plaid.androidwrappers.ListItem
import com.intuisoft.plaid.androidwrappers.setOnSingleClickListener
import com.intuisoft.plaid.model.LocalWalletModel
import com.intuisoft.plaid.common.repositories.LocalStoreRepository
import com.intuisoft.plaid.common.util.Constants
import kotlinx.android.synthetic.main.list_item_basic_wallet_detail.view.*


class BasicWalletDataDetail(
    val wallet: LocalWalletModel,
    val onClick: (LocalWalletModel) -> Unit,
    val localStoreRepository: LocalStoreRepository
) : ListItem {
    override val layoutId: Int
        get() = R.layout.list_item_basic_wallet_detail
    var view: View? = null

    override fun bind(holder: BindingViewHolder) {
        holder.itemView.apply {
            view = this

            this.setOnSingleClickListener(Constants.Time.MIN_CLICK_INTERVAL_SHORT) {
                onClick(wallet)
            }

            name.text = wallet.name
            wallet.walletStateOrType(stateOrType, 0)
        }
    }

    fun onWalletStateUpdated() {
        view?.let {
            wallet.walletStateOrType(it.findViewById(R.id.stateOrType), wallet.syncPercentage)
        }
    }
}