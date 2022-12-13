package com.intuisoft.plaid.features.dashboardflow.shared.adapters

import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.RecyclerView
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.BindingViewHolder
import com.intuisoft.plaid.databinding.ListItemSavedAddressBinding
import com.intuisoft.plaid.common.model.SavedAddressModel
import com.intuisoft.plaid.features.dashboardflow.shared.adapters.detail.SavedAddressItemDetail


class AddressBookAdapter(
    private val onItemSelected: (String) -> Unit
) : RecyclerView.Adapter<BindingViewHolder>() {

    var addressDetail = arrayListOf<SavedAddressItemDetail>()
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
        addressDetail[position].bind(holder)
        setAnimation(holder.itemView, position)
        lastPosition = position
    }

    override fun getItemCount(): Int = addressDetail.size

    override fun getItemViewType(position: Int): Int {
        return addressDetail[position].layoutId
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

    fun addSavedAddresses(items: ArrayList<SavedAddressModel>) {
        addressDetail.clear()
        addressDetail.addAll(items.mapIndexed { index, savedAddress ->
            SavedAddressItemDetail(savedAddress, onItemSelected)
        })

        notifyDataSetChanged()
    }
}