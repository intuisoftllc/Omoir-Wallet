package com.intuisoft.plaid.features.dashboardflow.shared.adapters.detail

import android.view.View
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.BindingViewHolder
import com.intuisoft.plaid.androidwrappers.ListItem
import com.intuisoft.plaid.androidwrappers.setOnSingleClickListener
import com.intuisoft.plaid.common.model.AssetTransferStatus
import com.intuisoft.plaid.common.model.BatchDataModel
import com.intuisoft.plaid.common.model.UtxoTransfer
import com.intuisoft.plaid.common.util.Constants
import kotlinx.android.synthetic.main.list_item_batch_tx_bottom_item.view.batch_tx
import kotlinx.android.synthetic.main.list_item_batch_tx_top_item.view.*


class BatchTxBottomDetail(
    var utxo: UtxoTransfer,
    var batch: BatchDataModel,
    val onTxClicked: (String) -> Unit
) : ListItem {
    override val layoutId: Int
        get() = R.layout.list_item_batch_tx_bottom_item

    private var view: View? = null

    override fun bind(holder: BindingViewHolder) {
        holder.itemView.apply {
            view = this

            batch_tx.setOnSingleClickListener(Constants.Time.MIN_CLICK_INTERVAL_SHORT) {
                if(isValid()) {
                    onTxClicked(utxo.txId)
                }
            }

            update(batch, utxo)
        }
    }

    private fun isValid() = utxo.txId.isNotBlank() && utxo.feeRate > 0

    fun update(batch: BatchDataModel, utxo: UtxoTransfer) {
        this.utxo = utxo

        view?.apply {
            if(utxo.txId.isNotBlank())
                batch_tx.text = utxo.txId
            else {
                if(batch.status == AssetTransferStatus.CANCELLED)
                    batch_tx.text = context.getString(R.string.not_applicable)
                else
                    batch_tx.text = context.getString(R.string.tbd)
            }

            if(isValid()) {
                batch_tx.setTextColor(context.getColor(R.color.brand_color_dark_blue))
            } else {
                batch_tx.setTextColor(context.getColor(R.color.text_grey))
            }
        }
    }
}