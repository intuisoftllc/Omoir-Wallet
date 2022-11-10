package com.intuisoft.plaid.features.homescreen.adapters

import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.RecyclerView
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.BindingViewHolder
import com.intuisoft.plaid.databinding.ListItemBasicTransactionDetailBinding
import com.intuisoft.plaid.features.dashboardscreen.adapters.detail.BasicTransactionDetail
import com.intuisoft.plaid.common.repositories.LocalStoreRepository
import io.horizontalsystems.bitcoincore.models.TransactionInfo


class BasicTransactionAdapter(
    private val onTransactionSelected: (TransactionInfo) -> Unit,
    private val getConfirmationsForTransaction: (TransactionInfo) -> Int,
    private val localStoreRepository: LocalStoreRepository
) : RecyclerView.Adapter<BindingViewHolder>() {

    var transactions = arrayListOf<BasicTransactionDetail>()
    private var lastPosition = -1

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BindingViewHolder {
        return when (viewType) {
            R.layout.list_item_basic_transaction_detail -> {
                BindingViewHolder.create(parent, ListItemBasicTransactionDetailBinding::inflate)
            }
            else -> throw IllegalArgumentException("Invalid BindingViewHolder Type")
        }
    }

    override fun onBindViewHolder(holder: BindingViewHolder, position: Int) {
        transactions[position].bind(holder)
        setAnimation(holder.itemView, position)
        lastPosition = position
    }

    override fun getItemCount(): Int = transactions.size

    override fun getItemViewType(position: Int): Int {
        return transactions[position].layoutId
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

    fun updateConversion() {
        transactions.forEach {
            it.onConversionUpdated()
        }
    }

    fun addTransactions(items: ArrayList<TransactionInfo>) {
        transactions.clear()
        transactions.addAll(items.mapIndexed { index, transaction ->
            BasicTransactionDetail(transaction, onTransactionSelected, getConfirmationsForTransaction, localStoreRepository)
        })

        notifyDataSetChanged()
    }
}