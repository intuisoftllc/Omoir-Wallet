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
import com.intuisoft.plaid.databinding.FragmentPurchaseSubscriptionBinding
import com.intuisoft.plaid.features.settings.viewmodel.SettingsViewModel
import com.intuisoft.plaid.features.settings.viewmodel.SubscriptionViewModel
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel


class PurchaseSubscriptionFragment : ConfigurableFragment<FragmentPurchaseSubscriptionBinding>(
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

        _binding = FragmentPurchaseSubscriptionBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onConfiguration(configuration: FragmentConfiguration?) {
        binding.subscribe.enableButton(false)
        billing.getSubscriptionProducts { products ->
            withBinding {
                loading.isVisible = false

                if (products.isNotEmpty()) {
                    val monthly = products.find { it.isMonthly }!!
                    val annual = products.find { !it.isMonthly }!!

                    benefit1.isVisible = true
                    benefit2.isVisible = true

                    option1.onRadioClicked { view, clicked ->
                        if (clicked) {
                            viewModel.setPurchaseProduct(annual.storeProduct)
                            option2.checkRadio(false)
                        }
                    }

                    option2.onRadioClicked { view, clicked ->
                        if (clicked) {
                            viewModel.setPurchaseProduct(monthly.storeProduct)
                            option1.checkRadio(false)
                        }
                    }

                    annualPrice.text =
                        getString(R.string.premium_subscription_annual_price, annual.price)
                    annualMonthlyConversion.text = getString(
                        R.string.premium_subscription_monthly_price_conversion,
                        annual.priceConversion
                    )
                    saveAmount.text = getString(
                        R.string.premium_subscription_monthly_price_savings,
                        annual.saveAmount
                    )
                    monthlyPrice.text =
                        getString(R.string.premium_subscription_monthly_price, monthly.price)
                    option1.checkRadio(true)
                } else {
                    styledSnackBar(
                        requireView(),
                        getString(R.string.premium_subscriptions_load_error),
                        true
                    )
                }
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
                                withBinding {
                                    subscribe.enableButton(true)
                                }
                            }
                        }
                    },
                    onFail = { error, cancelled ->
                        withBinding {
                            subscribe.enableButton(true)
                        }

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
        return R.id.purchaseSubscriptionFragment
    }
}