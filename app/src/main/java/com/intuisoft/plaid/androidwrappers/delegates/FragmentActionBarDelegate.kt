package com.intuisoft.plaid.androidwrappers.delegates

interface FragmentActionBarDelegate {
    fun actionBarVariant() : Int
    fun actionBarTitle() : Int
    fun actionBarSubtitle() : Int
    fun actionBarActionLeft() : Int
    fun actionBarActionRight() : Int
    fun onActionLeft()
    fun onActionRight()
    fun onSubtitleClicked()
}