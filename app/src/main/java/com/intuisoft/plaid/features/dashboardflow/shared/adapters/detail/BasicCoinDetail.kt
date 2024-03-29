package com.intuisoft.plaid.features.dashboardflow.shared.adapters.detail

import android.view.View
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.BindingViewHolder
import com.intuisoft.plaid.androidwrappers.ListItem
import com.intuisoft.plaid.androidwrappers.setOnSingleClickListener
import com.intuisoft.plaid.common.util.Constants
import com.intuisoft.plaid.common.util.RateConverter
import io.horizontalsystems.bitcoincore.storage.UnspentOutput
import kotlinx.android.synthetic.main.list_item_basic_coin_detail.view.*


class BasicCoinDetail(
    val coin: UnspentOutput,
    val onClick: (UnspentOutput) -> Unit
) : ListItem {
    override val layoutId: Int
        get() = R.layout.list_item_basic_coin_detail

    private var view: View? = null

    override fun bind(holder: BindingViewHolder) {
        holder.itemView.apply {
            view = this

            this.setOnSingleClickListener(Constants.Time.MIN_CLICK_INTERVAL_SHORT) {
                onClick(coin)
            }

            val converter = RateConverter(0.0)
            converter.setLocalRate(RateConverter.RateType.SATOSHI_RATE, coin.output.value.toDouble())

            address.text = coin.output.address
            amount.text = converter.from(RateConverter.RateType.SATOSHI_RATE, "").second
        }
    }
}