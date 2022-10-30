package com.intuisoft.plaid.features.dashboardscreen.adapters.detail

import com.intuisoft.plaid.androidwrappers.BindingViewHolder
import com.intuisoft.plaid.androidwrappers.ListItem
import com.intuisoft.plaid.repositories.LocalStoreRepository
import io.horizontalsystems.bitcoincore.models.TransactionInfo


class BasicTransactionDetail(
    val transaction: TransactionInfo,
    val onClick: (TransactionInfo) -> Unit,
    val localStoreRepository: LocalStoreRepository
) : ListItem {
    override val layoutId: Int
        get() = 0 //R.layout.basic_transaction_detail_list_item

    override fun bind(holder: BindingViewHolder) {
        holder.itemView.apply {
            this.setOnClickListener {
                onClick(transaction)
            }

//            if(transaction.blockHeight == null) {
//                if(transaction.status == TransactionStatus.INVALID) {
//                    sendReceiveIndicator.setImageResource(R.drawable.ic_error_invalid)
//                } else {
//                    sendReceiveIndicator.setImageResource(R.drawable.ic_pending_48)
//                }
//            } else {
//                when (transaction.type) {
//                    TransactionType.Incoming -> {
//                        sendReceiveIndicator.setImageResource(R.drawable.ic_recieve_coins_48)
//                    }
//
//                    TransactionType.Outgoing -> {
//                        sendReceiveIndicator.setImageResource(R.drawable.ic_send_coins_48)
//                    }
//
//                    TransactionType.SentToSelf -> {
//                        sendReceiveIndicator.setImageResource(R.drawable.group_40263)
//                    }
//                }
//            }
//
//            transactionTime.text = SimpleTimeFormat.timeToString(transaction.timestamp)
//            transactionAmount.text = SimpleCoinNumberFormat.format(localStoreRepository, transaction.amount)
        }
    }
}