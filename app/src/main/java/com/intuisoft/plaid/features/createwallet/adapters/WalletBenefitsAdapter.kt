package com.intuisoft.plaid.features.createwallet.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.intuisoft.plaid.features.createwallet.ui.AssetTransferProtocolIntroFragment
import com.intuisoft.plaid.features.createwallet.ui.NonCustodialWalletFragment
import com.intuisoft.plaid.features.createwallet.ui.PrivateAndSecureWalletFragment
import com.intuisoft.plaid.features.createwallet.ui.SwapCurrenciesWalletFragment

class WalletBenefitsAdapter(frag: Fragment) : FragmentStateAdapter(frag) {
    // Returns total number of pages
    override fun getItemCount(): Int =  NUM_ITEMS

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> NonCustodialWalletFragment()
            1 -> PrivateAndSecureWalletFragment()
            2 -> AssetTransferProtocolIntroFragment()
            3 -> SwapCurrenciesWalletFragment()
            else -> SwapCurrenciesWalletFragment()
        }
    }

    companion object {
        private const val NUM_ITEMS = 4
    }
}