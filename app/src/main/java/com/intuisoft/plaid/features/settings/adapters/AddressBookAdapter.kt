package com.intuisoft.plaid.features.settings.adapters

import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.RecyclerView
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.BindingViewHolder
import com.intuisoft.plaid.databinding.ListItemSavedAddressBinding
import com.intuisoft.plaid.features.settings.adapters.detail.SavedAddressDetail
import com.intuisoft.plaid.model.SavedAddressModel
import com.intuisoft.plaid.repositories.LocalStoreRepository
import io.horizontalsystems.bitcoincore.models.TransactionInfo


class AddressBookAdapter(
    private val onAddressSelected: (SavedAddressModel) -> Unit,
    private val onCopyAddress: (SavedAddressModel) -> Unit
) : RecyclerView.Adapter<BindingViewHolder>() {

    var transactions = arrayListOf<SavedAddressDetail>()
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
        transactions[position].bind(holder)
        setAnimation(holder.itemView, position)
        lastPosition = position
    }

    override fun getItemCount(): Int = transactions.size

    override fun getItemViewType(position: Int): Int {
        return transactions[position].layoutId
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

    fun addAddresses(items: ArrayList<SavedAddressModel>) {
        transactions.clear()
        transactions.addAll(items.mapIndexed { index, transaction ->
            SavedAddressDetail(transaction, onAddressSelected, onCopyAddress)
        })

        notifyDataSetChanged()
    }
}