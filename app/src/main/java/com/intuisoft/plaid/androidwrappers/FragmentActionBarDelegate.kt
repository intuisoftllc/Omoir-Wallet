package com.intuisoft.plaid.androidwrappers

import androidx.annotation.StringRes

interface FragmentActionBarDelegate {
    fun showActionBar() : Boolean
    fun actionBarTitle() : Int
}