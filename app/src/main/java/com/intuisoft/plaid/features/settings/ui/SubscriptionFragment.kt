package com.intuisoft.plaid.features.settings.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.ConfigurableFragment
import com.intuisoft.plaid.androidwrappers.delegates.FragmentConfiguration
import com.intuisoft.plaid.androidwrappers.TopBarView
import com.intuisoft.plaid.androidwrappers.openLink
import com.intuisoft.plaid.androidwrappers.styledSnackBar
import com.intuisoft.plaid.billing.BillingManager
import com.intuisoft.plaid.common.util.Constants
import com.intuisoft.plaid.databinding.FragmentSubscriptionBinding
import com.intuisoft.plaid.features.settings.viewmodel.SettingsViewModel
import com.intuisoft.plaid.features.settings.viewmodel.SubscriptionViewModel
import kotlinx.android.synthetic.main.fragment_subscription.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel


class SubscriptionFragment : ConfigurableFragment<FragmentSubscriptionBinding>(
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

        _binding = FragmentSubscriptionBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onConfiguration(configuration: FragmentConfiguration?) {
        billing.getCurrentSubscription(
            activity = requireActivity()
        ) { sub ->
            binding.info1.setSubTitleText(sub.renewalType)
            binding.info2.setSubTitleText(sub.expireDate)
            binding.info3.setSubTitleText(sub.state)

            // not the best way to do this but...
            binding.switchSubscription.isVisible = sub.state == getString(R.string.premium_subscription_state_type_1)
            if(getString(R.string.premium_subscription_renewal_type_1) == sub.renewalType) { // monthly
                binding.switchSubscription.setButtonText(getString(R.string.premium_subscription_switch_annual))
            } else {
                binding.switchSubscription.setButtonText(getString(R.string.premium_subscription_switch_monthly))
            }

            binding.switchSubscription.onClick(Constants.Time.MIN_CLICK_INTERVAL_LONG) {
                binding.switchSubscription.enableButton(false)
                billing.upgradeDownGrade(
                    activity = requireActivity(),
                    onSuccess = { subActive ->
                        binding.switchSubscription.enableButton(true)

                        if (subActive) {
                            styledSnackBar(requireView(), getString(R.string.premium_subscriptions_update_success))
                            onBackPressed()
                        } else {
                            styledSnackBar(requireView(), getString(R.string.premium_subscriptions_update_error))
                            onBackPressed()
                        }

                    },
                    onFail = { error, cancelled ->
                        binding.switchSubscription.enableButton(true)

                        if(cancelled) {
                            styledSnackBar(requireView(), getString(R.string.premium_subscriptions_cancelled_purchase), true)
                        } else {
                            FirebaseCrashlytics.getInstance().log(error.message)
                            styledSnackBar(requireView(), getString(R.string.premium_subscriptions_failed_purchase, error.message), true)
                        }
                    }
                )
            }

            binding.manage.onClick {
                sub.managementUrl?.let {
                    requireContext().openLink(it)
                }
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
        return R.id.currentSubscriptionFragment
    }

}