package com.intuisoft.plaid.features.dashboardflow.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.intuisoft.plaid.features.createwallet.ui.EscapePodProtocolIntroFragment
import com.intuisoft.plaid.features.createwallet.ui.NonCustodialWalletFragment
import com.intuisoft.plaid.features.createwallet.ui.PrivateAndSecureWalletFragment
import com.intuisoft.plaid.features.createwallet.ui.SwapCurrenciesWalletFragment
import com.intuisoft.plaid.features.dashboardflow.ui.HowDoesAtpWorkFragment
import com.intuisoft.plaid.features.dashboardflow.ui.WhatIsAtpFragment
import com.intuisoft.plaid.features.dashboardflow.ui.YourInFullControlFragment

class AtpInfoAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
    // Returns total number of pages
    override fun getItemCount(): Int =  NUM_ITEMS

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> WhatIsAtpFragment()
            1 -> HowDoesAtpWorkFragment()
            else -> YourInFullControlFragment()
        }
    }

    companion object {
        private const val NUM_ITEMS = 3
    }
}