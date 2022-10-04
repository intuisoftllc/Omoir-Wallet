package com.intuisoft.plaid.features.onboarding.ui

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.intuisoft.plaid.databinding.FragmentFingerprintSetupBinding
import com.intuisoft.plaid.features.onboarding.viewmodel.OnboardingViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.intuisoft.plaid.util.Constants
import java.util.concurrent.Executor


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
            if(viewModel.fingerprintEnrollRequired) {
                viewModel.fingerprintEnrollRequired = false
                navigateToSettings()
            } else {
                registerFingerprint()
            }
        }


        binding.registerFingerprint.isVisible = !viewModel.fingerprintEnrolled
        binding.registrationSuccess.isVisible = viewModel.fingerprintEnrolled
        viewModel.onBiometricRegisterSuccess.observe(viewLifecycleOwner, Observer {
            binding.registerFingerprint.isVisible = false
            binding.registrationSuccess.isVisible = true
        })
    }

    fun registerFingerprint() {
        var executor: Executor = ContextCompat.getMainExecutor(requireContext())
        var biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int,
                                                   errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                }

                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    viewModel.biometricAuthenticationSuccessful()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                }
            })

        var promptInfo: BiometricPrompt.PromptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Use Biometric Authentication")
            .setSubtitle("Use biometrics to add an additional layer of security to your wallet when signing transactions.")
            .setNegativeButtonText("Skip for now")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    override fun showActionBar(): Boolean {
        return false
    }

    override fun actionBarTitle(): Int {
        return 0
    }

    fun navigateToSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            requireActivity().startActivity(Intent(Settings.ACTION_BIOMETRIC_ENROLL));
        }
        else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                requireActivity().startActivity(Intent(Settings.ACTION_FINGERPRINT_ENROLL));
            }
            else {
                requireActivity().startActivity(Intent(Settings.ACTION_SECURITY_SETTINGS));
            }
        }
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