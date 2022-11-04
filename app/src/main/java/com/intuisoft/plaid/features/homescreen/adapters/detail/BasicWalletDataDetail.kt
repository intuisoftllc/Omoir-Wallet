package com.intuisoft.plaid.features.homescreen.adapters.detail

import android.view.View
import androidx.lifecycle.LifecycleOwner
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.BindingViewHolder
import com.intuisoft.plaid.androidwrappers.ListItem
import com.intuisoft.plaid.model.LocalWalletModel
import com.intuisoft.plaid.repositories.LocalStoreRepository
import kotlinx.android.synthetic.main.list_item_basic_wallet_detail.view.*


class BasicWalletDataDetail(
    val wallet: LocalWalletModel,
    val onClick: (LocalWalletModel) -> Unit,
    val localStoreRepository: LocalStoreRepository
) : ListItem {
    override val layoutId: Int
        get() = R.layout.list_item_basic_wallet_detail
    lateinit var view: View

    private val lifecycleOwner by lazy {
        view?.rootView.context as? LifecycleOwner
    }

    override fun bind(holder: BindingViewHolder) {
        holder.itemView.apply {
            view = this

            this.setOnClickListener {
                onClick(wallet)
            }

            name.text = wallet.name
            wallet.walletStateOrType(stateOrType, 0)

            lifecycleOwner?.let {
                wallet.walletStateUpdated.observe(it, androidx.lifecycle.Observer {
                    wallet.walletStateOrType(stateOrType, it)
                })
            }
        }
    }
}