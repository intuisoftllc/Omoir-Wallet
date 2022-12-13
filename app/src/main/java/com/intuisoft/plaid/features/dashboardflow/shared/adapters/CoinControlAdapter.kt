package com.intuisoft.plaid.features.homescreen.adapters

import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.RecyclerView
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.BindingViewHolder
import com.intuisoft.plaid.databinding.ListItemSavedAddressBinding
import com.intuisoft.plaid.features.dashboardflow.shared.adapters.detail.UnspentOutputDetail
import com.intuisoft.plaid.common.repositories.LocalStoreRepository
import com.intuisoft.plaid.common.util.extensions.remove
import io.horizontalsystems.bitcoincore.storage.UnspentOutput


class CoinControlAdapter(
    var localStoreRepository: LocalStoreRepository,
    private val onAllItemsSelected: (Boolean) -> Unit,
    private val onItemLongClicked: (UnspentOutput) -> Unit
) : RecyclerView.Adapter<BindingViewHolder>() {

    var utxoDetail = arrayListOf<UnspentOutputDetail>()
    var selectedUTXOs = mutableListOf<UnspentOutput>()
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
        utxoDetail[position].bind(holder)
        setAnimation(holder.itemView, position)
        lastPosition = position
    }

    override fun getItemCount(): Int = utxoDetail.size

    override fun getItemViewType(position: Int): Int {
        return utxoDetail[position].layoutId
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

    fun selectAll(select: Boolean) {
        utxoDetail.forEach {
            it.setChecked(select)
        }
    }

    fun onUtxoSelected(utxo: UnspentOutput, selected: Boolean) {
        if(selected && selectedUTXOs.find { it.output.address == utxo.output.address } == null) {
            selectedUTXOs.add(utxo)
        } else if(!selected) {
            selectedUTXOs.remove { it.output.address == utxo.output.address }
        }

        onAllItemsSelected(areAllItemsSelected())
    }

    fun onUtxoLongClicked(utxo: UnspentOutput) {
        onItemLongClicked(utxo)
    }

    fun areAllItemsSelected() = selectedUTXOs.size == utxoDetail.size

    fun addUTXOs(items: ArrayList<UnspentOutput>, selectedItems: ArrayList<UnspentOutput>) {
        utxoDetail.clear()
        utxoDetail.addAll(items.mapIndexed { index, utxo ->
            val detail = UnspentOutputDetail(utxo, ::onUtxoSelected, ::onUtxoLongClicked, localStoreRepository)
            if(selectedItems.find { it.output.address == utxo.output.address } != null) {
                detail.setChecked(true)
            }

            detail
        })

        notifyDataSetChanged()
    }
}