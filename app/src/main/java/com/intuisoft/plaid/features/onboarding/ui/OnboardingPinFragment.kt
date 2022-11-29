package com.intuisoft.plaid.features.onboarding.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.BindingFragment
import com.intuisoft.plaid.androidwrappers.FingerprintScanResponse
import com.intuisoft.plaid.androidwrappers.PasscodeView
import com.intuisoft.plaid.androidwrappers.TopBarView
import com.intuisoft.plaid.databinding.FragmentOnboardingPinBinding
import com.intuisoft.plaid.features.onboarding.viewmodel.OnboardingViewModel
import com.intuisoft.plaid.features.pin.viewmodel.PinViewModel
import com.intuisoft.plaid.common.util.Constants
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel


class OnboardingPinFragment : BindingFragment<FragmentOnboardingPinBinding>() {
    private val viewModel: OnboardingViewModel by sharedViewModel()
    private val pinViewModel: PinViewModel by inject()

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
                pinViewModel.updatePinCheckedTime()
                pinViewModel.startWalletManager()
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
                    com.intuisoft.plaid.common.util.Constants.Navigation.ANIMATED_ENTER_EXIT_RIGHT_NAV_OPTION
                )
            } else {
                findNavController().navigate(
                    OnboardingPinFragmentDirections.actionOnboardingPinSetupFragmentToAllSetFragment(),
                    com.intuisoft.plaid.common.util.Constants.Navigation.ANIMATED_ENTER_EXIT_RIGHT_NAV_OPTION
                )
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
                    com.intuisoft.plaid.common.util.Constants.Navigation.ANIMATED_ENTER_EXIT_RIGHT_NAV_OPTION
                )
            }
        )
    }

    override fun navigationId(): Int {
        return R.id.onboardingPinSetupFragment
    }
}