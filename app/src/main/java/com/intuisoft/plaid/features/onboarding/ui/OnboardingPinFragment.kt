package com.intuisoft.plaid.features.onboarding.ui

import android.R
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.intuisoft.plaid.androidwrappers.FingerprintScanResponse
import com.intuisoft.plaid.androidwrappers.PasscodeView
import com.intuisoft.plaid.databinding.FragmentOnboardingPinBinding
import com.intuisoft.plaid.databinding.FragmentWelcomeBinding
import com.intuisoft.plaid.features.onboarding.viewmodel.OnboardingViewModel
import com.intuisoft.plaid.features.pin.viewmodel.PinViewModel
import com.intuisoft.plaid.util.Constants
import com.intuisoft.plaid.util.Constants.Limit.MAX_ALIAS_LENGTH
import com.intuisoft.plaid.util.entensions.hideSoftKeyboard
import com.intuisoft.plaid.util.entensions.ignoreOnBackPressed
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel


class OnboardingPinFragment : OnboardingFragment<FragmentOnboardingPinBinding>() {
    private val viewModel: OnboardingViewModel by sharedViewModel()
    private val pinViewModel: PinViewModel by inject()
    override val onboardingStep = 2

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
                viewModel.savePin(number)
                pinViewModel.updatePinCheckedTime()
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

        viewModel.fingerprintSupported.observe(viewLifecycleOwner, Observer {
            if(it) {
                findNavController().navigate(
                    OnboardingPinFragmentDirections.actionOnboardingPinSetupFragmentToFingerprintSetupFragment(),
                    Constants.Navigation.ANIMATED_ENTER_EXIT_RIGHT_NAV_OPTION
                )
            } else {
                // goto to end of onboarding screen
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

    override fun onNextStep() {
        viewModel.checkFingerprintSupport()
    }

    override fun onPrevStep() {
        // do nothing
    }
}