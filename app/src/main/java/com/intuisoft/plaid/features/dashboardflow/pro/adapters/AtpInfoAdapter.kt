package com.intuisoft.plaid.features.dashboardflow.pro.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.intuisoft.plaid.features.dashboardflow.pro.ui.HowDoesAtpWorkFragment
import com.intuisoft.plaid.features.dashboardflow.pro.ui.WhatIsAtpFragment
import com.intuisoft.plaid.features.dashboardflow.pro.ui.YourInFullControlFragment

class AtpInfoAdapter(frga: Fragment) : FragmentStateAdapter(frga) {
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