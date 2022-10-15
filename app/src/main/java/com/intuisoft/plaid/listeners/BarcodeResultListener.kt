package com.intuisoft.plaid.listeners

interface BarcodeResultListener {

    fun onAddressReceived(address: String)
}