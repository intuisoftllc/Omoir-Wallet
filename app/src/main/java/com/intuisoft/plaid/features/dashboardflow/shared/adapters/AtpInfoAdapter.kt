package com.intuisoft.plaid.features.dashboardflow.shared.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.intuisoft.plaid.features.dashboardflow.shared.ui.HowDoesAtpWorkFragment
import com.intuisoft.plaid.features.dashboardflow.shared.ui.WhatIsAtpFragment
import com.intuisoft.plaid.features.dashboardflow.shared.ui.YourInFullControlFragment

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