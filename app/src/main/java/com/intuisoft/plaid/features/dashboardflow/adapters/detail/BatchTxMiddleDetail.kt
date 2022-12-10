package com.intuisoft.plaid.features.dashboardflow.adapters.detail

import android.view.View
import androidx.core.view.isVisible
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.BindingViewHolder
import com.intuisoft.plaid.androidwrappers.ListItem
import com.intuisoft.plaid.common.model.BatchDataModel
import com.intuisoft.plaid.common.model.ExchangeInfoDataModel
import com.intuisoft.plaid.common.model.UtxoTransfer
import com.intuisoft.plaid.common.repositories.LocalStoreRepository
import com.intuisoft.plaid.common.util.SimpleCoinNumberFormat
import com.intuisoft.plaid.features.dashboardflow.ui.AssetTransferDetailsFragment
import com.intuisoft.plaid.model.ExchangeStatus
import com.intuisoft.plaid.util.SimpleTimeFormat
import io.horizontalsystems.bitcoincore.models.TransactionInfo
import io.horizontalsystems.bitcoincore.models.TransactionStatus
import io.horizontalsystems.bitcoincore.models.TransactionType
import kotlinx.android.synthetic.main.list_item_batch_tx_middle_item.view.batch_tx
import kotlinx.android.synthetic.main.list_item_batch_tx_top_item.view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class BatchTxMiddleDetail(
    var utxo: UtxoTransfer,
    val onTxClicked: (String) -> Unit
) : ListItem {
    override val layoutId: Int
        get() = R.layout.list_item_batch_tx_middle_item

    private var view: View? = null

    override fun bind(holder: BindingViewHolder) {
        holder.itemView.apply {
            view = this

            batch_tx.setOnClickListener {
                if(isValid()) {
                    onTxClicked(utxo.txId)
                }
            }

            update(utxo)
        }
    }

    private fun isValid() = utxo.txId.isNotBlank() && utxo.feeRate > 0

    fun update(utxo: UtxoTransfer) {
        this.utxo = utxo

        view?.apply {
            if(utxo.txId.isNotBlank())
                batch_tx.text = utxo.txId
            else batch_tx.text = context.getString(R.string.tbd)

            if(isValid()) {
                batch_tx.setTextColor(context.getColor(R.color.brand_color_dark_blue))
            } else {
                batch_tx.setTextColor(context.getColor(R.color.text_grey))
            }
        }
    }
}