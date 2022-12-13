package com.intuisoft.plaid.features.dashboardflow.shared.adapters.detail

import android.view.View
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.BindingViewHolder
import com.intuisoft.plaid.androidwrappers.ListItem
import com.intuisoft.plaid.androidwrappers.copyToClipboard
import com.intuisoft.plaid.common.util.Constants
import com.intuisoft.plaid.common.util.RateConverter
import com.intuisoft.plaid.features.dashboardflow.shared.viewModel.AtpViewModel
import com.intuisoft.plaid.util.Plural
import kotlinx.android.synthetic.main.list_item_utxo_transfer.view.*
import kotlinx.coroutines.*


class UxtoTransferDetail(
    val utxoData: AtpViewModel.UtxoData
) : ListItem {
    override val layoutId: Int
        get() = R.layout.list_item_utxo_transfer

    private var view: View? = null

    override fun bind(holder: BindingViewHolder) {
        holder.itemView.apply {
            view = this

            val rate = RateConverter(0.0)
            rate.setLocalRate(RateConverter.RateType.SATOSHI_RATE, utxoData.totalFee.toDouble())
            val fee: String

            if(utxoData.totalFee <= 0) {
                when(utxoData.totalFee) {
                    -2L -> {
                        fee = context.getString(R.string.atp_utxo_transfer_error_1)
                    }
                    -1L -> {
                        fee = context.getString(R.string.atp_utxo_transfer_error_2)
                    }
                    else -> { // 0
                        fee = context.getString(R.string.atp_utxo_transfer_error_3)
                    }
                }
            } else {
                fee = context.getString(
                    R.string.atp_utxo_transfer_fee_amount,
                    rate.from(RateConverter.RateType.SATOSHI_RATE, "", false).second,
                    Plural.of("sat", utxoData.feeRate.toLong())
                )
            }

            this.setOnClickListener {
                MainScope().launch {
                    utxo_data?.showCheck(true)
                    utxo_data?.showCopy(false)
                    context.copyToClipboard(
                        """
                            ${utxoData.utxo.output.address!!}: 
                            ${fee}
                        """.trimIndent(),
                        "Utxo"
                    )
                    delay(Constants.Time.ITEM_COPY_DELAY.toLong())
                    utxo_data?.showCheck(false)
                    utxo_data?.showCopy(true)
                }

            }

            utxo_data.setSubTitleText(utxoData.utxo.output.address!!)
            utxo_data.setTitleText(
                fee
            )
        }
    }
}