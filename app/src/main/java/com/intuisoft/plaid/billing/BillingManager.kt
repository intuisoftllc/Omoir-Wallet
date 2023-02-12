package com.intuisoft.plaid.billing

import android.app.Activity
import android.app.Application
import android.net.Uri
import android.util.Log
import com.android.billingclient.api.BillingFlowParams.ProrationMode
import com.intuisoft.plaid.PlaidApp
import com.intuisoft.plaid.R
import com.intuisoft.plaid.common.CommonService
import com.intuisoft.plaid.common.local.AppPrefs
import com.intuisoft.plaid.common.util.extensions.roundTo
import com.intuisoft.plaid.features.settings.ui.PremiumSubscriptionsFragment
import com.intuisoft.plaid.util.NetworkUtil
import com.revenuecat.purchases.*
import com.revenuecat.purchases.interfaces.ProductChangeCallback
import com.revenuecat.purchases.interfaces.PurchaseCallback
import com.revenuecat.purchases.interfaces.ReceiveCustomerInfoCallback
import com.revenuecat.purchases.models.StoreProduct
import com.revenuecat.purchases.models.StoreTransaction
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*
import kotlin.math.roundToLong

class BillingManager(
    private val prefs: AppPrefs,
    private val application: Application
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
    fun shouldShowPremiumContent(callback: ((Boolean) -> Unit)? = null) {
        getCustomerInfo { info ->
            info?.let {
                prefs.isPremiumUser = hasSubscription(it)
            }

            callback?.invoke(prefs.isPremiumUser || CommonService.getPremiumOverride())
        }
    }

    fun getCurrentSubscription(
        activity: Activity,
        callback: (SubscriptionInfo) -> Unit
    ) {
        val failed = SubscriptionInfo(
            renewalType = activity.getString(R.string.unknown),
            expireDate = activity.getString(R.string.not_applicable),
            state = activity.getString(R.string.failed_to_get_state),
            managementUrl = Uri.EMPTY
        )

        getCustomerInfo { info ->
            if(info != null) {
                getProducts { products ->
                    if(products != null) {
                        callback(
                            buildSubscriptionInfo(
                                activity = activity,
                                customerInfo = info,
                                products = products
                            )
                        )
                    } else {
                        callback(
                            failed
                        )
                    }
                }
            } else {
                callback(
                    failed
                )
            }
        }
    }

    private fun getCustomerInfo(callback: (CustomerInfo?) -> Unit) {
        Purchases.sharedInstance.getCustomerInfo(object : ReceiveCustomerInfoCallback {
            override fun onError(error: PurchasesError) {
                if(com.intuisoft.plaid.BuildConfig.LOGGING_ENABLED) Log.e(TAG, "error: $error")
                callback?.invoke(null)
            }

            override fun onReceived(customerInfo: CustomerInfo) {
                if(com.intuisoft.plaid.BuildConfig.LOGGING_ENABLED) Log.i(TAG, "customerInfo: $customerInfo")
                callback?.invoke(customerInfo)
            }

        })
    }
    private fun buildSubscriptionInfo(
        activity: Activity,
        customerInfo: CustomerInfo,
        products: List<Package>
    ): SubscriptionInfo {
        val product = products.find { it.product.sku == customerInfo.entitlements[premiumUserEntitlement]?.productIdentifier }
        var expire: String = "?"
        try {
            if(isSubscriptionExpired(customerInfo)) {
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
                    else activity.getString(R.string.premium_subscription_state_type_1),
            managementUrl = customerInfo.managementURL
        )

    }

    fun upgradeDownGrade(
        activity: Activity,
        onSuccess: (Boolean) -> Unit,
        onFail: (PurchasesError, Boolean) -> Unit
    ) {

        getCustomerInfo { info ->
            if(info != null) {
                getProducts { products ->
                    if(products != null) {
                        val monthlyProduct = products.find { it.identifier == MONTHLY_PRODUCT }
                        val annualProduct = products.find { it.identifier == ANNUAL_PRODUCT }

                        when {
                            info.entitlements[premiumUserEntitlement]?.isActive == true &&
                                    info.entitlements[premiumUserEntitlement]?.productIdentifier == monthlyProduct?.product?.sku -> {
                                (activity.application as PlaidApp).ignorePinCheck = true

                                Purchases.sharedInstance.purchaseProduct(
                                    activity,
                                    annualProduct!!.product,
                                    UpgradeInfo(
                                        monthlyProduct!!.product!!.sku,
                                        ProrationMode.DEFERRED
                                    ),
                                    object: ProductChangeCallback {
                                        override fun onCompleted(
                                            storeTransaction: StoreTransaction?,
                                            customerInfo: CustomerInfo
                                        ) {
                                            onSuccess(hasSubscription(info))
                                        }

                                        override fun onError(error: PurchasesError, userCancelled: Boolean) {
                                            onFail(error, userCancelled)
                                        }

                                    }
                                )
                            }
                            info.entitlements[premiumUserEntitlement]?.isActive == true &&
                                    info.entitlements[premiumUserEntitlement]?.productIdentifier == annualProduct?.product?.sku -> {
                                (activity.application as PlaidApp).ignorePinCheck = true

                                Purchases.sharedInstance.purchaseProduct(
                                    activity,
                                    monthlyProduct!!.product,
                                    UpgradeInfo(
                                        annualProduct!!.product!!.sku,
                                        ProrationMode.DEFERRED
                                    ),
                                    object: ProductChangeCallback {
                                        override fun onCompleted(
                                            storeTransaction: StoreTransaction?,
                                            customerInfo: CustomerInfo
                                        ) {
                                            onSuccess(hasSubscription(info))
                                        }

                                        override fun onError(error: PurchasesError, userCancelled: Boolean) {
                                            onFail(error, userCancelled)
                                        }

                                    }
                                )
                            }
                        }
                    } else {
                        onFail(PurchasesError(PurchasesErrorCode.NetworkError), false)
                    }
                }
            } else {
                onFail(PurchasesError(PurchasesErrorCode.NetworkError), false)
            }
        }
    }

    private fun isSubscriptionExpired(customerInfo: CustomerInfo): Boolean {
        val date: Date // to prevent user from hacking the date we try to use the latest most date
        if(Date.from(Instant.now()).before(customerInfo.requestDate) || !NetworkUtil.hasInternet(application)) {
            date = customerInfo.requestDate
        } else {
            date = Date.from(Instant.now())
        }

        return customerInfo.entitlements[premiumUserEntitlement]?.expirationDate != null
                && customerInfo.entitlements[premiumUserEntitlement]?.expirationDate?.after(date) == false
    }

    private fun subscriptionActive(customerInfo: CustomerInfo): Boolean {
        return customerInfo.entitlements[premiumUserEntitlement]?.isActive == true
                && !isSubscriptionExpired(customerInfo)
    }

    private fun isSubscriptionPaused(customerInfo: CustomerInfo) =
        !subscriptionActive(customerInfo) && customerInfo.entitlements[premiumUserEntitlement]?.willRenew == true
    private fun hasSubscription(customerInfo: CustomerInfo) =
        subscriptionActive(customerInfo) || isSubscriptionPaused(customerInfo)

    fun purchase(
        activity: Activity,
        product: StoreProduct,
        onSuccess: (Boolean) -> Unit,
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
                    prefs.isPremiumUser = hasSubscription(customerInfo)
                    onSuccess(prefs.isPremiumUser)
                }

                override fun onError(error: PurchasesError, userCancelled: Boolean) {
                    onFail(error, userCancelled)
                }

            })
    }

    fun getSubscriptionProducts(callback: (List<Product>) -> Unit) {
        getProducts {
            if(it != null) {
                val monthly = it?.find { it.identifier == PremiumSubscriptionsFragment.MONTHLY_PRODUCT }
                val annual = it?.find { it.identifier == PremiumSubscriptionsFragment.ANNUAL_PRODUCT }

                if(monthly != null && annual != null) {
                    val annualPricePerMonth = (annual.product.priceAmountMicros.toDouble() / 1000000) / 12
                    val pricePerMonth = monthly.product.priceAmountMicros.toDouble() / 1000000
                    val savings = 100 - ((annualPricePerMonth / pricePerMonth) * 100)

                    callback(
                        listOf(
                            Product(
                                isMonthly = true,
                                price = monthly.product.price,
                                priceConversion = null,
                                saveAmount = null,
                                storeProduct = monthly.product
                            ),
                            Product(
                                isMonthly = false,
                                price = annual.product.price,
                                priceConversion = ((annual.product.priceAmountMicros.toDouble() / 1000000) / 12).roundTo(2).toString(),
                                saveAmount = savings.roundToLong().toString(),
                                storeProduct = annual.product
                            )
                        )
                    )
                } else {
                    callback(listOf())
                }
            } else {
                callback(listOf())
            }
        }
    }

    private fun getProducts(callback: (List<Package>?) -> Unit) {
        Purchases.sharedInstance.getOfferingsWith({ error ->
            callback(null)
        }) { offerings ->
            offerings.current?.availablePackages?.takeUnless { it.isNullOrEmpty() }?.let {
                // Display packages for sale
                if(com.intuisoft.plaid.BuildConfig.LOGGING_ENABLED) Log.i(TAG, "packages: $it")
                callback(it)
            }
        }
    }

    data class Product(
        val isMonthly: Boolean,
        val price: String,
        val priceConversion: String?,
        val saveAmount: String?,
        val storeProduct: StoreProduct
    )

    data class SubscriptionInfo(
        val renewalType: String,
        val expireDate: String,
        val state: String,
        val managementUrl: Uri?
    )

    companion object {
        const val MONTHLY_PRODUCT = "\$rc_monthly"
        const val ANNUAL_PRODUCT = "\$rc_annual"
    }
}