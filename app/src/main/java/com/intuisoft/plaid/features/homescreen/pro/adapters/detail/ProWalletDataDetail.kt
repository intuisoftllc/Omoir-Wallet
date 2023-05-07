package com.intuisoft.plaid.features.homescreen.pro.adapters.detail

import android.view.View
import android.widget.TextView
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.BindingViewHolder
import com.intuisoft.plaid.androidwrappers.ListItem
import com.intuisoft.plaid.androidwrappers.setOnSingleClickListener
import com.intuisoft.plaid.common.delegates.wallet.btc.LocalWalletModel
import com.intuisoft.plaid.common.repositories.LocalStoreRepository
import com.intuisoft.plaid.common.util.Constants
import kotlinx.android.synthetic.main.list_item_pro_wallet_detail.view.*


class ProWalletDataDetail(
    val wallet: LocalWalletModel,
    val onClick: (LocalWalletModel) -> Unit,
    val localStoreRepository: LocalStoreRepository
) : ListItem {
    override val layoutId: Int
        get() = R.layout.list_item_pro_wallet_detail
    var view: View? = null

    override fun bind(holder: BindingViewHolder) {
        holder.itemView.apply {
            view = this

            this.setOnSingleClickListener(Constants.Time.MIN_CLICK_INTERVAL_MED) {
                onClick(wallet)
            }

            name.text = wallet.name
            balance.text = wallet.getBalance(localStoreRepository, true)
            wallet.walletStateOrType(stateOrType, 0)
        }
    }

    fun onWalletStateUpdated() {
        view?.let {
            wallet.walletStateOrType(it.findViewById(R.id.stateOrType), wallet.syncPercentage)
            it.findViewById<TextView>(R.id.balance).text = wallet.getBalance(localStoreRepository, true)
        }
    }
}