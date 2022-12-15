package com.intuisoft.plaid.features.dashboardflow.shared.adapters.detail

import android.view.View
import androidx.core.view.isVisible
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.BindingViewHolder
import com.intuisoft.plaid.androidwrappers.ListItem
import com.intuisoft.plaid.common.model.BatchDataModel
import com.intuisoft.plaid.common.util.SimpleCoinNumberFormat
import com.intuisoft.plaid.common.util.extensions.toArrayList
import com.intuisoft.plaid.features.dashboardflow.shared.adapters.BatchTxAdapter
import com.intuisoft.plaid.features.dashboardflow.shared.ui.AssetTransferDetailsFragment
import kotlinx.android.synthetic.main.list_item_basic_batch_info_item.view.*


class BasicBatchInfoDetail(
    var batch: BatchDataModel,
    private val onTransactionClicked: (String) -> Unit
) : ListItem {
    override val layoutId: Int
        get() = R.layout.list_item_basic_batch_info_item

    private val adapter = BatchTxAdapter(onTransactionClicked)
    private var view: View? = null

    override fun bind(holder: BindingViewHolder) {
        holder.itemView.apply {
            view = this

            transfers.adapter = adapter
            batch_item_container.setOnClickListener {
                transfers.isVisible = !transfers.isVisible

                if(transfers.isVisible) {
                    chevron.setImageResource(R.drawable.ic_chevron_down)
                } else {
                    chevron.setImageResource(R.drawable.ic_chevron_right)
                }
            }

            onUpdate(batch)
        }
    }

    fun onUpdate(batch: BatchDataModel) {
        this.batch = batch
        view?.apply {
            adapter.addOrUpdateItems(batch)

            batch_number.text = context.getString(R.string.atp_batch_number, SimpleCoinNumberFormat.format(batch.batchNumber.toLong() + 1))

            blocks_left.isVisible = batch.blocksRemaining > 0
            blocks_left.text = context.getString(R.string.atp_blocks_left, "${batch.blocksRemaining}")

            AssetTransferDetailsFragment.showATPStatus(
                context,
                status,
                batch.status
            )
        }
    }
}