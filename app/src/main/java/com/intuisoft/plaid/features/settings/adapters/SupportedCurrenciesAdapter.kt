package com.intuisoft.plaid.features.settings.adapters

import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.RecyclerView
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.BindingViewHolder
import com.intuisoft.plaid.databinding.ListItemSavedAddressBinding
import com.intuisoft.plaid.databinding.ListItemSupportedCurrencyBinding
import com.intuisoft.plaid.features.settings.adapters.detail.SavedAddressDetail
import com.intuisoft.plaid.features.settings.adapters.detail.SupportedCurrencyDetail


class SupportedCurrenciesAdapter(
    private val currencySelected: (String) -> Unit
) : RecyclerView.Adapter<BindingViewHolder>() {

    var items = arrayListOf<SupportedCurrencyDetail>()
    private var lastPosition = -1

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BindingViewHolder {
        return when (viewType) {
            R.layout.list_item_supported_currency -> {
                BindingViewHolder.create(parent, ListItemSupportedCurrencyBinding::inflate)
            }
            else -> throw IllegalArgumentException("Invalid BindingViewHolder Type")
        }
    }

    override fun onBindViewHolder(holder: BindingViewHolder, position: Int) {
        items[position].bind(holder)
        setAnimation(holder.itemView, position)
        lastPosition = position
    }

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int): Int {
        return items[position].layoutId
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

    private fun onCurrencySelected(currencyCode: String) {
        items.forEach {
            if(it.selected) it.updateSelectedCurrency(false)
        }

        currencySelected(currencyCode)
    }

    fun addCurrencies(items: ArrayList<String>, initialCurrency: String) {
        this.items.clear()
        this.items.addAll(items.map {
            SupportedCurrencyDetail(it, it == initialCurrency, ::onCurrencySelected)
        })

        notifyDataSetChanged()
    }
}