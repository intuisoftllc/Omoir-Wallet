package com.intuisoft.plaid.listeners

interface NetworkStateChangeListener {

    fun onStateChanged(hasNetwork: Boolean)
}