package com.intuisoft.plaid.features.onboarding.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.intuisoft.plaid.features.onboarding.viewmodel.OnboardingViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.BindingFragment
import com.intuisoft.plaid.androidwrappers.TopBarView
import com.intuisoft.plaid.androidwrappers.validateFingerprint
import com.intuisoft.plaid.databinding.FragmentOnboardingFingerprintRegistrationBinding
import com.intuisoft.plaid.util.Constants


class FingerprintSetupFragment : BindingFragment<FragmentOnboardingFingerprintRegistrationBinding>() {
    private val viewModel: OnboardingViewModel by sharedViewModel()

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
            viewModel.checkFingerprintSupport(
                onEnroll = {
                    viewModel.navigateToFingerprintSettings(requireActivity())
                }
            )
        }

        binding.skip.onClick {
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

    override fun navigationId(): Int {
        return R.id.fingerprintSetupFragment
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun onNextStep() {
        findNavController().navigate(FingerprintSetupFragmentDirections.actionFingerprintSetupFragmentToAllSetFragment(),
            Constants.Navigation.ANIMATED_FADE_IN_EXIT_NAV_OPTION)
    }
}