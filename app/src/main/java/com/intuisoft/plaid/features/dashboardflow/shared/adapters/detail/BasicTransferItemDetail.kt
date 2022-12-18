package com.intuisoft.plaid.features.dashboardflow.shared.adapters.detail

import android.view.View
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.BindingViewHolder
import com.intuisoft.plaid.androidwrappers.ListItem
import com.intuisoft.plaid.androidwrappers.setOnSingleClickListener
import com.intuisoft.plaid.common.model.AssetTransferModel
import com.intuisoft.plaid.common.repositories.LocalStoreRepository
import com.intuisoft.plaid.common.util.Constants
import com.intuisoft.plaid.common.util.SimpleCoinNumberFormat
import com.intuisoft.plaid.features.dashboardflow.shared.ui.AssetTransferDetailsFragment
import kotlinx.android.synthetic.main.list_item_basic_atp_history_item.view.*


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

            AssetTransferDetailsFragment.showATPStatus(
                context,
                status,
                transfer.status
            )

            history_item_container.setOnSingleClickListener(Constants.Time.MIN_CLICK_INTERVAL_SHORT) {
                onClick(transfer)
            }
        }
    }
}