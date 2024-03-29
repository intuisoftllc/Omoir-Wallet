package com.intuisoft.plaid.features.homescreen.pro.adapters

import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.RecyclerView
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.BindingViewHolder
import com.intuisoft.plaid.databinding.ListItemBasicWalletDetailBinding
import com.intuisoft.plaid.features.homescreen.free.adapters.detail.BasicWalletDataDetail
import com.intuisoft.plaid.model.LocalWalletModel
import com.intuisoft.plaid.common.repositories.LocalStoreRepository
import com.intuisoft.plaid.databinding.ListItemProWalletDetailBinding
import com.intuisoft.plaid.features.homescreen.pro.adapters.detail.ProWalletDataDetail


class ProWalletDataAdapter(
    private val onWalletSelected: (LocalWalletModel) -> Unit,
    private val localStoreRepository: LocalStoreRepository
) : RecyclerView.Adapter<BindingViewHolder>() {

    var wallets = arrayListOf<ProWalletDataDetail>()
    private var lastPosition = -1

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BindingViewHolder {
        return when (viewType) {
            R.layout.list_item_pro_wallet_detail -> {
                BindingViewHolder.create(parent, ListItemProWalletDetailBinding::inflate)
            }
            else -> throw IllegalArgumentException("Invalid BindingViewHolder Type")
        }
    }

    override fun onBindViewHolder(holder: BindingViewHolder, position: Int) {
        wallets[position].bind(holder)
        setAnimation(holder.itemView, position)
        lastPosition = position
    }

    override fun getItemCount(): Int = wallets.size

    override fun getItemViewType(position: Int): Int {
        return wallets[position].layoutId
    }

    private fun setAnimation(view: View, position: Int) {
        if (position > lastPosition) {
            val animation: Animation =
                AnimationUtils.loadAnimation(view.context, R.anim.fall_down)
            view.startAnimation(animation)
        } else {
            view.clearAnimation()
        }
    }

    fun updateConversion() {
        wallets.forEach {
            it.onWalletStateUpdated()
        }
    }

    fun onWalletStateUpdated(wallet: LocalWalletModel) {
        wallets.find { it.wallet.uuid == wallet.uuid }?.onWalletStateUpdated()
    }

    fun addWallets(items: ArrayList<LocalWalletModel>) {
        wallets.clear()
        wallets.addAll(items.mapIndexed { index, wallet ->
            ProWalletDataDetail(wallet, onWalletSelected, localStoreRepository)
        })

        notifyDataSetChanged()
    }
}