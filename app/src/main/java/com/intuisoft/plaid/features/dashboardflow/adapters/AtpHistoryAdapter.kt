package com.intuisoft.plaid.features.dashboardflow.adapters

import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.RecyclerView
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.BindingViewHolder
import com.intuisoft.plaid.androidwrappers.ListItem
import com.intuisoft.plaid.common.model.AssetTransferModel
import com.intuisoft.plaid.common.repositories.LocalStoreRepository
import com.intuisoft.plaid.common.util.Constants
import com.intuisoft.plaid.databinding.ListItemBasicAtpHistoryItemBinding
import com.intuisoft.plaid.databinding.ListItemSectionInfoBinding
import com.intuisoft.plaid.features.dashboardflow.adapters.detail.BasicTransferItemDetail
import com.intuisoft.plaid.features.dashboardflow.adapters.detail.ExchangeDateDetail
import java.time.Instant


class AtpHistoryAdapter(
    private val onItemSelected: (AssetTransferModel) -> Unit,
    private val getWalletName: (String) -> String,
    private val localStoreRepository: LocalStoreRepository
) : RecyclerView.Adapter<BindingViewHolder>() {

    var items = arrayListOf<ListItem>()
    private var lastPosition = -1

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BindingViewHolder {
        return when (viewType) {
            R.layout.list_item_basic_atp_history_item -> {
                BindingViewHolder.create(parent, ListItemBasicAtpHistoryItemBinding::inflate)
            }
            R.layout.list_item_section_info -> {
                BindingViewHolder.create(parent, ListItemSectionInfoBinding::inflate)
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

    fun addOrUpdate(items: ArrayList<AssetTransferModel>) {
        val transfersCount = this.items.count {
            when(it) {
                is BasicTransferItemDetail -> {
                    true
                }
                else -> {
                    false
                }
            }
        }

        if(transfersCount == items.size) {
            var itemIndex = 0
            this.items.forEach {
                when(it) {
                    is BasicTransferItemDetail -> {
                        it.onUpdate(items[itemIndex++])
                    }
                }
            }
        } else {
            addItems(items)
        }
    }

    private fun addItems(items: ArrayList<AssetTransferModel>) {
        this.items.clear()
        var lastDate: Long = 0

        items.forEachIndexed { index, transfer ->
            val date = (transfer.createdAt / Constants.Time.MILLS_PER_SEC) / Constants.Time.SECONDS_PER_DAY

            if(date != lastDate) {
                lastDate = date
                this.items.add(ExchangeDateDetail(Instant.ofEpochMilli(transfer.createdAt)))
            }

            this.items.add(BasicTransferItemDetail(transfer, onItemSelected, getWalletName, localStoreRepository))
        }

        notifyDataSetChanged()
    }
}