package com.intuisoft.plaid.features.dashboardscreen.adapters.detail

import com.intuisoft.plaid.androidwrappers.BindingViewHolder
import com.intuisoft.plaid.androidwrappers.ListItem
import com.intuisoft.plaid.androidwrappers.SettingsItemView
import com.intuisoft.plaid.repositories.LocalStoreRepository
import io.horizontalsystems.bitcoincore.storage.UnspentOutput


class UnspentOutputDetail(
    val utxo: UnspentOutput,
    val onClick: (UnspentOutput, Boolean) -> Unit,
    private val localStoreRepository: LocalStoreRepository
) : ListItem {
    override val layoutId: Int
        get() = 0//R.layout.coin_control_list_item
    private var view: SettingsItemView? = null
    private var isChecked = false

    override fun bind(holder: BindingViewHolder) {
        holder.itemView.apply {
//            this@UnspentOutputDetail.view = utxoItem
//
//            utxoItem.setTitleText(SimpleCoinNumberFormat.format(localStoreRepository, utxo.output.value, true))
//            utxoItem.setSubTitleText(utxo.output.address ?: "?")
//
//            setChecked(isChecked)
//            utxoItem.onClick {
//                val checked = !it.isChecked()
//                setChecked(checked)
//            }
        }
    }

    fun setChecked(checked: Boolean) {
        isChecked = checked

        if(view != null) {
            view!!.showCheck(checked)
            onClick(utxo, checked)
        }
    }
}