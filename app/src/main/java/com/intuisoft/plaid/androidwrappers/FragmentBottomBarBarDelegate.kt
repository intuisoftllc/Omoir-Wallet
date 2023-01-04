package com.intuisoft.plaid.androidwrappers

interface FragmentBottomBarBarDelegate {
    fun onNavigateTo(destination: Int)
    fun navigationId() : Int
    fun onBackPressed()
}