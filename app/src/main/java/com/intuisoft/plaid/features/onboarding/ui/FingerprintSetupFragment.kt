package com.intuisoft.plaid.features.onboarding.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.intuisoft.plaid.databinding.FragmentFingerprintSetupBinding
import com.intuisoft.plaid.features.onboarding.viewmodel.OnboardingViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.intuisoft.plaid.util.Constants
import com.intuisoft.plaid.util.entensions.validateFingerprint


class FingerprintSetupFragment : OnboardingFragment<FragmentFingerprintSetupBinding>() {
    private val viewModel: OnboardingViewModel by sharedViewModel()
    override val onboardingStep = 3

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentFingerprintSetupBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.enableNextButton(true)
        binding.registerFingerprint.onClick {
            viewModel.checkFingerprintSupport(
                onEnroll = {
                    viewModel.navigateToFingerprintSettings(requireActivity())
                }
            )
        }

        viewModel.fingerprintSupported.observe(viewLifecycleOwner, Observer {
            if(it) {
                validateFingerprint {
                    viewModel.biometricAuthenticationSuccessful()
                }
            }
        })

        binding.registerFingerprint.isVisible = !viewModel.fingerprintEnrolled
        binding.registrationSuccess.isVisible = viewModel.fingerprintEnrolled
        viewModel.onBiometricRegisterSuccess.observe(viewLifecycleOwner, Observer {
            binding.registerFingerprint.isVisible = false
            binding.registrationSuccess.isVisible = true
        })
    }


    override fun showActionBar(): Boolean {
        return false
    }

    override fun actionBarTitle(): Int {
        return 0
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onNextStep() {
        findNavController().navigate(FingerprintSetupFragmentDirections.actionFingerprintSetupFragmentToAllSetFragment(),
            Constants.Navigation.ANIMATED_FADE_IN_EXIT_NAV_OPTION)
    }

    override fun onPrevStep() {
        // do nothing
    }
}