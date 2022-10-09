package com.intuisoft.plaid.androidwrappers

import androidx.annotation.LayoutRes

interface ListItem {
    @get:LayoutRes
    val layoutId: Int
    fun bind(holder: BindingViewHolder)
}