package com.intuisoft.plaid.features.onboarding.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.*
import com.intuisoft.plaid.androidwrappers.delegates.FragmentConfiguration
import com.intuisoft.plaid.billing.BillingManager
import com.intuisoft.plaid.common.analytics.EventTracker
import com.intuisoft.plaid.common.analytics.events.EventOnboardingFinish
import com.intuisoft.plaid.common.repositories.LocalStoreRepository
import com.intuisoft.plaid.databinding.FragmentOnboardingPinBinding
import com.intuisoft.plaid.features.onboarding.viewmodel.OnboardingViewModel
import com.intuisoft.plaid.common.util.Constants
import com.intuisoft.plaid.util.fragmentconfig.AllSetData
import com.intuisoft.plaid.walletmanager.AbstractWalletManager
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel


class OnboardingPinFragment : BindingFragment<FragmentOnboardingPinBinding>() {
    private val viewModel: OnboardingViewModel by sharedViewModel()
    private val localStoreRepository: LocalStoreRepository by inject()
    protected val eventTracker: EventTracker by inject()
    protected val walletManager: AbstractWalletManager by inject()
    protected val billing: BillingManager by inject()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentOnboardingPinBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.passcodeView.setListener(object : PasscodeView.PasscodeViewListener {
            override fun onFail(wrongNumber: String?) {
                // do nothing
            }

            override fun onSuccess(number: String?) {
                viewModel.saveUserAlias()
                localStoreRepository.updatePinCheckedTime()
                walletManager.start()
                onNextStep()
            }

            override fun onMaxAttempts() {
                // not possible
            }

            override fun onScanFingerprint(listener: FingerprintScanResponse) {
                // should never come here
                listener.onScanFail()
            }

        })

        viewModel.fingerprintEnroll.observe(viewLifecycleOwner, Observer {
            if(it) {
                findNavController().navigate(
                    OnboardingPinFragmentDirections.actionOnboardingPinSetupFragmentToFingerprintSetupFragment(),
                    Constants.Navigation.ANIMATED_ENTER_EXIT_RIGHT_NAV_OPTION
                )
            } else {
                billing.shouldShowPremiumContent { hasSubscription ->
                    localStoreRepository.setOnboardingComplete(true)
                    eventTracker.log(EventOnboardingFinish())

                    var bundle = bundleOf(
                        Constants.Navigation.FRAGMENT_CONFIG to FragmentConfiguration(
                            configurationType = FragmentConfigurationType.CONFIGURATION_ONBOARDING_All_SET,
                            configData = AllSetData(
                                title = getString(R.string.all_set_title),
                                subtitle = getString(R.string.all_set_description),
                                positiveText = getString(R.string.create_new_wallet),
                                negativeText = getString(R.string.goto_homescreen),
                                positiveDestination = R.id.createWalletFragment,
                                negativeDestination = if (hasSubscription) R.id.proHomescreenFragment else R.id.homescreenFragment,
                                walletUUID = ""
                            )
                        )
                    )

                    navigate(
                        R.id.allSetFragment,
                        bundle
                    )
                }
            }
        })
    }

    override fun actionBarTitle(): Int {
        return 0
    }

    override fun onNavigateTo(destination: Int) {
        // ignore
    }

    override fun actionBarVariant(): Int {
        return TopBarView.NO_BAR
    }

    override fun actionBarSubtitle(): Int {
        return 0
    }

    override fun actionBarActionLeft(): Int {
        return 0
    }

    override fun actionBarActionRight(): Int {
        return 0
    }

    override fun onActionLeft() {
        // ignore
    }

    override fun onActionRight() {
        // ignore
    }

    override fun onSubtitleClicked() {
        // ignore
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun onNextStep() {
        viewModel.checkFingerprintSupport(
            onEnroll = {
                findNavController().navigate(
                    OnboardingPinFragmentDirections.actionOnboardingPinSetupFragmentToFingerprintSetupFragment(),
                    Constants.Navigation.ANIMATED_ENTER_EXIT_RIGHT_NAV_OPTION
                )
            }
        )
    }

    override fun navigationId(): Int {
        return R.id.onboardingPinSetupFragment
    }
}