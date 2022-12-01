package com.intuisoft.plaid.features.dashboardflow.adapters.detail

import android.view.View
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.BindingViewHolder
import com.intuisoft.plaid.androidwrappers.ListItem
import com.intuisoft.plaid.androidwrappers.loadUrl
import com.intuisoft.plaid.common.local.db.SupportedCurrency
import com.intuisoft.plaid.common.model.ExchangeInfoDataModel
import com.intuisoft.plaid.common.network.blockchair.response.SupportedCurrencyModel
import com.intuisoft.plaid.common.repositories.LocalStoreRepository
import com.intuisoft.plaid.common.util.SimpleCoinNumberFormat
import com.intuisoft.plaid.model.ExchangeStatus
import com.intuisoft.plaid.util.SimpleTimeFormat
import io.horizontalsystems.bitcoincore.models.TransactionInfo
import io.horizontalsystems.bitcoincore.models.TransactionStatus
import io.horizontalsystems.bitcoincore.models.TransactionType
import kotlinx.android.synthetic.main.list_item_basic_exchange_details.view.*
import kotlinx.android.synthetic.main.list_item_basic_transaction_detail.view.*
import kotlinx.android.synthetic.main.list_item_supported_currency_details.view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class SupportedCurrencyDetail(
    val currency: SupportedCurrencyModel,
    val onClick: (SupportedCurrencyModel) -> Unit
) : ListItem {
    override val layoutId: Int
        get() = R.layout.list_item_supported_currency_details

    override fun bind(holder: BindingViewHolder) {
        holder.itemView.apply {

            this.setOnClickListener {
                onClick(currency)
            }

            icon.loadUrl(currency.image)
            ticker.text = currency.ticker
            name.text = currency.name
        }
    }
}