package com.intuisoft.plaid.features.homescreen.adapters

import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.RecyclerView
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.BindingViewHolder
import com.intuisoft.plaid.databinding.ListItemUtxoTransferBinding
import com.intuisoft.plaid.features.dashboardflow.shared.adapters.detail.*
import com.intuisoft.plaid.features.dashboardflow.shared.viewModel.AtpViewModel


class UtxoTransfersAdapter() : RecyclerView.Adapter<BindingViewHolder>() {

    var utxos = arrayListOf<UxtoTransferDetail>()
    private var lastPosition = -1

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BindingViewHolder {
        return when (viewType) {
            R.layout.list_item_utxo_transfer -> {
                BindingViewHolder.create(parent, ListItemUtxoTransferBinding::inflate)
            }
            else -> throw IllegalArgumentException("Invalid BindingViewHolder Type")
        }
    }

    override fun onBindViewHolder(holder: BindingViewHolder, position: Int) {
        utxos[position].bind(holder)
        setAnimation(holder.itemView, position)
        lastPosition = position
    }

    override fun getItemCount(): Int = utxos.size

    override fun getItemViewType(position: Int): Int {
        return utxos[position].layoutId
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

    fun addUtxos(items: ArrayList<AtpViewModel.UtxoData>) {
        utxos.clear()

        items.forEach {
            utxos.add(UxtoTransferDetail(it))
        }

        notifyDataSetChanged()
    }
}