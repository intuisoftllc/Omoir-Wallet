package com.intuisoft.plaid.features.onboarding.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import com.intuisoft.plaid.features.onboarding.viewmodel.OnboardingViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import androidx.lifecycle.Observer
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.*
import com.intuisoft.plaid.common.analytics.EventTracker
import com.intuisoft.plaid.common.analytics.events.EventOnboardingFingerprintRegister
import com.intuisoft.plaid.common.analytics.events.EventOnboardingFinish
import com.intuisoft.plaid.common.analytics.events.EventOnboardingSkipFingerprintRegister
import com.intuisoft.plaid.common.repositories.LocalStoreRepository
import com.intuisoft.plaid.databinding.FragmentOnboardingFingerprintRegistrationBinding
import com.intuisoft.plaid.common.util.Constants
import com.intuisoft.plaid.util.fragmentconfig.AllSetData
import org.koin.android.ext.android.inject


class FingerprintSetupFragment : BindingFragment<FragmentOnboardingFingerprintRegistrationBinding>() {
    private val viewModel: OnboardingViewModel by sharedViewModel()
    private val localStoreRepository: LocalStoreRepository by inject()
    protected val eventTracker: EventTracker by inject()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentOnboardingFingerprintRegistrationBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.register.onClick {
            eventTracker.log(EventOnboardingFingerprintRegister())
            viewModel.checkFingerprintSupport(
                onEnroll = {
                    viewModel.navigateToFingerprintSettings(requireActivity())
                }
            )
        }

        binding.skip.onClick {
            eventTracker.log(EventOnboardingSkipFingerprintRegister())
            onNextStep()
        }

        viewModel.fingerprintSupported.observe(viewLifecycleOwner, Observer {
            if(it) {
                validateFingerprint {
                    viewModel.biometricAuthenticationSuccessful()
                }
            }
        })

        viewModel.onBiometricRegisterSuccess.observe(viewLifecycleOwner, Observer {
            onNextStep()
        })
    }

    override fun onNavigateTo(destination: Int) {
        // ignore
    }

    override fun actionBarTitle(): Int {
        return 0
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

    override fun navigationId(): Int {
        return R.id.fingerprintSetupFragment
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun onNextStep() {
        localStoreRepository.setOnboardingComplete(true)
        eventTracker.log(EventOnboardingFinish())

        var bundle = bundleOf(
            Constants.Navigation.FRAGMENT_CONFIG to FragmentConfiguration(
                actionBarTitle = 0,
                actionBarSubtitle = 0,
                actionBarVariant = 0,
                actionLeft = 0,
                actionRight = 0,
                configurationType = FragmentConfigurationType.CONFIGURATION_ONBOARDING_All_SET,
                configData = AllSetData(
                    title = getString(R.string.all_set_title),
                    subtitle = getString(R.string.all_set_description),
                    positiveText = getString(R.string.create_new_wallet),
                    negativeText = getString(R.string.goto_homescreen),
                    positiveDestination = R.id.createWalletFragment,
                    negativeDestination = if(localStoreRepository.isProEnabled()) R.id.proHomescreenFragment else R.id.homescreenFragment,
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