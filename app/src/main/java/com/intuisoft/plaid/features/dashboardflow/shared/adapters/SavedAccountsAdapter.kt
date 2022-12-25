package com.intuisoft.plaid.features.dashboardflow.shared.adapters

import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.RecyclerView
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.BindingViewHolder
import com.intuisoft.plaid.common.model.SavedAccountModel
import com.intuisoft.plaid.databinding.ListItemSavedAddressBinding
import com.intuisoft.plaid.common.model.SavedAddressModel
import com.intuisoft.plaid.features.dashboardflow.shared.adapters.detail.SavedAccountItemDetail
import com.intuisoft.plaid.features.dashboardflow.shared.adapters.detail.SavedAddressItemDetail
import io.horizontalsystems.hdwalletkit.HDWallet


class SavedAccountsAdapter(
    private val onItemSelected: (SavedAccountModel) -> Unit
) : RecyclerView.Adapter<BindingViewHolder>() {

    var items = arrayListOf<SavedAccountItemDetail>()
    private var lastPosition = -1

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BindingViewHolder {
        return when (viewType) {
            R.layout.list_item_saved_address -> {
                BindingViewHolder.create(parent, ListItemSavedAddressBinding::inflate)
            }
            else -> throw IllegalArgumentException("Invalid BindingViewHolder Type")
        }
    }

    override fun onBindViewHolder(holder: BindingViewHolder, position: Int) {
        items[position].bind(holder)
        setAnimation(holder.itemView, position)
        lastPosition = position
    }

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int): Int {
        return items[position].layoutId
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

    fun addSavedAccounts(items: ArrayList<SavedAccountModel>) {
        this.items.clear()
        this.items.addAll(items.mapIndexed { index, savedAccount ->
            SavedAccountItemDetail(savedAccount, onItemSelected)
        })

        notifyDataSetChanged()
    }
}