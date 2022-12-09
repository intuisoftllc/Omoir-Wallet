package com.intuisoft.plaid.features.dashboardflow.adapters.detail

import android.view.View
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.BindingViewHolder
import com.intuisoft.plaid.androidwrappers.ListItem
import com.intuisoft.plaid.common.model.AssetTransferModel
import com.intuisoft.plaid.common.model.AssetTransferStatus
import com.intuisoft.plaid.common.model.SavedAddressModel
import com.intuisoft.plaid.common.repositories.LocalStoreRepository
import com.intuisoft.plaid.common.util.SimpleCoinNumberFormat
import com.intuisoft.plaid.common.util.SimpleCurrencyFormat
import kotlinx.android.synthetic.main.list_item_basic_atp_history_item.view.*
import kotlinx.android.synthetic.main.list_item_saved_address.view.*


class BasicTransferItemDetail(
    var transfer: AssetTransferModel,
    val onClick: (AssetTransferModel) -> Unit,
    val getWalletName: (String) -> String,
    val localStoreRepository: LocalStoreRepository
) : ListItem {
    override val layoutId: Int
        get() = R.layout.list_item_basic_atp_history_item

    var view: View? = null

    override fun bind(holder: BindingViewHolder) {
        holder.itemView.apply {
            view = this
            onUpdate(transfer)
        }
    }

    fun onUpdate(assetTransfer: AssetTransferModel) {
        this.transfer = assetTransfer
        view?.apply {
            send_amount.text = SimpleCoinNumberFormat.format(localStoreRepository, transfer.expectedAmount)
            wallet_name.text = getWalletName(transfer.walletId)

            when(transfer.status) {
                AssetTransferStatus.NOT_STARTED -> {
                    status.text = context.getString(R.string.atp_status_not_started)
                    status.setTextColor(context.getColor(R.color.text_grey))
                }

                AssetTransferStatus.IN_PROGRESS -> {
                    status.text = context.getString(R.string.atp_status_in_progress)
                    status.setTextColor(context.getColor(R.color.text_grey))
                }

                AssetTransferStatus.PARTIALLY_COMPLETED -> {
                    status.text = context.getString(R.string.atp_status_partially_completed)
                    status.setTextColor(context.getColor(R.color.warning_color))
                }

                AssetTransferStatus.COMPLETED -> {
                    status.text = context.getString(R.string.atp_status_completed)
                    status.setTextColor(context.getColor(R.color.success_color))
                }

                AssetTransferStatus.FAILED -> {
                    status.text = context.getString(R.string.atp_status_failed)
                    status.setTextColor(context.getColor(R.color.error_color))
                }

                AssetTransferStatus.CANCELLED -> {
                    status.text = context.getString(R.string.atp_status_cancelled)
                    status.setTextColor(context.getColor(R.color.error_color))
                }

            }

            history_item_container.setOnClickListener {
                onClick(transfer)
            }
        }
    }
}