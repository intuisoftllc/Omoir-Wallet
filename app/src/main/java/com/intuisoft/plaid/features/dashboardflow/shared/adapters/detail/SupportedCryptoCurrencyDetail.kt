package com.intuisoft.plaid.features.dashboardflow.shared.adapters.detail

import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.BindingViewHolder
import com.intuisoft.plaid.androidwrappers.ListItem
import com.intuisoft.plaid.androidwrappers.loadUrl
import com.intuisoft.plaid.androidwrappers.setOnSingleClickListener
import com.intuisoft.plaid.common.network.blockchair.response.SupportedCurrencyModel
import com.intuisoft.plaid.common.util.Constants
import kotlinx.android.synthetic.main.list_item_basic_exchange_details.view.*
import kotlinx.android.synthetic.main.list_item_basic_transaction_detail.view.*
import kotlinx.android.synthetic.main.list_item_supported_crypto_currency.view.*


class SupportedCryptoCurrencyDetail(
    val currency: SupportedCurrencyModel,
    val onClick: (SupportedCurrencyModel) -> Unit
) : ListItem {
    override val layoutId: Int
        get() = R.layout.list_item_supported_crypto_currency

    override fun bind(holder: BindingViewHolder) {
        holder.itemView.apply {

            this.setOnSingleClickListener(Constants.Time.MIN_CLICK_INTERVAL_SHORT) {
                onClick(currency)
            }

            icon.loadUrl(currency.image)
            ticker.text = currency.ticker
            name.text = currency.name
        }
    }
}