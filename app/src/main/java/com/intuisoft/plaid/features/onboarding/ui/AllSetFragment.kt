package com.intuisoft.plaid.features.onboarding.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.*
import com.intuisoft.plaid.androidwrappers.delegates.FragmentConfiguration
import com.intuisoft.plaid.common.analytics.EventTracker
import com.intuisoft.plaid.common.analytics.events.EventOnboardingCreateWallet
import com.intuisoft.plaid.common.analytics.events.EventOnboardingGotoHomescreen
import com.intuisoft.plaid.common.repositories.LocalStoreRepository
import com.intuisoft.plaid.databinding.FragmentOnboardingAllSetBinding
import com.intuisoft.plaid.features.onboarding.viewmodel.OnboardingViewModel
import com.intuisoft.plaid.util.fragmentconfig.AllSetData
import com.intuisoft.plaid.walletmanager.AbstractWalletManager
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel


class AllSetFragment : ConfigurableFragment<FragmentOnboardingAllSetBinding>(
    pinProtection = true,
    requiresWallet = false
) {
    private val viewModel: OnboardingViewModel by sharedViewModel()
    private val localStoreRepository: LocalStoreRepository by inject()
    protected val eventTracker: EventTracker by inject()
    protected val walletManager: AbstractWalletManager by inject()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentOnboardingAllSetBinding.inflate(inflater, container, false)
        setupConfiguration(viewModel,
            listOf(
                FragmentConfigurationType.CONFIGURATION_All_SET,
                FragmentConfigurationType.CONFIGURATION_ONBOARDING_All_SET
            )
        )
        return binding.root

    }

    override fun onConfiguration(configuration: FragmentConfiguration?) {
        when(configuration?.configurationType ?: FragmentConfigurationType.CONFIGURATION_NONE) {
            FragmentConfigurationType.CONFIGURATION_ONBOARDING_All_SET,
            FragmentConfigurationType.CONFIGURATION_All_SET -> {
                val data = configuration!!.configData as AllSetData

                binding.successIcon.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(800)

                binding.allSetTitle.text = data.title
                binding.allSetDescription.text = data.subtitle
                binding.positiveButton.setButtonText(data.positiveText)
                binding.negativeButton.setButtonText(data.negativeText)

                binding.positiveButton.onClick {
                    if(data.positiveDestination == R.id.createWalletFragment
                        && configuration.configurationType == FragmentConfigurationType.CONFIGURATION_ONBOARDING_All_SET) {
                        eventTracker.log(EventOnboardingCreateWallet())
                    }

                    if(data.walletUUID.isNotBlank()) {
                        walletManager.openWallet(walletManager.findLocalWallet(data.walletUUID)!!)
                    }

                    navigate(
                        data.positiveDestination,
                        navOptions {
                            anim {
                                enter = android.R.anim.fade_in
                                popEnter = android.R.anim.slide_in_left
                            }
                        }
                    )
                }

                binding.negativeButton.onClick {
                    if((data.negativeDestination == R.id.proHomescreenFragment || data.negativeDestination == R.id.homescreenFragment)
                        && configuration.configurationType == FragmentConfigurationType.CONFIGURATION_ONBOARDING_All_SET) {
                        eventTracker.log(EventOnboardingGotoHomescreen())
                    }

                    navigate(
                        data.negativeDestination,
                        navOptions {
                            anim {
                                enter = android.R.anim.fade_in
                                popEnter = android.R.anim.slide_in_left
                            }
                        }
                    )
                }
            }
            else -> {
                throw UnsupportedOperationException("Invalid configuration set ${configuration?.configurationType}")
            }
        }
    }

    override fun navigationId(): Int {
        return R.id.allSetFragment
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}