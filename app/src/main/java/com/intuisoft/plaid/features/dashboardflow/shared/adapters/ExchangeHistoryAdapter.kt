package com.intuisoft.plaid.features.homescreen.adapters

import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.RecyclerView
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.BindingViewHolder
import com.intuisoft.plaid.androidwrappers.ListItem
import com.intuisoft.plaid.common.model.ExchangeInfoDataModel
import com.intuisoft.plaid.common.repositories.LocalStoreRepository
import com.intuisoft.plaid.common.util.Constants
import com.intuisoft.plaid.databinding.ListItemBasicExchangeDetailsBinding
import com.intuisoft.plaid.databinding.ListItemSectionInfoBinding
import com.intuisoft.plaid.features.dashboardflow.shared.adapters.detail.BasicExchangeDetail
import com.intuisoft.plaid.features.dashboardflow.shared.adapters.detail.ExchangeDateDetail


class ExchangeHistoryAdapter(
    private val onExchangeSelected: (ExchangeInfoDataModel) -> Unit
) : RecyclerView.Adapter<BindingViewHolder>() {

    var exchanges = arrayListOf<ListItem>()
    private var lastPosition = -1

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BindingViewHolder {
        return when (viewType) {
            R.layout.list_item_basic_exchange_details -> {
                BindingViewHolder.create(parent, ListItemBasicExchangeDetailsBinding::inflate)
            }
            R.layout.list_item_section_info -> {
                BindingViewHolder.create(parent, ListItemSectionInfoBinding::inflate)
            }
            else -> throw IllegalArgumentException("Invalid BindingViewHolder Type")
        }
    }

    override fun onBindViewHolder(holder: BindingViewHolder, position: Int) {
        exchanges[position].bind(holder)
        setAnimation(holder.itemView, position)
        lastPosition = position
    }

    override fun getItemCount(): Int = exchanges.size

    override fun getItemViewType(position: Int): Int {
        return exchanges[position].layoutId
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

    fun addExchanges(items: ArrayList<ExchangeInfoDataModel>) {
        exchanges.clear()
        var lastDate: Long = 0

        items.forEach { exchange ->
            val date = exchange.timestamp.epochSecond / Constants.Time.SECONDS_PER_DAY

            if(date != lastDate) {
                lastDate = date
                exchanges.add(ExchangeDateDetail(exchange.timestamp))
            }

            exchanges.add(BasicExchangeDetail(exchange, onExchangeSelected))
        }

        notifyDataSetChanged()
    }
}