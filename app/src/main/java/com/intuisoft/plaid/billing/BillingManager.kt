package com.intuisoft.plaid.billing

import android.app.Activity
import android.util.Log
import com.android.billingclient.api.BillingFlowParams.ProrationMode
import com.android.billingclient.api.Purchase
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.intuisoft.plaid.PlaidApp
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.styledSnackBar
import com.intuisoft.plaid.common.local.AppPrefs
import com.revenuecat.purchases.*
import com.revenuecat.purchases.interfaces.ProductChangeCallback
import com.revenuecat.purchases.interfaces.PurchaseCallback
import com.revenuecat.purchases.interfaces.ReceiveCustomerInfoCallback
import com.revenuecat.purchases.models.StoreProduct
import com.revenuecat.purchases.models.StoreTransaction
import org.koin.java.KoinJavaComponent.inject
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*

class BillingManager(
    private val prefs: AppPrefs
) {
    private val TAG = "BillingManager"
    private val premiumUserEntitlement = "pro"

    fun onBackground() {
        Purchases.sharedInstance.onAppBackgrounded()
    }

    fun onForeground() {
        Purchases.sharedInstance.onAppForegrounded()
    }

    fun getUserId() = Purchases.sharedInstance.appUserID
    fun checkEntitlement(callback: ((CustomerInfo) -> Unit)? = null) {
        Log.i(TAG, "running entitlementCheck: ${Purchases.isConfigured} ${Purchases.sharedInstance}")
        Purchases.sharedInstance.getCustomerInfo(object : ReceiveCustomerInfoCallback {
            override fun onError(error: PurchasesError) {
                // do nothing
                Log.e(TAG, "error: $error")
            }

            override fun onReceived(customerInfo: CustomerInfo) {
                Log.i(TAG, "customerInfo: $customerInfo")
                prefs.isPremiumUser = subscriptionActive(customerInfo)
                callback?.invoke(customerInfo)
            }

        })
    }

    fun getCurrentSubscription(activity: Activity, customerInfo: CustomerInfo, products: List<com.revenuecat.purchases.Package>): SubscriptionInfo {
        val product = products.find { it.product.sku == customerInfo.entitlements[premiumUserEntitlement]?.productIdentifier }
        var expire: String = "?"
        try {
            if(customerInfo.entitlements[premiumUserEntitlement]!!.expirationDate!!.before(Date.from(Instant.now()))) {
                expire = activity.getString(R.string.premium_subscription_expired_state)
            } else expire =  SimpleDateFormat("MMM dd, yyyy hh:mm aa").format(customerInfo.entitlements[premiumUserEntitlement]!!.expirationDate!!)
        } catch(e: java.lang.Exception){}

        return SubscriptionInfo(
            renewalType = when {
                product?.identifier == MONTHLY_PRODUCT -> {
                    activity.getString(R.string.premium_subscription_renewal_type_1)
                }
                product?.identifier == ANNUAL_PRODUCT -> {
                    activity.getString(R.string.premium_subscription_renewal_type_2)
                }
                else -> "?"
            },
            expireDate = expire,
            state = if(isSubscriptionPaused(customerInfo)) activity.getString(R.string.premium_subscription_state_type_2)
                    else activity.getString(R.string.premium_subscription_state_type_1)
        )

    }

    fun upgradeDownGrade(
        activity: Activity,
        customerInfo: CustomerInfo,
        products: List<com.revenuecat.purchases.Package>,
        onSuccess: (CustomerInfo) -> Unit,
        onFail: (PurchasesError, Boolean) -> Unit) {
        (activity.application as PlaidApp).ignorePinCheck = true
        val monthlyProduct = products.find { it.identifier == MONTHLY_PRODUCT }
        val annualProduct = products.find { it.identifier == ANNUAL_PRODUCT }

        when {
            customerInfo.entitlements[premiumUserEntitlement]?.isActive == true &&
                    customerInfo.entitlements[premiumUserEntitlement]?.productIdentifier == monthlyProduct?.product?.sku -> {
                Purchases.sharedInstance.purchaseProduct(activity, annualProduct!!.product, UpgradeInfo(monthlyProduct!!.product!!.sku, ProrationMode.DEFERRED), object: ProductChangeCallback {
                    override fun onCompleted(
                        storeTransaction: StoreTransaction?,
                        customerInfo: CustomerInfo
                    ) {
                        onSuccess(customerInfo)
                    }

                    override fun onError(error: PurchasesError, userCancelled: Boolean) {
                        onFail(error, userCancelled)
                    }

                })
            }
            customerInfo.entitlements[premiumUserEntitlement]?.isActive == true &&
                    customerInfo.entitlements[premiumUserEntitlement]?.productIdentifier == annualProduct?.product?.sku -> {
                Purchases.sharedInstance.purchaseProduct(activity, monthlyProduct!!.product, UpgradeInfo(annualProduct!!.product!!.sku, ProrationMode.DEFERRED), object: ProductChangeCallback {
                    override fun onCompleted(
                        storeTransaction: StoreTransaction?,
                        customerInfo: CustomerInfo
                    ) {
                        onSuccess(customerInfo)
                    }

                    override fun onError(error: PurchasesError, userCancelled: Boolean) {
                        onFail(error, userCancelled)
                    }

                })
            }
        }
    }

    fun subscriptionActive(customerInfo: CustomerInfo): Boolean {
        val date = Math.max(customerInfo.requestDate.time, Date.from(Instant.now()).time)
        return customerInfo.entitlements[premiumUserEntitlement]?.isActive == true
                && (customerInfo.entitlements[premiumUserEntitlement]?.expirationDate == null
                    || customerInfo.entitlements[premiumUserEntitlement]?.expirationDate?.after(Date(date)) == true)
    }

    private fun isSubscriptionPaused(customerInfo: CustomerInfo) =
        !subscriptionActive(customerInfo) && customerInfo.entitlements[premiumUserEntitlement]?.willRenew == true
    fun hasSubscription(customerInfo: CustomerInfo) =
        subscriptionActive(customerInfo) || isSubscriptionPaused(customerInfo)

    fun purchase(
        product: StoreProduct,
        activity: Activity,
        onSuccess: (CustomerInfo) -> Unit,
        onFail: (PurchasesError, Boolean) -> Unit
    ) {
        (activity.application as PlaidApp).ignorePinCheck = true
        Purchases.sharedInstance.purchaseProduct(
            activity,
            product,
            object: PurchaseCallback {
                override fun onCompleted(
                    storeTransaction: StoreTransaction,
                    customerInfo: CustomerInfo
                ) {
                    onSuccess(customerInfo)
                }

                override fun onError(error: PurchasesError, userCancelled: Boolean) {
                    onFail(error, userCancelled)
                }

            })
    }

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

    data class SubscriptionInfo(
        val renewalType: String,
        val expireDate: String,
        val state: String
    )

    companion object {
        const val MONTHLY_PRODUCT = "\$rc_monthly"
        const val ANNUAL_PRODUCT = "\$rc_annual"
    }
}