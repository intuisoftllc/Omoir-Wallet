package com.intuisoft.plaid.androidwrappers.delegates

interface FragmentBottomBarBarDelegate {
    fun onNavigateTo(destination: Int)
    fun navigationId() : Int
    fun showBottomBar(): Boolean
    fun onBackPressed()
}