package com.intuisoft.plaid.features.homescreen.adapters

import android.graphics.PorterDuff
import android.view.View
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.findViewTreeLifecycleOwner
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.BindingViewHolder
import com.intuisoft.plaid.androidwrappers.ListItem
import com.intuisoft.plaid.androidwrappers.styledSnackBar
import com.intuisoft.plaid.model.BitcoinDisplayUnit
import com.intuisoft.plaid.model.LocalWalletModel
import com.intuisoft.plaid.model.WalletState
import com.intuisoft.plaid.model.WalletType
import com.intuisoft.plaid.repositories.LocalStoreRepository
import com.intuisoft.plaid.util.Constants
import com.intuisoft.plaid.util.SimpleCoinNumberFormat
import kotlinx.android.synthetic.main.basic_wallet_list_item.view.*


class BasicWalletDataDetail(
    val wallet: LocalWalletModel,
    val onClick: (LocalWalletModel) -> Unit,
    val localStoreRepository: LocalStoreRepository
) : ListItem {
    override val layoutId: Int
        get() = R.layout.basic_wallet_list_item
    lateinit var view: View

    private val lifecycleOwner by lazy{
        view?.rootView.context as? LifecycleOwner
    }

    override fun bind(holder: BindingViewHolder) {
        holder.itemView.apply {
            view = this

            try {
                this.setOnClickListener {
                    onClick(wallet)
                }

                if(wallet.testNetWallet) {
                    if (wallet.type == WalletType.READ_ONLY) {
                        walletTypeImg.drawable.mutate().setColorFilter(
                            context.getColor(R.color.brand_color_accent_3),
                            PorterDuff.Mode.SRC_ATOP
                        )
                    } else {
                        walletTypeImg.drawable.mutate().setColorFilter(
                            context.getColor(R.color.viridian),
                            PorterDuff.Mode.SRC_ATOP
                        )
                    }
                } else {
                    if (wallet.type == WalletType.READ_ONLY) {
                        walletTypeImg.drawable.mutate().setColorFilter(
                            context.getColor(R.color.grey),
                            PorterDuff.Mode.SRC_ATOP
                        )
                    } else {
                        walletTypeImg.drawable.mutate().setColorFilter(
                            context.getColor(R.color.bitcoin_color),
                            PorterDuff.Mode.SRC_ATOP
                        )
                    }
                }

                walletName.text = wallet.name
                walletBalance.text = getWalletBalance()

                lifecycleOwner?.let {
                    wallet.walletStateUpdated.observe(it, androidx.lifecycle.Observer {
                        onWalletStateChanged()
                    })
                }
            } catch (error: java.lang.Exception) {
                styledSnackBar(this.rootView, "Oops an error occurred: ${error}")
            }
        }
    }

    private fun getWalletBalance(): String {
        if(localStoreRepository.getBitcoinDisplayUnit() == BitcoinDisplayUnit.BTC) {
            return "" //+ wallet.wallet!!.balance / Constants.Limit.SATS_PER_BTC + " BTC"
        } else {
            val balance = 0L//wallet.wallet?.balance?.value ?: 0L
            val postfix = if(balance == 1L) "Sat" else "Sats"

            return SimpleCoinNumberFormat.format(balance) + " " + postfix
        }
    }

    private fun onWalletStateChanged() {
        view.apply {
            when(wallet.walletState) {
                WalletState.SYNCING -> {
                    walletBalance.text = Constants.Strings.SYNCING
                } else -> {
                    walletBalance.text = getWalletBalance()
                }
            }
        }
    }
}