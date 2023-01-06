package com.intuisoft.plaid.androidwrappers.delegates

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

interface ActionBarDelegate {
    val isActionBarShowing: Boolean
    fun setActionBarTitle(title: String)
    fun setActionBarSubTitle(title: String)
    fun setActionBarActionLeft(@DrawableRes action: Int)
    fun setActionBarActionRight(@DrawableRes action: Int)
    fun setActionBarVariant(variant: Int)
}