package com.intuisoft.plaid.androidwrappers

import androidx.annotation.StringRes

interface ActionBarDelegate {
    var isActionBarShowing: Boolean
    fun showActionBarTitle()
    fun hideActionBarTitle()
    var actionBarTitle: CharSequence?
    fun setActionBarTitle(@StringRes title: Int)
}