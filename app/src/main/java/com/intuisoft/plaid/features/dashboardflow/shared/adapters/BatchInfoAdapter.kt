package com.intuisoft.plaid.features.dashboardflow.shared.adapters

import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.RecyclerView
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.BindingViewHolder
import com.intuisoft.plaid.common.model.BatchDataModel
import com.intuisoft.plaid.common.repositories.LocalStoreRepository
import com.intuisoft.plaid.databinding.ListItemBasicBatchInfoItemBinding
import com.intuisoft.plaid.features.dashboardflow.shared.adapters.detail.BasicBatchInfoDetail


class BatchInfoAdapter(
    private val localStoreRepository: LocalStoreRepository,
    private val onTransactionClicked: (String) -> Unit
) : RecyclerView.Adapter<BindingViewHolder>() {

    var items = arrayListOf<BasicBatchInfoDetail>()
    private var lastPosition = -1

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BindingViewHolder {
        return when (viewType) {
            R.layout.list_item_basic_batch_info_item -> {
                BindingViewHolder.create(parent, ListItemBasicBatchInfoItemBinding::inflate)
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

    fun addOrUpdateItems(items: ArrayList<BatchDataModel>) {
        if(this.items.size == items.size) {
            this.items.forEachIndexed { index, it ->
                it.onUpdate(items[index])
            }
        } else {
            addItems(items)
        }
    }

    private fun addItems(items: ArrayList<BatchDataModel>) {
        this.items.clear()

        items.forEachIndexed { index, batch ->
            this.items.add(BasicBatchInfoDetail(batch, onTransactionClicked))
        }

        notifyDataSetChanged()
    }
}