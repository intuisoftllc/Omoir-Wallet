package com.intuisoft.plaid.features.pin.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.navOptions
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.BindingFragment
import com.intuisoft.plaid.androidwrappers.FingerprintScanResponse
import com.intuisoft.plaid.androidwrappers.PasscodeView
import com.intuisoft.plaid.databinding.FragmentPinEntryBinding
import com.intuisoft.plaid.databinding.FragmentWelcomeBinding
import com.intuisoft.plaid.features.onboarding.ui.OnboardingFragment
import com.intuisoft.plaid.features.onboarding.ui.WelcomeFragmentDirections
import com.intuisoft.plaid.features.onboarding.viewmodel.OnboardingViewModel
import com.intuisoft.plaid.features.pin.viewmodel.PinViewModel
import com.intuisoft.plaid.util.Constants
import com.intuisoft.plaid.util.entensions.hideSoftKeyboard
import com.intuisoft.plaid.util.entensions.ignoreOnBackPressed
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.concurrent.Executor

class PinFragment: BindingFragment<FragmentPinEntryBinding>() {
    private val pinViewModel: PinViewModel by inject()

    var setupPin = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentPinEntryBinding.inflate(inflater, container, false)
        setupPin = arguments?.get("setupPin") as? Boolean ?: false
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if(setupPin) {
            binding.passcodeView.setFirstInputTip(getString(R.string.enter_pin_to_reset_message))
            binding.passcodeView.resetView()
            binding.passcodeView.disableFingerprint()
        } else {
            ignoreOnBackPressed()
        }

        binding.passcodeView.setLocalPasscode(pinViewModel.pin)

        binding.passcodeView.setListener(object: PasscodeView.PasscodeViewListener {
            override fun onFail(wrongNumber: String?) {
                // do nothing
            }

            override fun onSuccess(number: String?) {
                pinViewModel.updatePinCheckedTime()

                if(setupPin) {
                    setupPin = false
                    binding.passcodeView.setPasscodeType(PasscodeView.PasscodeViewType.TYPE_SET_PASSCODE)
                    binding.passcodeView.setFirstInputTip(getString(R.string.create_pin_tip_message))
                    binding.passcodeView.setSecondInputTip(getString(R.string.re_enter_pin_tip_message))
                    binding.passcodeView.disablePinAttemptTracking()
                    binding.passcodeView.resetView()
                } else {
                    if(binding.passcodeView.passcodeType == PasscodeView.PasscodeViewType.TYPE_SET_PASSCODE) {
                        pinViewModel.updatePin(number!!)
                    }

                    findNavController().popBackStack()
                }
            }

            override fun onMaxAttempts() {
                this@PinFragment.pinViewModel.onMaxAttempts()
                findNavController().navigate(PinFragmentDirections.actionPinFragmentToSplashFragment(), navOptions {
                    popUpTo(R.id.pinFragment) {
                        inclusive = true
                    }

                })
            }

            override fun onScanFingerprint(listener: FingerprintScanResponse) {
                var executor: Executor = ContextCompat.getMainExecutor(requireContext())
                var biometricPrompt = BiometricPrompt(this@PinFragment, executor,
                    object : BiometricPrompt.AuthenticationCallback() {
                        override fun onAuthenticationError(errorCode: Int,
                                                           errString: CharSequence) {
                            super.onAuthenticationError(errorCode, errString)
                            listener.onScanFail()
                        }

                        override fun onAuthenticationSucceeded(
                            result: BiometricPrompt.AuthenticationResult) {
                            super.onAuthenticationSucceeded(result)
                            listener.onScanSuccess()
                        }

                        override fun onAuthenticationFailed() {
                            super.onAuthenticationFailed()
                        }
                    })

                var promptInfo: BiometricPrompt.PromptInfo = BiometricPrompt.PromptInfo.Builder()
                    .setTitle("Use Biometric Authentication")
                    .setSubtitle("Unlock Plaid Crypto Wallet™ using biometrics.")
                    .setNegativeButtonText("Use pin")
                    .build()

                biometricPrompt.authenticate(promptInfo)
            }

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
}