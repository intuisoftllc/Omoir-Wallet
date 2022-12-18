package com.intuisoft.plaid.features.settings.adapters.detail

import android.view.View
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.BindingViewHolder
import com.intuisoft.plaid.androidwrappers.ListItem
import com.intuisoft.plaid.common.model.SavedAddressModel
import com.intuisoft.plaid.common.util.Constants
import com.intuisoft.plaid.common.util.SimpleCurrencyFormat
import kotlinx.android.synthetic.main.list_item_supported_currency.view.*


class SupportedCurrencyDetail(
    val currencyCode: String,
    var selected: Boolean,
    val onClick: (String) -> Unit
) : ListItem {
    override val layoutId: Int
        get() = R.layout.list_item_supported_currency

    private var view: View? = null

    override fun bind(holder: BindingViewHolder) {
        holder.itemView.apply {
            view = this
            currency.setTitleText(SimpleCurrencyFormat.formatTypeBasic(currencyCode))
            currency.onRadioClicked { view, clicked ->
                if(clicked) {
                    onClick(currencyCode)
                    this@SupportedCurrencyDetail.selected = clicked
                    currency.checkRadio(selected)
                }
            }

            currency.checkRadio(selected)
        }
    }

    fun updateSelectedCurrency(select: Boolean) {
        this.selected = select

        view?.apply {
            currency.checkRadio(selected)
        }
    }
}