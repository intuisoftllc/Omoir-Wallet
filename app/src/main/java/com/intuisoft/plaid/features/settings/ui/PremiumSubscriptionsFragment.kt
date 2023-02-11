package com.intuisoft.plaid.features.settings.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.ConfigurableFragment
import com.intuisoft.plaid.androidwrappers.delegates.FragmentConfiguration
import com.intuisoft.plaid.androidwrappers.TopBarView
import com.intuisoft.plaid.androidwrappers.styledSnackBar
import com.intuisoft.plaid.billing.BillingManager
import com.intuisoft.plaid.common.util.extensions.roundTo
import com.intuisoft.plaid.databinding.FragmentPremiumSubscriptionsBinding
import com.intuisoft.plaid.features.settings.viewmodel.SettingsViewModel
import com.intuisoft.plaid.features.settings.viewmodel.SubscriptionViewModel
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesError
import com.revenuecat.purchases.interfaces.PurchaseCallback
import com.revenuecat.purchases.models.StoreTransaction
import com.revenuecat.purchases.purchasePackageWith
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import kotlin.math.roundToLong


class PremiumSubscriptionsFragment : ConfigurableFragment<FragmentPremiumSubscriptionsBinding>(
    pinProtection = true,
    requiresWallet = false
) {
    private val viewModel: SubscriptionViewModel by sharedViewModel()
    private val settingsViewModel: SettingsViewModel by sharedViewModel()
    private val billing: BillingManager by inject()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentPremiumSubscriptionsBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onConfiguration(configuration: FragmentConfiguration?) {
        binding.subscribe.enableButton(false)
        billing.getSubscriptionProducts { products ->
            binding.loading.isVisible = false

            if(products.isNotEmpty()) {
                val monthly = products.find { it.isMonthly }!!
                val annual = products.find { !it.isMonthly }!!

                binding.benefit1.isVisible = true
                binding.benefit2.isVisible = true

                binding.option1.onRadioClicked { view, clicked ->
                    if(clicked) {
                        viewModel.setPurchaseProduct(annual.storeProduct)
                        binding.option2.checkRadio(false)
                    }
                }

                binding.option2.onRadioClicked { view, clicked ->
                    if(clicked) {
                        viewModel.setPurchaseProduct(monthly.storeProduct)
                        binding.option1.checkRadio(false)
                    }
                }

                binding.annualPrice.text = getString(R.string.premium_subscription_annual_price, annual.price)
                binding.annualMonthlyConversion.text = getString(R.string.premium_subscription_monthly_price_conversion, annual.priceConversion)
                binding.saveAmount.text = getString(R.string.premium_subscription_monthly_price_savings, annual.saveAmount)
                binding.monthlyPrice.text = getString(R.string.premium_subscription_monthly_price, monthly.price)
                binding.option1.checkRadio(true)
            } else {
                styledSnackBar(requireView(), getString(R.string.premium_subscriptions_load_error), true)
            }
        }

        viewModel.purchaseProductUpdated.observe(viewLifecycleOwner, Observer {
            binding.subscribe.enableButton(true)
        })

        binding.subscribe.onClick {
            binding.subscribe.enableButton(false)

            viewModel.getPurchaseProduct()?.let {
                billing.purchase(
                    product = it,
                    activity = requireActivity(),
                    onSuccess = {
                        view?.let { view ->
                            if (it) {
                                styledSnackBar(
                                    view,
                                    getString(R.string.premium_subscriptions_success)
                                )
                                settingsViewModel.appRestartNeeded = true
                                onBackPressed()
                            } else {
                                styledSnackBar(
                                    view,
                                    getString(R.string.premium_subscriptions_update_error)
                                )
                                binding.subscribe.enableButton(true)
                            }
                        }
                    },
                    onFail = { error, cancelled ->
                        binding.subscribe.enableButton(true)

                        if(cancelled) {
                            styledSnackBar(requireView(), getString(R.string.premium_subscriptions_cancelled_purchase), true)
                        } else {
                            FirebaseCrashlytics.getInstance().log(error.message)
                            styledSnackBar(requireView(), getString(R.string.premium_subscriptions_failed_purchase, error.message), true)
                        }
                    }
                )
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun actionBarVariant(): Int {
        return TopBarView.NO_BAR
    }

    override fun actionBarTitle(): Int {
        return 0
    }

    override fun actionBarActionLeft(): Int {
        return R.drawable.ic_arrow_left
    }

    override fun onActionLeft() {
        findNavController().popBackStack()
    }

    override fun navigationId(): Int {
        return R.id.premiumSubscriptionFragment
    }

    companion object {
        const val MONTHLY_PRODUCT = "\$rc_monthly"
        const val ANNUAL_PRODUCT = "\$rc_annual"
    }
}