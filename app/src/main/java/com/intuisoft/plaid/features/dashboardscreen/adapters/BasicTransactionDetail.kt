package com.intuisoft.plaid.features.homescreen.adapters

import android.graphics.PorterDuff
import android.view.View
import androidx.lifecycle.LifecycleOwner
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.BindingViewHolder
import com.intuisoft.plaid.androidwrappers.ListItem
import com.intuisoft.plaid.androidwrappers.styledSnackBar
import com.intuisoft.plaid.model.BitcoinDisplayUnit
import com.intuisoft.plaid.model.LocalWalletModel
import com.intuisoft.plaid.model.WalletType
import com.intuisoft.plaid.repositories.LocalStoreRepository
import com.intuisoft.plaid.util.Constants
import com.intuisoft.plaid.util.SimpleCoinNumberFormat
import com.intuisoft.plaid.util.SimpleTimeFormat
import io.horizontalsystems.bitcoincore.models.TransactionInfo
import io.horizontalsystems.bitcoincore.models.TransactionType
import kotlinx.android.synthetic.main.basic_transaction_detail_list_item.view.*
import java.text.NumberFormat


class BasicTransactionDetail(
    val transaction: TransactionInfo,
    val onClick: (TransactionInfo) -> Unit,
    val localStoreRepository: LocalStoreRepository
) : ListItem {
    override val layoutId: Int
        get() = R.layout.basic_transaction_detail_list_item

    override fun bind(holder: BindingViewHolder) {
        holder.itemView.apply {
            this.setOnClickListener {
                onClick(transaction)
            }

            when(transaction.type) {
                TransactionType.Incoming -> {
                    sendReceiveIndicator.setImageResource(R.drawable.ic_recieve_coins_48)
                }

                TransactionType.Outgoing -> {
                    sendReceiveIndicator.setImageResource(R.drawable.ic_send_coins_48)
                }

                TransactionType.SentToSelf -> {
                    sendReceiveIndicator.setImageResource(R.drawable.ic_sent_coins_to_self_48)
                }
            }

            transactionTime.text = SimpleTimeFormat.timeToString(transaction.timestamp)
            when(localStoreRepository.getBitcoinDisplayUnit()) {
                BitcoinDisplayUnit.BTC -> {
                    transactionAmount.text = "" + SimpleCoinNumberFormat.format(transaction.amount.toDouble() / Constants.Limit.SATS_PER_BTC.toDouble()) + " BTC"
                }

                BitcoinDisplayUnit.SATS -> {
                    transactionAmount.text = "" + SimpleCoinNumberFormat.format(transaction.amount) + " Sats"
                }
            }
        }
    }
}