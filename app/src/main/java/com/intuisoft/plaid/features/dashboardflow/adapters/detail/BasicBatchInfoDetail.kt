package com.intuisoft.plaid.features.dashboardflow.adapters.detail

import android.view.View
import androidx.core.view.isVisible
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.BindingViewHolder
import com.intuisoft.plaid.androidwrappers.ListItem
import com.intuisoft.plaid.common.model.BatchDataModel
import com.intuisoft.plaid.common.model.ExchangeInfoDataModel
import com.intuisoft.plaid.common.repositories.LocalStoreRepository
import com.intuisoft.plaid.common.util.SimpleCoinNumberFormat
import com.intuisoft.plaid.common.util.extensions.toArrayList
import com.intuisoft.plaid.features.dashboardflow.adapters.BatchTxAdapter
import com.intuisoft.plaid.features.dashboardflow.ui.AssetTransferDetailsFragment
import com.intuisoft.plaid.model.ExchangeStatus
import com.intuisoft.plaid.util.SimpleTimeFormat
import io.horizontalsystems.bitcoincore.models.TransactionInfo
import io.horizontalsystems.bitcoincore.models.TransactionStatus
import io.horizontalsystems.bitcoincore.models.TransactionType
import kotlinx.android.synthetic.main.list_item_basic_batch_info_item.view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


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
            adapter.addOrUpdateItems(batch.utxos.toArrayList())

            batch_number.text = context.getString(R.string.atp_batch_number, "${batch.batchNumber + 1}")

            AssetTransferDetailsFragment.showATPStatus(
                context,
                status,
                batch.status
            )
        }
    }
}