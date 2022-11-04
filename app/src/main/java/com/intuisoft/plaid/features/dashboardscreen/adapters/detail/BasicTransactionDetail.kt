package com.intuisoft.plaid.features.dashboardscreen.adapters.detail

import android.view.View
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.BindingViewHolder
import com.intuisoft.plaid.androidwrappers.ListItem
import com.intuisoft.plaid.repositories.LocalStoreRepository
import com.intuisoft.plaid.util.SimpleCoinNumberFormat
import com.intuisoft.plaid.util.SimpleTimeFormat
import io.horizontalsystems.bitcoincore.models.TransactionInfo
import io.horizontalsystems.bitcoincore.models.TransactionStatus
import io.horizontalsystems.bitcoincore.models.TransactionType
import kotlinx.android.synthetic.main.list_item_basic_transaction_detail.view.*


class BasicTransactionDetail(
    val transaction: TransactionInfo,
    val onClick: (TransactionInfo) -> Unit,
    val getConfirmations: (TransactionInfo) -> Int,
    val localStoreRepository: LocalStoreRepository
) : ListItem {
    override val layoutId: Int
        get() = R.layout.list_item_basic_transaction_detail

    private var view: View? = null

    override fun bind(holder: BindingViewHolder) {
        holder.itemView.apply {
            view = this

            this.setOnClickListener {
                onClick(transaction)
            }

            if(getConfirmations(transaction) < localStoreRepository.getMinimumConfirmations()) {
                if(transaction.status == TransactionStatus.INVALID) {
                    transaction_status_indicator.setImageResource(R.drawable.ic_alert_red)
                } else {
                    transaction_status_indicator.setImageResource(R.drawable.ic_transaction_pending)
                }
            } else {
                when (transaction.type) {
                    TransactionType.Incoming -> {
                        transaction_status_indicator.setImageResource(R.drawable.ic_incoming)
                    }

                    TransactionType.Outgoing -> {
                        transaction_status_indicator.setImageResource(R.drawable.ic_outgoing)
                    }

                    TransactionType.SentToSelf -> {
                        transaction_status_indicator.setImageResource(R.drawable.ic_sent_to_self)
                    }
                }
            }

            time_passed.text = SimpleTimeFormat.timeToString(transaction.timestamp)
            onConversionUpdated()
        }
    }

    fun onConversionUpdated() {
        view?.apply {
            if(transaction.type == TransactionType.SentToSelf && transaction.fee != null) {
                transaction_amount.text = "-${SimpleCoinNumberFormat.format(localStoreRepository, transaction.fee!!)}"
            } else {
                transaction_amount.text = SimpleCoinNumberFormat.format(localStoreRepository, transaction.amount)
            }
        }
    }
}