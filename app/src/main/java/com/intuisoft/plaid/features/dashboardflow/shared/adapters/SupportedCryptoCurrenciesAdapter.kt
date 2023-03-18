package com.intuisoft.plaid.features.homescreen.adapters

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.RecyclerView
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.BindingViewHolder
import com.intuisoft.plaid.androidwrappers.ListItem
import com.intuisoft.plaid.common.network.blockchair.response.SupportedCurrencyModel
import com.intuisoft.plaid.databinding.ListItemSectionInfoBinding
import com.intuisoft.plaid.databinding.ListItemSupportedCryptoCurrencyBinding
import com.intuisoft.plaid.features.dashboardflow.shared.adapters.detail.ListSectionInfoDetail
import com.intuisoft.plaid.features.dashboardflow.shared.adapters.detail.SupportedCryptoCurrencyDetail


class SupportedCryptoCurrenciesAdapter(
    private val onCurrencySelected: (SupportedCurrencyModel) -> Unit
) : RecyclerView.Adapter<BindingViewHolder>() {

    var currencies = arrayListOf<ListItem>()
    private var lastPosition = -1

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BindingViewHolder {
        return when (viewType) {
            R.layout.list_item_supported_crypto_currency -> {
                BindingViewHolder.create(parent, ListItemSupportedCryptoCurrencyBinding::inflate)
            }
            R.layout.list_item_section_info -> {
                BindingViewHolder.create(parent, ListItemSectionInfoBinding::inflate)
            }
            else -> throw IllegalArgumentException("Invalid BindingViewHolder Type")
        }
    }

    override fun onBindViewHolder(holder: BindingViewHolder, position: Int) {
        currencies[position].bind(holder)
        setAnimation(holder.itemView, position)
        lastPosition = position
    }

    override fun getItemCount(): Int = currencies.size

    override fun getItemViewType(position: Int): Int {
        return currencies[position].layoutId
    }

    private fun setAnimation(view: View, position: Int) {
        if (position > lastPosition) {
            val animation: Animation =
                AnimationUtils.loadAnimation(view.context, R.anim.fall_down)
            view.startAnimation(animation)
        } else {
            view.clearAnimation()
        }
    }

    fun addCurrencies(context: Context, mostUsed: ArrayList<SupportedCurrencyModel>, items: ArrayList<SupportedCurrencyModel>) {
        currencies.clear()

        if(mostUsed.isNotEmpty()) {
            currencies.add(ListSectionInfoDetail(context.getString(R.string.search_currencies_info_title_1)))

            mostUsed.forEach {
                currencies.add(SupportedCryptoCurrencyDetail(it, onCurrencySelected))
            }
        }

        currencies.add(ListSectionInfoDetail(context.getString(R.string.search_currencies_info_title_2, items.size.toString())))

        items.forEach {
            currencies.add(SupportedCryptoCurrencyDetail(it, onCurrencySelected))
        }

        notifyDataSetChanged()
    }
}