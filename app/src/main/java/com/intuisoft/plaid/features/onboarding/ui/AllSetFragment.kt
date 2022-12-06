package com.intuisoft.plaid.features.onboarding.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.*
import com.intuisoft.plaid.common.repositories.LocalStoreRepository
import com.intuisoft.plaid.databinding.FragmentOnboardingAllSetBinding
import com.intuisoft.plaid.features.onboarding.viewmodel.OnboardingViewModel
import com.intuisoft.plaid.common.util.Constants
import com.intuisoft.plaid.util.fragmentconfig.AllSetData
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel


class AllSetFragment : ConfigurableFragment<FragmentOnboardingAllSetBinding>(pinProtection = true) {
    private val viewModel: OnboardingViewModel by sharedViewModel()
    private val localStoreRepository: LocalStoreRepository by inject()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentOnboardingAllSetBinding.inflate(inflater, container, false)
        setupConfiguration(viewModel,
            listOf(
                FragmentConfigurationType.CONFIGURATION_All_SET
            )
        )
        return binding.root

    }

    // todo: clear backstack when navigating here
    override fun onConfiguration(configuration: FragmentConfiguration?) {
        when(configuration?.configurationType ?: FragmentConfigurationType.CONFIGURATION_NONE) {
            FragmentConfigurationType.CONFIGURATION_All_SET -> {
                val data = configuration!!.configData as AllSetData

                binding.successIcon.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(800)
                ignoreOnBackPressed()

                binding.allSetTitle.text = data.title
                binding.allSetDescription.text = data.subtitle
                binding.positiveButton.setButtonText(data.positiveText)
                binding.negativeButton.setButtonText(data.negativeText)

                binding.positiveButton.onClick {
                    val bundle = bundleOf(Constants.Navigation.WALLET_UUID_BUNDLE_ID to data.walletUUID)

                    findNavController().popBackStack(if(localStoreRepository.isProEnabled()) R.id.proHomescreenFragment else R.id.homescreenFragment, false)
                    navigate(
                        data.positiveDestination,
                        bundle,
                        navOptions {
                            anim {
                                enter = android.R.anim.fade_in
                                popEnter = android.R.anim.slide_in_left
                            }
                        }
                    )
                }

                binding.negativeButton.onClick {
                    val bundle = bundleOf(Constants.Navigation.WALLET_UUID_BUNDLE_ID to data.walletUUID)

                    findNavController().popBackStack(if(localStoreRepository.isProEnabled()) R.id.proHomescreenFragment else R.id.homescreenFragment, false)
                    navigate(
                        data.negativeDestination,
                        bundle,
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