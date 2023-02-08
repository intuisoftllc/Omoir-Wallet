package com.intuisoft.plaid.billing

import android.app.Activity
import android.util.Log
import com.android.billingclient.api.Purchase
import com.intuisoft.plaid.common.local.AppPrefs
import com.revenuecat.purchases.*
import com.revenuecat.purchases.interfaces.ReceiveCustomerInfoCallback
import org.koin.java.KoinJavaComponent.inject

class BillingManager(
    private val prefs: AppPrefs
) {
    private val TAG = "BillingManager"
    private val premiumUserEntitlement = "pro"

    fun checkEntitlement(callback: ((Boolean) -> Unit)? = null) {
        Purchases.sharedInstance.getCustomerInfo(object : ReceiveCustomerInfoCallback {
            override fun onError(error: PurchasesError) {
                // do nothing
            }

            override fun onReceived(customerInfo: CustomerInfo) {
                prefs.isPremiumUser = subscriptionActive(customerInfo)
                callback?.invoke(prefs.isPremiumUser)
            }

        })
    }

    fun subscriptionActive(customerInfo: CustomerInfo) =
        customerInfo.entitlements[premiumUserEntitlement]?.isActive == true

    fun getProducts(callback: (List<com.revenuecat.purchases.Package>?) -> Unit) {
        Purchases.sharedInstance.getOfferingsWith({ error ->
            callback(null)
        }) { offerings ->
            offerings.current?.availablePackages?.takeUnless { it.isNullOrEmpty() }?.let {
                // Display packages for sale
                Log.i(TAG, "packages: $it")
                callback(it)
            }
        }
    }
}