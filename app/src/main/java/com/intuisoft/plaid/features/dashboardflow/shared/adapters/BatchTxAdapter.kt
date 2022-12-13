package com.intuisoft.plaid.features.dashboardflow.shared.adapters

import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.RecyclerView
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.BindingViewHolder
import com.intuisoft.plaid.androidwrappers.ListItem
import com.intuisoft.plaid.common.model.AssetTransferModel
import com.intuisoft.plaid.common.model.BatchDataModel
import com.intuisoft.plaid.common.model.UtxoTransfer
import com.intuisoft.plaid.common.repositories.LocalStoreRepository
import com.intuisoft.plaid.common.util.Constants
import com.intuisoft.plaid.databinding.ListItemBasicAtpHistoryItemBinding
import com.intuisoft.plaid.databinding.ListItemBasicBatchInfoItemBinding
import com.intuisoft.plaid.databinding.ListItemBatchTxBottomItemBinding
import com.intuisoft.plaid.databinding.ListItemBatchTxMiddleItemBinding
import com.intuisoft.plaid.databinding.ListItemBatchTxTopItemBinding
import com.intuisoft.plaid.databinding.ListItemSectionInfoBinding
import com.intuisoft.plaid.features.dashboardflow.shared.adapters.detail.BatchTxBottomDetail
import com.intuisoft.plaid.features.dashboardflow.shared.adapters.detail.BatchTxMiddleDetail
import com.intuisoft.plaid.features.dashboardflow.shared.adapters.detail.BatchTxTopDetail
import java.time.Instant


class BatchTxAdapter(
    private val onTransactionClicked: (String) -> Unit
) : RecyclerView.Adapter<BindingViewHolder>() {

    var items = arrayListOf<ListItem>()
    private var lastPosition = -1

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BindingViewHolder {
        return when (viewType) {
            R.layout.list_item_batch_tx_top_item -> {
                BindingViewHolder.create(parent, ListItemBatchTxTopItemBinding::inflate)
            }
            R.layout.list_item_batch_tx_middle_item -> {
                BindingViewHolder.create(parent, ListItemBatchTxMiddleItemBinding::inflate)
            }
            R.layout.list_item_batch_tx_bottom_item -> {
                BindingViewHolder.create(parent, ListItemBatchTxBottomItemBinding::inflate)
            }
            else -> throw IllegalArgumentException("Invalid BindingViewHolder Type")
        }
    }

    override fun onBindViewHolder(holder: BindingViewHolder, position: Int) {
        items[position].bind(holder)
        lastPosition = position
    }

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int): Int {
        return items[position].layoutId
    }

    fun addOrUpdateItems(items: ArrayList<UtxoTransfer>) {
        if(this.items.size == items.size) {
            this.items.forEachIndexed { index, item ->
                when(item) {
                    is BatchTxTopDetail -> {
                        item.update(items[index])
                    }
                    is BatchTxBottomDetail -> {
                        item.update(items[index])
                    }
                    is BatchTxMiddleDetail -> {
                        item.update(items[index])
                    }
                }
            }
        } else {
            addItems(items)
        }
    }

    private fun addItems(items: ArrayList<UtxoTransfer>) {
        this.items.clear()

        if(items.size == 1) {
            this.items.add(BatchTxBottomDetail(items[0], onTransactionClicked))
        } else if(items.size == 2) {
            this.items.add(BatchTxTopDetail(items[0], onTransactionClicked))
            this.items.add(BatchTxBottomDetail(items[0], onTransactionClicked))
        } else {
            items.forEachIndexed { index, utxoTransfer ->
                if(index == 0) {
                    this.items.add(BatchTxTopDetail(items[index], onTransactionClicked))
                } else if(index == (items.size - 1)) {
                    this.items.add(BatchTxBottomDetail(items[index], onTransactionClicked))
                } else {
                    this.items.add(BatchTxMiddleDetail(items[index], onTransactionClicked))
                }
            }
        }

        notifyDataSetChanged()
    }
}