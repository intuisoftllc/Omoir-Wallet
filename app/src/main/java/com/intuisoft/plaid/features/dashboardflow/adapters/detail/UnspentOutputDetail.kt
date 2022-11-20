package com.intuisoft.plaid.features.dashboardflow.adapters.detail

import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.BindingViewHolder
import com.intuisoft.plaid.androidwrappers.ListItem
import com.intuisoft.plaid.androidwrappers.SettingsItemView
import com.intuisoft.plaid.common.repositories.LocalStoreRepository
import com.intuisoft.plaid.common.util.SimpleCoinNumberFormat
import io.horizontalsystems.bitcoincore.storage.UnspentOutput
import kotlinx.android.synthetic.main.list_item_saved_address.view.*


class UnspentOutputDetail(
    val utxo: UnspentOutput,
    val onClick: (UnspentOutput, Boolean) -> Unit,
    private val localStoreRepository: LocalStoreRepository
) : ListItem {
    override val layoutId: Int
        get() = R.layout.list_item_saved_address
    private var view: SettingsItemView? = null
    private var isChecked = false

    override fun bind(holder: BindingViewHolder) {
        holder.itemView.apply {
            this@UnspentOutputDetail.view = saved_address

            saved_address.setTitleText(SimpleCoinNumberFormat.format(localStoreRepository, utxo.output.value, false))
            saved_address.setSubTitleText(utxo.output.address ?: "?")

            setChecked(isChecked)
            saved_address.onClick {
                val checked = !it.isChecked()
                setChecked(checked)
            }
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