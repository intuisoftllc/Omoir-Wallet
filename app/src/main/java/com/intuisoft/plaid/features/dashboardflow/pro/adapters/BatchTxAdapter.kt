package com.intuisoft.plaid.features.dashboardflow.pro.adapters

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.BindingViewHolder
import com.intuisoft.plaid.androidwrappers.ListItem
import com.intuisoft.plaid.common.model.BatchDataModel
import com.intuisoft.plaid.common.model.UtxoTransfer
import com.intuisoft.plaid.common.util.extensions.toArrayList
import com.intuisoft.plaid.databinding.ListItemBatchTxBottomItemBinding
import com.intuisoft.plaid.databinding.ListItemBatchTxMiddleItemBinding
import com.intuisoft.plaid.databinding.ListItemBatchTxTopItemBinding
import com.intuisoft.plaid.features.dashboardflow.pro.adapters.detail.BatchTxBottomDetail
import com.intuisoft.plaid.features.dashboardflow.pro.adapters.detail.BatchTxMiddleDetail
import com.intuisoft.plaid.features.dashboardflow.pro.adapters.detail.BatchTxTopDetail


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

    fun addOrUpdateItems(batch: BatchDataModel) {
        val items: ArrayList<UtxoTransfer> = batch.utxos.toArrayList()

        if(this.items.size == items.size) {
            this.items.forEachIndexed { index, item ->
                when(item) {
                    is BatchTxTopDetail -> {
                        item.update(batch, items[index])
                    }
                    is BatchTxBottomDetail -> {
                        item.update(batch, items[index])
                    }
                    is BatchTxMiddleDetail -> {
                        item.update(batch, items[index])
                    }
                }
            }
        } else {
            addItems(batch, items)
        }
    }

    private fun addItems(batch: BatchDataModel, items: ArrayList<UtxoTransfer>) {
        this.items.clear()

        if(items.size == 1) {
            this.items.add(BatchTxBottomDetail(items[0], batch, onTransactionClicked))
        } else if(items.size == 2) {
            this.items.add(BatchTxTopDetail(items[0], batch, onTransactionClicked))
            this.items.add(BatchTxBottomDetail(items[0], batch, onTransactionClicked))
        } else {
            items.forEachIndexed { index, utxoTransfer ->
                if(index == 0) {
                    this.items.add(BatchTxTopDetail(items[index], batch, onTransactionClicked))
                } else if(index == (items.size - 1)) {
                    this.items.add(BatchTxBottomDetail(items[index], batch, onTransactionClicked))
                } else {
                    this.items.add(BatchTxMiddleDetail(items[index], batch, onTransactionClicked))
                }
            }
        }

        notifyDataSetChanged()
    }
}